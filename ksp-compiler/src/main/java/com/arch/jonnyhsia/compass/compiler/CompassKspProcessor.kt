package com.arch.jonnyhsia.compass.compiler

import com.arch.jonnyhsia.compass.facade.CompassMeta
import com.arch.jonnyhsia.compass.facade.CompassPage
import com.arch.jonnyhsia.compass.facade.CompassEcho
import com.arch.jonnyhsia.compass.facade.ICompassTable
import com.arch.jonnyhsia.compass.facade.annotation.Route
import com.arch.jonnyhsia.compass.facade.enums.TargetType
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.concurrent.thread

@KotlinPoetKspPreview
@KspExperimental
class CompassKspProcessor(
    environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private companion object {
        val ROUTE_NAME = Route::class.qualifiedName!!
        const val TABLE_FULL_PATH = "compassTable"
    }

    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger

    private val tablePackage: String
    private val tableName: String

    private val routeSymbols = ArrayList<KspRouteSymbol>()

    private lateinit var dependencies: Dependencies

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

    private lateinit var blockType: KSType
    private lateinit var activityType: KSType
    private lateinit var fragmentType: KSType
    private lateinit var stringType: KSType

    override fun process(resolver: Resolver): List<KSAnnotated> {
        stringType = resolver.builtIns.stringType
        blockType =
            resolver.getClassDeclarationByName("com.arch.jonnyhsia.compass.facade.IRouteEcho")!!
                .asType()
        activityType = resolver.getClassDeclarationByName("android.app.Activity")!!.asType()
        fragmentType =
            resolver.getClassDeclarationByName("androidx.fragment.app.Fragment")!!.asType()

        logger.warn("Start processing...")
        logger.warn("getSymbolsWithAnnotation: $ROUTE_NAME")
        val symbols = resolver.getSymbolsWithAnnotation(ROUTE_NAME)

        dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())

        symbols.forEach { type ->
            if (type is KSClassDeclaration) {
                routeSymbols.add(KspRouteSymbol(type))
            } else {
                logger.warn("$type is not class declaration")
            }
        }
        logger.warn("Count: ${routeSymbols.size}")
        return emptyList()
    }

    override fun finish() {
        generateCode(routeSymbols)
        super.finish()
    }

    private fun generateCode(annotations: List<KspRouteSymbol>) {
        thread {
            val filename = tableName
            codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = tablePackage,
                fileName = filename,
                extensionName = "kt"
            ).writer().use { writer ->
                val tableType = HashMap::class.asClassName().parameterizedBy(
                    stringType.toTypeName(),
                    CompassMeta::class.asTypeName()
                )
                logger.warn("hello: $annotations")
                FileSpec.builder(tablePackage, filename)
                    .addType(
                        TypeSpec.objectBuilder(filename)
                            .addSuperinterface(ICompassTable::class)
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
        // val map = HashMap<String, CompassMeta>()
        addStatement("val map = %T()", tableType)

        for (symbol in symbols) {
            when (val targetType = getTargetType(symbol)) {
                TargetType.ECHO -> {
                    logger.warn("runnable")
                    // map[name] = CompassRunnable(name, Target::class.java, TargetType.RUNNABLE
                    addStatement(
                        "map[%S] = %T(%S, %T::class.java, %T.%L)",
                        symbol.route.name,
                        CompassEcho::class.java,
                        symbol.route.name,
                        symbol.target,
                        targetType::class.java, targetType.name
                    )
                }
                else -> {
                    logger.warn("page")
                    // map[name] = CompassRunnable(name, Target::class.java, TargetType,
                    addStatement(
                        "map[%S] = %T(%S, %T::class.java, %T.%L, %S, %L)",
                        symbol.route.name,
                        CompassPage::class.java,
                        symbol.route.name,
                        symbol.target,
                        targetType::class.java, targetType.name,
                        symbol.route.scheme,
                        symbol.route.requestCode
                    )
                }
            }
        }
        addCode("return map")
        return this
    }

    private fun getTargetType(symbol: KspRouteSymbol): TargetType {
        return when {
            activityType.isAssignableFrom(symbol.targetKsType) -> TargetType.ACTIVITY
            fragmentType.isAssignableFrom(symbol.targetKsType) -> TargetType.FRAGMENT
            blockType.isAssignableFrom(symbol.targetKsType) -> TargetType.ECHO
            else -> TargetType.UNKNOWN
        }
    }
}
