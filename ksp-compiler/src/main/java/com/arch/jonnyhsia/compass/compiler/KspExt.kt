package com.arch.jonnyhsia.compass.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName


internal fun KSClassDeclaration.asType() = asType(emptyList())

@KspExperimental
internal fun KSClassDeclaration.isKotlinClass(): Boolean {
    return origin == Origin.KOTLIN ||
            origin == Origin.KOTLIN_LIB ||
            isAnnotationPresent(Metadata::class)
}

@KspExperimental
internal inline fun <reified T : Annotation> KSAnnotated.findAnnotationWithType(): T? {
    return getAnnotationsByType(T::class).firstOrNull()
}

internal fun KSType.unwrapTypeAlias(): KSType {
    return if (this.declaration is KSTypeAlias) {
        (this.declaration as KSTypeAlias).type.resolve()
    } else {
        this
    }
}

internal fun KSAnnotation.toAnnotationSpec(resolver: Resolver): AnnotationSpec {
    val element = annotationType.resolve().unwrapTypeAlias().declaration as KSClassDeclaration
    val builder = AnnotationSpec.builder(element.toClassName())
    for (argument in arguments) {
        val member = CodeBlock.builder()
        val name = argument.name!!.getShortName()
        member.add("%L = ", name)
        addValueToBlock(argument.value!!, resolver, member)
        builder.addMember(member.build())
    }
    return builder.build()
}

private fun addValueToBlock(value: Any, resolver: Resolver, member: CodeBlock.Builder) {
    when (value) {
        is List<*> -> {
            // Array type
            member.add("arrayOf(⇥⇥")
            value.forEachIndexed { index, innerValue ->
                if (index > 0) member.add(", ")
                addValueToBlock(innerValue!!, resolver, member)
            }
            member.add("⇤⇤)")
        }
        is KSType -> {
            val unwrapped = value.unwrapTypeAlias()
            val isEnum =
                (unwrapped.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_ENTRY
            if (isEnum) {
                val parent = unwrapped.declaration.parentDeclaration as KSClassDeclaration
                val entry = unwrapped.declaration.simpleName.getShortName()
                member.add("%T.%L", parent.toClassName(), entry)
            } else {
                member.add("%T::class", unwrapped.toClassName())
            }
        }
        is KSName ->
            member.add(
                "%T.%L", ClassName.bestGuess(value.getQualifier()),
                value.getShortName()
            )
        is KSAnnotation -> member.add("%L", value.toAnnotationSpec(resolver))
        else -> member.add(memberForValue(value))
    }
}

/**
 * Creates a [CodeBlock] with parameter `format` depending on the given `value` object.
 * Handles a number of special cases, such as appending "f" to `Float` values, and uses
 * `%L` for other types.
 */
internal fun memberForValue(value: Any) = when (value) {
    is Class<*> -> CodeBlock.of("%T::class", value)
    is Enum<*> -> CodeBlock.of("%T.%L", value.javaClass, value.name)
    is String -> CodeBlock.of("%S", value)
    is Float -> CodeBlock.of("%Lf", value)
    is Double -> CodeBlock.of("%L", value)
    is Char -> CodeBlock.of("$value.toChar()")
    is Byte -> CodeBlock.of("$value.toByte()")
    is Short -> CodeBlock.of("$value.toShort()")
    // Int or Boolean
    else -> CodeBlock.of("%L", value)
}

internal inline fun KSPLogger.check(condition: Boolean, message: () -> String) {
    check(condition, null, message)
}

internal inline fun KSPLogger.check(condition: Boolean, element: KSNode?, message: () -> String) {
    if (!condition) {
        error(message(), element)
    }
}