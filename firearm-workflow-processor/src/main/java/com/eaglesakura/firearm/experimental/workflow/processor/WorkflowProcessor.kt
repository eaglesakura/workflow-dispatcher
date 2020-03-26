package com.eaglesakura.firearm.experimental.workflow.processor

import com.eaglesakura.firearm.experimental.workflow.annotations.OnActivityResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnDialogResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnRuntimePermissionResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.WorkflowOwner
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * Kotlin Annotation Processor
 */
class WorkflowProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(WorkflowOwner::class.java).forEach { ownerElement ->
            //            generateExtension(roundEnv, ownerElement as TypeElement)
            WorkflowFileGenerator(
                    processor = this,
                    elementUtils = processingEnv.elementUtils!!,
                    processingEnv = processingEnv,
                    roundEnv = roundEnv,
                    workflowOwner = ownerElement as TypeElement
            ).generate()
        }
        return true
    }

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_7

    override fun getSupportedAnnotationTypes(): Set<String> {
        return listOf(
                WorkflowOwner::class,
                OnActivityResultFlow::class,
                OnDialogResultFlow::class,
                OnRuntimePermissionResultFlow::class
        ).map { it.qualifiedName!! }.toSet()
    }
}