package com.arch.jonnyhsia.compass.compiler

import com.arch.jonnyhsia.compass.api.Route
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
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
        logger.info("process")
        val annotations = ArrayList<Route>()
        val symbols = resolver.getSymbolsWithAnnotation(ROUTE_NAME)
        for (type in symbols) {
            if (type !is KSDeclaration) {
                continue
            }

            val annotation = type.findAnnotationWithType<Route>() ?: continue
            annotations.add(annotation)
        }
        return emptyList()
    }

    private fun generateCode(annotations: List<Route>) {
        thread {
            val filename = "$tableName${count++}"
            codeGenerator.createNewFile(
                dependencies = Dependencies.ALL_FILES,
                packageName = tablePackage,
                fileName = filename,
                extensionName = "kt"
            ).writer().use { writer ->
                val spec = FileSpec.builder(tablePackage, filename)
                    .addType(
                        TypeSpec.classBuilder(filename)
                            .primaryConstructor(
                                FunSpec.constructorBuilder()
                                    .build()
                            )
//                        .addProperty(
//                            PropertySpec.builder(
//                                name = "pages",
//                                type = Map::class.asClassName().parameterizedBy(
//                                    String::class.asTypeName(),
//                                    CompassPage::class.asTypeName()
//                                )
//                            ).build()
//                        )
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
        generateCode(emptyList())
    }


    inner class RouteVisitor : KSVisitorVoid()
}