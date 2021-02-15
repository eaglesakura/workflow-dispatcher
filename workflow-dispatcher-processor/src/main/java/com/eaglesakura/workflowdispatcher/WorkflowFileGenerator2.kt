package com.eaglesakura.workflowdispatcher

import com.samskivert.mustache.Mustache
import java.io.Writer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.StandardLocation

internal class WorkflowFileGenerator2(
    @Suppress("unused") private val processor: WorkflowProcessor,
    @Suppress("unused") private val elementUtils: Elements,
    private val processingEnv: ProcessingEnvironment,
    @Suppress("unused") private val roundEnv: RoundEnvironment,
    private val executableElement: ExecutableElement,
) {
    /**
     * Annotation class name.
     */
    private val callbackReceiverClassName: String
        get() = (executableElement.enclosingElement as TypeElement).qualifiedName.toString()

    private val callbackReceiverMethodName: String
        get() = executableElement.methodName

    /**
     * Generated workflow execution package name.
     */
    private val workerFragmentPackageName: String
        get() = (executableElement.enclosingElement as TypeElement).poetClassName.packageName

    /**
     * Generated workflow execution fragment.
     */
    private val workerFragmentName: String
        get() {
            return buildString {
                append(executableElement.enclosingElement.simpleName)
                append("_")
                append(executableElement.methodName)
            }
        }

    private fun compileMustache(writer: Writer, templatePath: String, parameters: Any) {
        val template = javaClass.classLoader.getResourceAsStream(templatePath).use { stream ->
            checkNotNull(stream) {
                "not found $templatePath"
            }
            stream.readBytes().toString(Charsets.UTF_8)
        }

        val mustache = Mustache.compiler().compile(template)
        writer.write(
            mustache.execute(parameters)
                .replace("java.lang.String", "String")
        )
    }

    fun generateForActivity() {
        println("Generate $workerFragmentName.kt")
        val annotation = executableElement.getAnnotation(OnActivityResultFlow::class.java)!!
        processingEnv.filer!!.createResource(
            StandardLocation.SOURCE_OUTPUT,
            workerFragmentPackageName,
            "$workerFragmentName.kt"
        ).openWriter()!!.use { writer ->
            compileMustache(
                writer,
                "templates/activity_dispatcher_fragment.mustache",
                WorkflowFragmentParameters(
                    packageName = workerFragmentPackageName,
                    className = workerFragmentName,
                    callbackClassName = callbackReceiverClassName,
                    callbackMethodName = callbackReceiverMethodName,
                    savedStateList = WorkflowFragmentParameters.parseSavedState(
                        executableElement,
                        defaultArguments = 1
                    ),
                    entryPointFunctionName = annotation.entryPointName,
                )
            )
        }
    }

    fun generateForDialog() {
        println("Generate $workerFragmentName.kt")
        val annotation = executableElement.getAnnotation(OnDialogResultFlow::class.java)!!
        processingEnv.filer!!.createResource(
            StandardLocation.SOURCE_OUTPUT,
            workerFragmentPackageName,
            "$workerFragmentName.kt"
        ).openWriter()!!.use { writer ->
            compileMustache(
                writer,
                "templates/dialog_dispatcher_fragment.mustache",
                WorkflowFragmentParameters(
                    packageName = workerFragmentPackageName,
                    className = workerFragmentName,
                    callbackClassName = callbackReceiverClassName,
                    callbackMethodName = callbackReceiverMethodName,
                    savedStateList = WorkflowFragmentParameters.parseSavedState(
                        executableElement,
                        defaultArguments = 1
                    ),
                    entryPointFunctionName = annotation.entryPointName,
                )
            )
        }
    }

    fun generateForRuntimePermissions() {
        println("Generate $workerFragmentName.kt")
        val annotation =
            executableElement.getAnnotation(OnRuntimePermissionResultFlow::class.java)!!
        processingEnv.filer!!.createResource(
            StandardLocation.SOURCE_OUTPUT,
            workerFragmentPackageName,
            "$workerFragmentName.kt"
        ).openWriter()!!.use { writer ->
            compileMustache(
                writer,
                "templates/permissions_dispatcher_fragment.mustache",
                WorkflowFragmentParameters(
                    packageName = workerFragmentPackageName,
                    className = workerFragmentName,
                    callbackClassName = callbackReceiverClassName,
                    callbackMethodName = callbackReceiverMethodName,
                    savedStateList = WorkflowFragmentParameters.parseSavedState(
                        executableElement,
                        defaultArguments = 1
                    ),
                    entryPointFunctionName = annotation.entryPointName,
                    permissions = annotation.permissions.toList(),
                )
            )
        }
    }
}
