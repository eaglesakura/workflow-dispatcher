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

    @Suppress("FunctionName")
    private fun process_v1_1(annotations: Set<TypeElement>, roundEnv: RoundEnvironment) {
        listOf(
            roundEnv.getElementsAnnotatedWith(OnDialogResultFlow::class.java)
                .map { it as ExecutableElement },
        ).flatten().forEach { executableElement ->
            WorkflowFileGenerator2(
                processor = this,
                elementUtils = processingEnv.elementUtils!!,
                processingEnv = processingEnv,
                roundEnv = roundEnv,
                executableElement = executableElement,
            ).generateForDialog()
        }

        listOf(
            roundEnv.getElementsAnnotatedWith(OnActivityResultFlow::class.java)
                .map { it as ExecutableElement },
        ).flatten().forEach { executableElement ->
            WorkflowFileGenerator2(
                processor = this,
                elementUtils = processingEnv.elementUtils!!,
                processingEnv = processingEnv,
                roundEnv = roundEnv,
                executableElement = executableElement,
            ).generateForActivity()
        }

        listOf(
            roundEnv.getElementsAnnotatedWith(OnRuntimePermissionResultFlow::class.java)
                .map { it as ExecutableElement },
        ).flatten().forEach { executableElement ->
            WorkflowFileGenerator2(
                processor = this,
                elementUtils = processingEnv.elementUtils!!,
                processingEnv = processingEnv,
                roundEnv = roundEnv,
                executableElement = executableElement,
            ).generateForRuntimePermissions()
        }
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        process_v1_1(annotations, roundEnv)
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
