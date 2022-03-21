package com.arch.jonnyhsia.compass.compiler

import com.arch.jonnyhsia.compass.api.*
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import kotlin.concurrent.thread

@KotlinPoetKspPreview
@KspExperimental
class CompassProcessor(
    environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    companion object {
        private val ROUTE_NAME = Route::class.qualifiedName!!
        private const val TABLE_FULL_PATH = "compassTable"
        private const val DEFAULT_SCHEME = "compassDefaultPageScheme"

        var count = 0
    }

    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger

    private val tablePackage: String
    private val tableName: String

    private val routeSymbols = ArrayList<KspRouteSymbol>()

    init {
        val tableFullName = environment.options[TABLE_FULL_PATH]
        if (!tableFullName.isNullOrEmpty()) {
            val indexOfLastPeriod = tableFullName.lastIndexOf(".")
            tablePackage = tableFullName.substring(0, indexOfLastPeriod)
            tableName = tableFullName.substring(indexOfLastPeriod + 1)
        } else {
            tablePackage = "com.arch.jonnyhsia.compass"
            tableName = "CompassTable"
        }
    }

    private lateinit var activityType: KSType
    private lateinit var fragmentType: KSType

    override fun process(resolver: Resolver): List<KSAnnotated> {
        activityType = resolver.getClassDeclarationByName("android.app.Activity")!!.asType()
        fragmentType =
            resolver.getClassDeclarationByName("androidx.fragment.app.Fragment")!!.asType()

        logger.warn("Start processing...")
        val symbols = resolver.getSymbolsWithAnnotation(ROUTE_NAME)
        val ret = symbols.filterNot { it.validate() }.toList()
        symbols.filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .forEach { type ->
                routeSymbols.add(KspRouteSymbol(type))
            }
        logger.warn("Count: ${routeSymbols.size}")
        return ret
    }

    override fun finish() {
        generateCode(routeSymbols)
        super.finish()
    }

    private fun generateCode(annotations: List<KspRouteSymbol>) {
        thread {
            val filename = tableName
            codeGenerator.createNewFile(
                dependencies = Dependencies(true),
                packageName = tablePackage,
                fileName = filename,
                extensionName = "kt"
            ).writer().use { writer ->
                val tableType = HashMap::class.asClassName().parameterizedBy(
                    PageKey::class.asTypeName(),
                    CompassPage::class.asTypeName()
                )
                logger.warn("hello: $annotations")
                FileSpec.builder(tablePackage, filename)
                    .addType(
                        TypeSpec.classBuilder(filename)
                            .addSuperinterface(ICompassTable::class)
                            .primaryConstructor(
                                FunSpec.constructorBuilder()
                                    .build()
                            )
                            .addFunction(
                                FunSpec.builder("getPages")
                                    .addModifiers(KModifier.OVERRIDE)
                                    .returns(tableType)
                                    .collectAllRoutesAndReturn(annotations, tableType)
                                    .build()
                            )
                            .build()
                    )
                    .build()
                    .writeTo(writer)
            }
        }
    }

    private fun FunSpec.Builder.collectAllRoutesAndReturn(
        symbols: List<KspRouteSymbol>,
        tableType: ParameterizedTypeName
    ): FunSpec.Builder {
        logger.warn("hello222")
        addComment("Read routes(${symbols.size}) from annotations and generate router map")
        addStatement("val map = %T()", tableType)

        for (symbol in symbols) {
            val isActivity = activityType.isAssignableFrom(symbol.targetKsType)
            val isFragment = fragmentType.isAssignableFrom(symbol.targetKsType)
//            logger.warn("${symbol.symbol.asType().toClassName().packageName} isAssignableFrom: $result")
            logger.warn("Activity isAssignableFrom symbol: $isActivity")
            logger.warn("Fragment isAssignableFrom symbol: $isFragment")

            val targetType: TargetType = when {
                isActivity -> TargetType.ACTIVITY
                isFragment -> TargetType.FRAGMENT
                else -> TargetType.UNKNOWN
            }
            addStatement(
                "map[%T(%S)] = %T(%S, %T::class.java, %L, %T.%L, %S)",
                PageKey::class.java,
                symbol.route.name,
                CompassPage::class.java,
                symbol.route.name,
                symbol.target,
                symbol.route.requestCode,
                targetType::class.java, targetType.name,
                symbol.route.scheme
            )
        }
        addCode("return map")
        return this
    }
//    inner class RouteVisitor : KSVisitorVoid() {
//        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
//            super.visitClassDeclaration(classDeclaration, data)
//        }
//    }
}
