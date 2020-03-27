package com.eaglesakura.firearm.experimental.workflow.processor

import com.eaglesakura.firearm.experimental.workflow.annotations.OnActivityResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnDialogResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnRuntimePermissionResultFlow
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.tools.StandardLocation

internal class WorkflowFileGenerator(
    private val processor: WorkflowProcessor,
    private val elementUtils: Elements,
    private val processingEnv: ProcessingEnvironment,
    private val roundEnv: RoundEnvironment,
    private val workflowOwner: TypeElement
) {
    @Suppress("PrivatePropertyName")
    private val CLASS_WORKFLOW_OWNER: ClassName = workflowOwner.poetClassName

    @Suppress("PrivatePropertyName")
    private val CLASS_WORKFLOW_REGISTRY: ParameterizedTypeName =
            ClassName(BASE_PACKAGE_NAME, "WorkflowRegistry")
                    .parameterizedBy(workflowOwner.poetClassName)

    @Suppress("PrivatePropertyName")
    private val CLASS_ACTIVITY_RESULT_ACTION: ParameterizedTypeName = ClassName(
            "$BASE_PACKAGE_NAME.activity",
            "ActivityResultAction"
    ).parameterizedBy(workflowOwner.poetClassName)

    @Suppress("PrivatePropertyName")
    private val CLASS_DIALOG_ACTION: ParameterizedTypeName = ClassName(
            "$BASE_PACKAGE_NAME.dialog",
            "DialogAction"
    ).parameterizedBy(workflowOwner.poetClassName)

    @Suppress("PrivatePropertyName")
    private val CLASS_RUNTIME_PERMISSION_ACTION: ParameterizedTypeName
        get() = ClassName(
                "$BASE_PACKAGE_NAME.permission",
                "RuntimePermissionAction"
        ).parameterizedBy(workflowOwner.poetClassName)

    private val fileName = "${CLASS_WORKFLOW_OWNER.simpleName}GeneratedExtensions.kt"

    private val fileSpec: FileSpec.Builder =
            FileSpec.builder(CLASS_WORKFLOW_OWNER.packageName, CLASS_WORKFLOW_OWNER.simpleName)

    private val propertyWorkflowRegistry =
            PropertySpec.builder("registry", CLASS_WORKFLOW_REGISTRY)
                    .initializer("WorkflowRegistry.of(${CLASS_WORKFLOW_OWNER.simpleName}::class)")
                    .addModifiers(KModifier.PRIVATE)
                    .build()

    private val funcValidateFlow =
            FunSpec.builder("validateFlow")
                    .addModifiers(KModifier.PRIVATE)
                    .addStatement(
                            """
                        check(initialized) {
                            "You should call '${CLASS_WORKFLOW_OWNER.simpleName}.loadWorkflowModules()'" +
                            "\n class ${CLASS_WORKFLOW_OWNER.simpleName} {" +
                            "\n   init {" +
                            "\n       loadWorkflowModules()" +
                            "\n   }" +
                            "\n }"
                        }
                    """.trimIndent()
                    )
                    .build()

    fun generate() {
        generateInitializer()

        // check all methods
        listOf(
                // find methods
                elementUtils.getAllMembers(workflowOwner)
        ).flatten().filterIsInstance<ExecutableElement>().forEach { memberElement ->
            memberElement.getAnnotationsByType(OnDialogResultFlow::class.java)
                    ?.takeIf { it.isNotEmpty() }
                    ?.firstOrNull()
                    ?.also { generateExtension(memberElement, it) }
            memberElement.getAnnotationsByType(OnActivityResultFlow::class.java)
                    ?.takeIf { it.isNotEmpty() }
                    ?.firstOrNull()
                    ?.also { generateExtension(memberElement, it) }
            memberElement.getAnnotationsByType(OnRuntimePermissionResultFlow::class.java)
                    ?.takeIf { it.isNotEmpty() }
                    ?.firstOrNull()
                    ?.also { generateExtension(memberElement, it) }
        }

        // add `fun loadWorkflowModules()`
        fileSpec.addFunction(
                FunSpec.builder("loadWorkflowModules")
                        .addModifiers(KModifier.INTERNAL)
                        .receiver(workflowOwner.poetClassName)
                        .addStatement("$BASE_PACKAGE_NAME.internal.supportWorkflowOwner(this /* Type Validation */ )")
                        .addStatement("initialized = true")
                        .build()
        )

        processingEnv.filer!!.createResource(
                StandardLocation.SOURCE_OUTPUT,
                workflowOwner.poetClassName.packageName,
                fileName
        ).openWriter().use { writer ->
            writer.write(fileSpec.build().toString().let {
                var result = it
                result = result.replace("import java.lang.String", "")
                result = result.replace("import kotlin.String", "")
                result
            })
        }
    }

    private fun generateInitializer() {
        // add `val registry: WorkflowRegistry<T>`
        fileSpec.addProperty(propertyWorkflowRegistry)

        // add `val initialized: Boolean`
        fileSpec.addProperty(
                PropertySpec.builder("initialized", Boolean::class.java)
                        .mutable(true)
                        .initializer("false")
                        .addModifiers(KModifier.PRIVATE)
                        .build()
        )

        // add assertion.
        fileSpec.addFunction(funcValidateFlow)
    }

    /**
     * @param flowResultMethod Flow result method
     * @param entryPointClass Entry-Point type, e.g.) [CLASS_DIALOG_ACTION, CLASS_ACTIVITY_RESULT_ACTION, CLASS_RUNTIME_PERMISSION_ACTION]
     * @param propertyGeneratorName Entry-Point generator, e.g.["dialogAction", "activityResultAction", "runtimePermissionAction"]
     * @param defaultResultArguments Flow result default arguments, e.g.) ["result", "data"]
     */
    private fun generateEntryPointProperty(
        flowResultMethod: ExecutableElement,
        entryPointClass: TypeName,
        propertyGeneratorName: String,
        defaultResultArguments: List<ParameterSpec>
    ): PropertySpec {
        // Generate Entry-Point Variable.
        val entryPointName = "${propertyGeneratorName}_${flowResultMethod.methodName}"
        val savedFlowParameters =
                parseSavedFlowStateArguments(flowResultMethod, defaultResultArguments)

        return PropertySpec.builder(
                entryPointName,
                entryPointClass
        ).initializer(buildString {
            append("${propertyWorkflowRegistry.name}.$propertyGeneratorName(\"$entryPointName\") { sender")
            defaultResultArguments.forEach { arg ->
                append(", ${arg.name}")
            }
            append(", savedFlowState ->")
            append("\n")

            append("sender.${flowResultMethod.methodName}(")
            defaultResultArguments.forEachIndexed { index, arg ->
                if (index > 0) {
                    append(", ")
                }
                append(arg.name)
            }

            savedFlowParameters.forEach { arg ->
                append(", $BASE_PACKAGE_NAME.internal.parseSavedStateBundle(")
                append("savedFlowState")
                append(", \"${arg.name}\"")
                append(", ${arg.type.isNullable}")
                append(")")
            }

            append(")")

            append("\n}")
        })
                .addModifiers(KModifier.PRIVATE)
                .build()
    }

    /**
     * @param flowResultMethod Flow result method
     * @param entryPointName Entry-Point name, value from [@OnDialogResultFlow.entryPointName, @OnActivityResultFlow.entryPointName...]
     * @param entryPointArguments Entry-Point lambda arguments.
     * @param resultCallbackArguments Flow result default arguments, e.g.) ["result: ActivityResult"]
     */
    private fun generateEntryPointFunction(
        flowResultMethod: ExecutableElement,
        entryPointName: String,
        entryPointArguments: List<ParameterSpec>,
        entryPointImpl: PropertySpec,
        resultCallbackArguments: List<ParameterSpec>
    ): FunSpec {
        val flowStateArguments =
                parseSavedFlowStateArguments(flowResultMethod, resultCallbackArguments)

        // Generate Entry-Point function.
        return FunSpec.builder(entryPointName).apply {
            addModifiers(KModifier.INTERNAL)
            receiver(workflowOwner.poetClassName)
            addStatement("validateFlow()")

            val body = StringBuilder()
            body.append("${entryPointImpl.name}(this")

            entryPointArguments.forEach { arg ->
                addParameter(arg)
                body.append(", ${arg.name}")
            }

            if (flowStateArguments.isNotEmpty()) {
                body.append(", androidx.core.os.bundleOf(")
                flowStateArguments.forEachIndexed { index, arg ->
                    addParameter(arg)
                    if (index > 0) {
                        body.append(", ")
                    }
                    body.append("\"${arg.name}\" to ${arg.name}")
                }
                body.append(")")
            }
            body.append(")")
            addStatement(body.toString())
            returns(Unit::class.java)
        }.build()
    }

    private fun parseSavedFlowStateArguments(
        flowResultMethod: ExecutableElement,
        requireArguments: List<ParameterSpec>
    ): List<ParameterSpec> {
        val callbackParams = flowResultMethod.parameters
        val flowStateArguments: List<VariableElement> =
                if (callbackParams.size > requireArguments.size) {
                    callbackParams.subList(requireArguments.size, callbackParams.size).also {
                        println("callback[$callbackParams], required[$requireArguments], state[$it]")
                        check(it.isNotEmpty())
                    }
                } else {
                    emptyList()
                }

        return mutableListOf<ParameterSpec>().also { result ->
            flowStateArguments.forEach { arg ->
                val nullable =
                        arg.getAnnotation(org.jetbrains.annotations.Nullable::class.java) != null
                val typeName = arg.asType().asTypeName().copy(nullable = nullable)
                println("${flowResultMethod.methodName} : ${arg.simpleName}")
                result.add(ParameterSpec.builder(arg.simpleName.toString(), typeName).build())
            }
        }
    }

    /**
     * Generate DialogFlowAction function.
     */
    private fun generateExtension(
        member: ExecutableElement,
        flowAnnotation: OnDialogResultFlow
    ) {
        val entryPointArguments: List<ParameterSpec> = mutableListOf(
                ParameterSpec.builder("factory", CLASS_PARCELABLE_DIALOG_FACTORY).build()
        )
        val resultCallbackArguments: List<ParameterSpec> = listOf(
                ParameterSpec.builder("result", CLASS_DIALOG_RESULT).build()
        )

        // Generate Entry-Point impl
        val entryPointImpl = generateEntryPointProperty(
                member,
                CLASS_DIALOG_ACTION,
                "dialogAction",
                resultCallbackArguments
        )

        // Generate Entry-Point function.
        val extensionFunc = generateEntryPointFunction(
                member,
                flowAnnotation.entryPointName,
                entryPointArguments,
                entryPointImpl,
                resultCallbackArguments
        )

        fileSpec.addProperty(entryPointImpl)
        fileSpec.addFunction(extensionFunc)
    }

    /**
     * Generate ActivityResultAction function.
     */
    private fun generateExtension(
        member: ExecutableElement,
        flowAnnotation: OnActivityResultFlow
    ) {
        val entryPointArguments: List<ParameterSpec> = mutableListOf(
                ParameterSpec.builder("intent", CLASS_INTENT).build(),
                ParameterSpec.builder("options", CLASS_BUNDLE.copy(nullable = true))
                        .defaultValue("null")
                        .build()
        )
        val resultCallbackArguments: List<ParameterSpec> = listOf(
                ParameterSpec.builder("result", CLASS_ACTIVITY_RESULT).build()
        )

        // Generate Entry-Point impl
        val entryPointImpl = generateEntryPointProperty(
                member,
                CLASS_ACTIVITY_RESULT_ACTION,
                "activityResultAction",
                resultCallbackArguments
        )

        // Generate Entry-Point function.
        val extensionFunc = generateEntryPointFunction(
                member,
                flowAnnotation.entryPointName,
                entryPointArguments,
                entryPointImpl,
                resultCallbackArguments
        )

        fileSpec.addProperty(entryPointImpl)
        fileSpec.addFunction(extensionFunc)
    }

    /**
     * Generate ActivityResultAction function.
     */
    private fun generateExtension(
        member: ExecutableElement,
        flowAnnotation: OnRuntimePermissionResultFlow
    ) {
        val entryPointArguments: List<ParameterSpec> = mutableListOf(
                ParameterSpec.builder("permissions", CLASS_STRING_LIST)
                        .defaultValue(buildString {
                            append("listOf(")
                            flowAnnotation.permissions.forEachIndexed { index, permission ->
                                if (index > 0) {
                                    append(", ")
                                }
                                append("\"$permission\"")
                            }
                            append(")")
                        })
                        .build()
        )
        val resultCallbackArguments: List<ParameterSpec> = listOf(
                ParameterSpec.builder("result", CLASS_RUNTIME_PERMISSION_RESULT).build()
        )

        // Generate Entry-Point impl
        val entryPointImpl = generateEntryPointProperty(
                member,
                CLASS_RUNTIME_PERMISSION_ACTION,
                "requestPermissionsAction",
                resultCallbackArguments
        )

        // Generate Entry-Point function.
        val extensionFunc = generateEntryPointFunction(
                member,
                flowAnnotation.entryPointName,
                entryPointArguments,
                entryPointImpl,
                resultCallbackArguments
        )

        fileSpec.addProperty(entryPointImpl)
        fileSpec.addFunction(extensionFunc)
    }

    companion object {
        private val BASE_PACKAGE_NAME = "com.eaglesakura.firearm.experimental.workflow"

        private val CLASS_PARCELABLE_DIALOG_FACTORY =
                ClassName(
                        "$BASE_PACKAGE_NAME.dialog",
                        "ParcelableDialogFactory"
                )

        private val CLASS_DIALOG_RESULT =
                ClassName(
                        "$BASE_PACKAGE_NAME.dialog",
                        "DialogResult"
                )

        private val CLASS_ACTIVITY_RESULT =
                ClassName(
                        "$BASE_PACKAGE_NAME.activity",
                        "ActivityResult"
                )

        private val CLASS_RUNTIME_PERMISSION_RESULT =
                ClassName(
                        "$BASE_PACKAGE_NAME.permission",
                        "RuntimePermissionResult"
                )

        private val CLASS_BUNDLE =
                ClassName("android.os", "Bundle")

        private val CLASS_INTENT =
                ClassName("android.content", "Intent")

        private val CLASS_STRING_LIST: ParameterizedTypeName
            get() = ClassName("kotlin.collections", "List")
                    .parameterizedBy(STRING)
    }
}