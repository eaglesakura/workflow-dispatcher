package com.eaglesakura.workflowdispatcher

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Kotlin Annotation Processor
 */
internal class WorkflowProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val buildTargets = listOf(
            roundEnv.getElementsAnnotatedWith(OnDialogResultFlow::class.java)
                .map { (it as ExecutableElement).enclosingElement as TypeElement },
            roundEnv.getElementsAnnotatedWith(OnActivityResultFlow::class.java)
                .map { (it as ExecutableElement).enclosingElement as TypeElement },
            roundEnv.getElementsAnnotatedWith(OnRuntimePermissionResultFlow::class.java)
                .map { (it as ExecutableElement).enclosingElement as TypeElement }
        ).flatten().toSet().toList()

        buildTargets.forEach { ownerElement ->
            println("WorkflowProcessor: Generate ${ownerElement.simpleName} extensions")
            WorkflowFileGenerator(
                processor = this,
                elementUtils = processingEnv.elementUtils!!,
                processingEnv = processingEnv,
                roundEnv = roundEnv,
                workflowOwner = ownerElement
            ).generate()
        }
        return true
    }

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_7

    override fun getSupportedAnnotationTypes(): Set<String> {
        return listOf(
            OnActivityResultFlow::class,
            OnDialogResultFlow::class,
            OnRuntimePermissionResultFlow::class
        ).map { it.qualifiedName!! }.toSet()
    }
}
