package com.eaglesakura.firearm.experimental.workflow.processor

import com.squareup.kotlinpoet.ClassName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 *  Returns ClassName for kotlinpoet.
 */
val TypeElement.poetClassName: ClassName
    get() {
        val packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."))
        val className = simpleName.toString()
        return ClassName(packageName, className)
    }

/**
 * Returns method name from Element.
 */
val ExecutableElement.methodName: String
    get() {
        val index = simpleName.toString().lastIndexOf("$")
        return if (index < 0) {
            simpleName.toString()
        } else {
            simpleName.toString().substring(0, index)
        }
    }
