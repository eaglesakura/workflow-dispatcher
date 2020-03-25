package com.eaglesakura.firearm.experimental.workflow.processor

import com.squareup.kotlinpoet.ClassName
import javax.lang.model.element.TypeElement

val TypeElement.className: ClassName
    get() {
        val packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."))
        val className = simpleName.toString()
        return ClassName(packageName, className)
    }