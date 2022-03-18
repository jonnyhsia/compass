package com.arch.jonnyhsia.compass.compiler

import com.arch.jonnyhsia.compass.api.CompassPage
import com.arch.jonnyhsia.compass.api.ICompassTable
import com.arch.jonnyhsia.compass.api.PageKey
import com.arch.jonnyhsia.compass.api.Route
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.concurrent.thread

@KspExperimental
class CompassKspProcessor(
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

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("Start processing...")
        val symbols = resolver.getSymbolsWithAnnotation(ROUTE_NAME)
        for (type in symbols) {
            logger.warn("aaa")
            if (type !is KSClassDeclaration) {
                continue
            }
            logger.warn("Detect symbol: $type, ${type::class.simpleName}")
            routeSymbols.add(KspRouteSymbol(type).also {
                logger.warn(it.toString())
            })
        }
        logger.warn("Symbols: ${routeSymbols.size}")
        generateCode(routeSymbols)
        return emptyList()
    }

    private fun generateCode(annotations: List<KspRouteSymbol>) {
        thread {
            val filename = tableName
            codeGenerator.createNewFile(
                dependencies = Dependencies.ALL_FILES,
                packageName = tablePackage,
                fileName = filename,
                extensionName = "kt"
            ).writer().use { writer ->
                val tableType = HashMap::class.asClassName().parameterizedBy(
                    PageKey::class.asTypeName(),
                    CompassPage::class.asTypeName()
                )
                logger.warn("hello")
                val spec = FileSpec.builder(tablePackage, filename)
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
//                for (annotation in annotations) {
//                    val name = annotation.name
//                    val scheme = annotation.scheme
//                    val interceptors = annotation.interceptors
//                    val requestCode = annotation.requestCode
//                }
            }
        }
    }

    override fun finish() {
        super.finish()
    }

    inner class RouteVisitor : KSVisitorVoid()

    private fun FunSpec.Builder.collectAllRoutesAndReturn(
        symbols: List<KspRouteSymbol>,
        tableType: ParameterizedTypeName
    ): FunSpec.Builder {
        logger.warn("hello222")
        addComment("Read routes(${symbols.size}) from annotations and generate router map")
        addStatement("val map = %T()", tableType)
        if (symbols.isEmpty()) {
            throw Exception()
        }

        for (symbol in symbols) {
//            val map = hashMapOf<PageKey, CompassPage>()
//            map[PageKey(symbol.route.scheme, symbol.route.name)] = CompassPage(
//                symbol.route.name,
//                Any::class.java,
//                symbol.route.requestCode
//            )
//            logger.warn("target: ${symbol.target}")
            val clz = Class.forName(symbol.target)
            logger.warn(clz.toString())
            addStatement(
                "map[%T(%S, %S)] = %T(%S, %T::class.java, %L)",
                PageKey::class.java,
                symbol.route.scheme,
                symbol.route.name,
                CompassPage::class.java, symbol.route.name,
                clz.asTypeName(),
                symbol.route.requestCode
            )
        }
        addCode("return map")
        return this
    }
}
