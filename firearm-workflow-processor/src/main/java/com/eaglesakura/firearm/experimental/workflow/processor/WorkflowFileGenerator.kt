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
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.tools.StandardLocation

class WorkflowFileGenerator(
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
        ClassName("com.eaglesakura.firearm.experimental.workflow", "WorkflowRegistry")
            .parameterizedBy(workflowOwner.poetClassName)

    @Suppress("PrivatePropertyName")
    private val CLASS_ACTIVITY_RESULT_ACTION: ParameterizedTypeName = ClassName(
        "com.eaglesakura.firearm.experimental.workflow.activity",
        "ActivityResultAction"
    ).parameterizedBy(workflowOwner.poetClassName)

    @Suppress("PrivatePropertyName")
    private val CLASS_DIALOG_ACTION: ParameterizedTypeName = ClassName(
        "com.eaglesakura.firearm.experimental.workflow.dialog",
        "DialogAction"
    ).parameterizedBy(workflowOwner.poetClassName)

    @Suppress("PrivatePropertyName")
    private val CLASS_RUNTIME_PERMISSION_ACTION: ParameterizedTypeName
        get() = ClassName(
            "com.eaglesakura.firearm.experimental.workflow.permission",
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

        elementUtils.getAllMembers(workflowOwner).forEach { memberElement ->
            val onDialogResultFlow =
                memberElement.getAnnotationsByType(OnDialogResultFlow::class.java)
                    ?.takeIf { it.isNotEmpty() }
                    ?.get(0)

            val onActivityResultFlow =
                memberElement.getAnnotationsByType(OnActivityResultFlow::class.java)
                    ?.takeIf { it.isNotEmpty() }
                    ?.get(0)

            val onRuntimePermissionResultFlow =
                memberElement.getAnnotationsByType(OnRuntimePermissionResultFlow::class.java)
                    ?.takeIf { it.isNotEmpty() }
                    ?.get(0)

            when {
                onDialogResultFlow != null -> {
                    generateExtension(
                        memberElement as ExecutableElement,
                        onDialogResultFlow
                    )
                }
                onActivityResultFlow != null -> {
                    generateExtension(
                        memberElement as ExecutableElement,
                        onActivityResultFlow
                    )
                }
                onRuntimePermissionResultFlow != null -> {
                    generateExtension(
                        memberElement as ExecutableElement,
                        onRuntimePermissionResultFlow
                    )
                }
            }
        }

        // add `fun loadWorkflowModules()`
        fileSpec.addFunction(
            FunSpec.builder("loadWorkflowModules")
                .addModifiers(KModifier.INTERNAL)
                .receiver(workflowOwner.poetClassName)
                .addStatement("initialized = true")
                .build()
        )

        processingEnv.filer!!.createResource(
            StandardLocation.SOURCE_OUTPUT,
            workflowOwner.poetClassName.packageName,
            fileName
        ).openWriter().use { writer ->
            writer.write(fileSpec.build().toString())
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
     * Generate DialogFlowAction function.
     */
    private fun generateExtension(
        member: ExecutableElement,
        flowAnnotation: OnDialogResultFlow
    ) {
        val requireArguments = 1
        val flowStateArguments: List<VariableElement> =
            if (member.parameters.size > requireArguments) {
                member.parameters.subList(0, member.parameters.lastIndex)
            } else {
                emptyList()
            }

        // Generate Entry-Point Variable.
        val entryPointName = "${member.methodName}DialogEntry"
        val workflowEntry = PropertySpec.builder(
            entryPointName,
            CLASS_DIALOG_ACTION
        ).initializer(
            buildString {
                append("""${propertyWorkflowRegistry.name}.dialogAction("$entryPointName") { sender, result, savedFlowState ->""")
                append("\n")
                if (flowStateArguments.isEmpty()) {
                    append("sender.${member.methodName}(result)")
                } else {
                    append("sender.${member.methodName}(result, savedFlowState)")
                }
                append("\n")
                append("}")
            }
        ).addModifiers(KModifier.PRIVATE).build()

        // Generate Entry-Point function.
        val extensionFunc = FunSpec.builder(flowAnnotation.entryPointName).apply {
            addModifiers(KModifier.INTERNAL)
            receiver(workflowOwner.poetClassName)
            addStatement("validateFlow()")

            addParameter("factory", CLASS_PARCELABLE_DIALOG_FACTORY)
            if (flowStateArguments.isEmpty()) {
                addStatement("${workflowEntry.name}(this, factory)")
            } else {
                addParameter("flowState", CLASS_BUNDLE)
                addStatement("${workflowEntry.name}(this, factory, flowState)")
            }
            returns(Unit::class.java)
        }.build()

        fileSpec.addProperty(workflowEntry)
        fileSpec.addFunction(extensionFunc)
    }

    /**
     * Generate ActivityResultAction function.
     */
    private fun generateExtension(
        member: ExecutableElement,
        flowAnnotation: OnActivityResultFlow
    ) {
        val requireArguments = 2
        val flowStateArguments: List<VariableElement> =
            if (member.parameters.size > requireArguments) {
                member.parameters.subList(0, member.parameters.lastIndex)
            } else {
                emptyList()
            }

        // Generate Entry-Point Variable.
        val entryPointName = "${member.methodName}ActivityResultEntry"
        val workflowEntry = PropertySpec.builder(
            entryPointName,
            CLASS_ACTIVITY_RESULT_ACTION
        ).initializer(
            buildString {
                append("""${propertyWorkflowRegistry.name}.activityResultAction("$entryPointName") { sender, result, data, savedFlowState ->""")
                append("\n")
                if (flowStateArguments.isEmpty()) {
                    append("sender.${member.methodName}(result, data)")
                } else {
                    append("sender.${member.methodName}(result, data, savedFlowState)")
                }
                append("\n")
                append("}")
            }
        ).addModifiers(KModifier.PRIVATE).build()

        // Generate Entry-Point function.
        val extensionFunc = FunSpec.builder(flowAnnotation.entryPointName).apply {
            addModifiers(KModifier.INTERNAL)
            receiver(workflowOwner.poetClassName)
            addStatement("validateFlow()")

            addParameter("intent", CLASS_INTENT)
            addParameter(
                ParameterSpec.builder("options", CLASS_BUNDLE.copy(nullable = true))
                    .defaultValue("null")
                    .build()
            )
            if (flowStateArguments.isEmpty()) {
                addStatement("${workflowEntry.name}(this, intent, options)")
            } else {
                addParameter("flowState", CLASS_BUNDLE)
                addStatement("${workflowEntry.name}(this, intent, options, flowState)")
            }
            returns(Unit::class.java)
        }.build()

        fileSpec.addProperty(workflowEntry)
        fileSpec.addFunction(extensionFunc)
    }

    /**
     * Generate ActivityResultAction function.
     */
    private fun generateExtension(
        member: ExecutableElement,
        flowAnnotation: OnRuntimePermissionResultFlow
    ) {
        val requireArguments = 1
        val flowStateArguments: List<VariableElement> =
            if (member.parameters.size > requireArguments) {
                member.parameters.subList(0, member.parameters.lastIndex)
            } else {
                emptyList()
            }

        // Generate Entry-Point Variable.
        val entryPointName = "${member.methodName}RuntimePermissionEntry"
        val workflowEntry = PropertySpec.builder(
            entryPointName,
            CLASS_RUNTIME_PERMISSION_ACTION
        ).initializer(
            buildString {
                append("""${propertyWorkflowRegistry.name}.requestPermissionsAction("$entryPointName") { sender, _, grantResults, savedFlowState ->""")
                append("\n")
                if (flowStateArguments.isEmpty()) {
                    append("sender.${member.methodName}(grantResults)")
                } else {
                    append("sender.${member.methodName}(grantResults, savedFlowState)")
                }
                append("\n")
                append("}")
            }
        ).addModifiers(KModifier.PRIVATE).build()

        // Generate Entry-Point function.
        val extensionFunc = FunSpec.builder(flowAnnotation.entryPointName).apply {
            addModifiers(KModifier.INTERNAL)
            receiver(workflowOwner.poetClassName)
            addStatement("validateFlow()")
            addParameter(
                ParameterSpec.builder("permissions", CLASS_STRING_LIST)
                    .defaultValue(buildString {
                        append("listOf(")
                        flowAnnotation.permissions.forEachIndexed { index, permission ->
                            if (index > 0) {
                                append(",")
                            }
                            append("\"$permission\"")
                        }
                        append(")")
                    })
                    .build()
            )
            if (flowStateArguments.isEmpty()) {
                addStatement("${workflowEntry.name}(this, permissions.toList())")
            } else {
                addParameter("flowState", CLASS_BUNDLE)
                addStatement("${workflowEntry.name}(this, permissions.toList(), flowState)")
            }
            returns(Unit::class.java)
        }.build()

        fileSpec.addProperty(workflowEntry)
        fileSpec.addFunction(extensionFunc)
    }

    companion object {
        private val CLASS_PARCELABLE_DIALOG_FACTORY =
            ClassName(
                "com.eaglesakura.firearm.experimental.workflow.dialog",
                "ParcelableDialogFactory"
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