package com.arch.jonnyhsia.compass.compiler

import com.arch.jonnyhsia.compass.facade.CompassEcho
import com.arch.jonnyhsia.compass.facade.CompassMeta
import com.arch.jonnyhsia.compass.facade.CompassPage
import com.arch.jonnyhsia.compass.facade.ICompassTable
import com.arch.jonnyhsia.compass.facade.annotation.Route
import com.arch.jonnyhsia.compass.facade.enums.TargetType
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName

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

//    private val routeSymbols = ArrayList<KspRouteSymbol>()

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
        val symbols = resolver.getSymbolsWithAnnotation(ROUTE_NAME)
        // val ret = symbols.filter { !it.validate() }.toList()

        stringType = resolver.builtIns.stringType
        blockType =
            resolver.getClassDeclarationByName("com.arch.jonnyhsia.compass.facade.IRouteEcho")!!
                .asType()
        activityType = resolver.getClassDeclarationByName("android.app.Activity")!!.asType()
        fragmentType =
            resolver.getClassDeclarationByName("androidx.fragment.app.Fragment")!!.asType()

        logger.warn("Start processing...")
        logger.warn("getSymbolsWithAnnotation: $ROUTE_NAME")

        val list = symbols
            .onEach {
                logger.warn("onEach: ${it.containingFile?.fileName}")
            }
            .filterIsInstance<KSClassDeclaration>()
            .map { declaration ->
                KspRouteSymbol(declaration)
            }
            .toList()
        generateCode(list)

        logger.warn("Count: ${list.size}")
        return emptyList()
    }

    override fun finish() {
        super.finish()
    }

    private fun generateCode(annotations: List<KspRouteSymbol>) {
        if (annotations.isEmpty()) {
            logger.warn("No annotations found")
            return
        }

        val ksFiles = ArrayList<KSFile>(annotations.size)
        val filename = tableName
        val tableType = HashMap::class.asClassName().parameterizedBy(
            stringType.toTypeName(),
            CompassMeta::class.asTypeName()
        )
        logger.warn("hello: $annotations")
        val fileSpec = FileSpec.builder(tablePackage, filename)
            .addType(
                TypeSpec.objectBuilder(filename)
                    .addSuperinterface(ICompassTable::class)
                    .addFunction(
                        FunSpec.builder("getPages")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(tableType)
                            .collectAllRoutesAndReturn(annotations, tableType, ksFiles)
                            .build()
                    )
                    .build()
            )
            .build()

        codeGenerator.createNewFile(
            dependencies = Dependencies(true, *ksFiles.toTypedArray()),
            packageName = tablePackage,
            fileName = filename,
            extensionName = "kt"
        ).writer().use { writer ->
            fileSpec.writeTo(writer)
        }
    }

    private fun FunSpec.Builder.collectAllRoutesAndReturn(
        symbols: List<KspRouteSymbol>,
        tableType: ParameterizedTypeName,
        ksFiles: ArrayList<KSFile>
    ): FunSpec.Builder {
        addComment("Read routes(${symbols.size}) from annotations and generate router map")
        // val map = HashMap<String, CompassMeta>()
        addStatement("val map = %T()", tableType)

        for (symbol in symbols) {
            symbol.symbol.containingFile?.let {
                ksFiles.add(it)
            }
            when (val targetType = getTargetType(symbol)) {
                TargetType.ECHO -> {
                    logger.warn("runnable")
                    // map[name] = CompassEcho(name, target, type, extras)
                    addStatement(
                        "map[%S] = %T(%S, %T::class.java, %L, %L, %S)",
                        symbol.route.name,
                        CompassEcho::class.java,
                        symbol.route.name,
                        symbol.target,
                        targetType,
                        symbol.route.extras,
                        symbol.route.group
                    )
                }

                else -> {
                    logger.warn("page $symbol")
                    // map[name] = CompassPage(name, Target::class.java, TargetType, extras)
                    addStatement(
                        "map[%S] = %T(%S, %T::class.java, %L, %L, %S)",
                        symbol.route.name,
                        CompassPage::class.java,
                        symbol.route.name,
                        symbol.target,
                        targetType,
                        symbol.route.extras,
                        symbol.route.group
                    )
                }
            }
        }
        addCode("return map")
        return this
    }

    private fun getTargetType(symbol: KspRouteSymbol): Int {
        return when {
            activityType.isAssignableFrom(symbol.targetKsType) -> TargetType.ACTIVITY
            fragmentType.isAssignableFrom(symbol.targetKsType) -> TargetType.FRAGMENT
            blockType.isAssignableFrom(symbol.targetKsType) -> TargetType.ECHO
            else -> TargetType.UNKNOWN
        }
    }
}
