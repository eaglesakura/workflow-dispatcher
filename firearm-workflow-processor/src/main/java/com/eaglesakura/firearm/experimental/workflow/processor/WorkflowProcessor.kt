package com.eaglesakura.firearm.experimental.workflow.processor

import com.eaglesakura.firearm.experimental.workflow.annotations.OnActivityResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnDialogResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnRuntimePermissionResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.WorkflowOwner
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.StandardLocation

private val parcelableDialogFactoryClass =
    ClassName("com.eaglesakura.firearm.experimental.workflow.dialog", "ParcelableDialogFactory")

private val bundleClass =
    ClassName("android.os", "Bundle")

private val intentClass =
    ClassName("android.content", "Intent")

private val TypeElement.workflowRegistryClass: ParameterizedTypeName
    get() = ClassName("com.eaglesakura.firearm.experimental.workflow", "WorkflowRegistry")
        .parameterizedBy(poetClassName)

private val stringList: ParameterizedTypeName
    get() = ClassName("kotlin.collections", "List")
        .parameterizedBy(STRING)

private val TypeElement.workflowActivityEntryClass: ParameterizedTypeName
    get() = ClassName(
        "com.eaglesakura.firearm.experimental.workflow.activity",
        "ActivityResultAction"
    ).parameterizedBy(poetClassName)

private val TypeElement.workflowDialogEntryClass: ParameterizedTypeName
    get() = ClassName("com.eaglesakura.firearm.experimental.workflow.dialog", "DialogAction")
        .parameterizedBy(poetClassName)

private val TypeElement.workflowRuntimePermissionEntryClass: ParameterizedTypeName
    get() = ClassName(
        "com.eaglesakura.firearm.experimental.workflow.permission",
        "RuntimePermissionAction"
    ).parameterizedBy(poetClassName)

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

    private fun generateExtension(
        roundEnv: RoundEnvironment,
        workflowOwner: TypeElement
    ) {
        val className = workflowOwner.simpleName
        val fileName = "${className}GeneratedExtensions.kt"
        println("Generate: $fileName")

        val elementUtils = processingEnv.elementUtils!!
        val filer = processingEnv.filer!!

        val generateFile = filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            workflowOwner.poetClassName.packageName,
            fileName
        )

        generateFile.openWriter().use { writer ->
            val spec = FileSpec.builder(
                workflowOwner.poetClassName.packageName,
                workflowOwner.simpleName.toString()
            )

            val workflowRegistry =
                PropertySpec.builder("registry", workflowOwner.workflowRegistryClass)
                    .initializer("WorkflowRegistry.of($className::class)")
                    .addModifiers(KModifier.PRIVATE)
                    .build()

            spec.addProperty(workflowRegistry)
            spec.addProperty(
                PropertySpec.builder("initialized", Boolean::class.java)
                    .mutable(true)
                    .initializer("false")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            spec.addFunction(
                FunSpec.builder("validateFlow")
                    .addModifiers(KModifier.PRIVATE)
                    .addStatement(
                        """
                        check(initialized) {
                            "You should call '$className.loadWorkflowModules()'"
                        }
                    """.trimIndent()
                    )
                    .build()
            )
            spec.addFunction(
                FunSpec.builder("loadWorkflowModules")
                    .addModifiers(KModifier.INTERNAL)
                    .receiver(workflowOwner.poetClassName)
                    .addStatement("initialized = true")
                    .build()
            )

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
                        generateFunction(
                            spec,
                            workflowRegistry,
                            roundEnv,
                            workflowOwner,
                            memberElement as ExecutableElement,
                            onDialogResultFlow
                        )
                    }
                    onActivityResultFlow != null -> {
                        generateFunction(
                            spec,
                            workflowRegistry,
                            roundEnv,
                            workflowOwner,
                            memberElement as ExecutableElement,
                            onActivityResultFlow
                        )
                    }
                    onRuntimePermissionResultFlow != null -> {
                        generateFunction(
                            spec,
                            workflowRegistry,
                            roundEnv,
                            workflowOwner,
                            memberElement as ExecutableElement,
                            onRuntimePermissionResultFlow
                        )
                    }
                }
            }

            writer.write(spec.build().toString())
        }
    }

    /**
     * Generate DialogFlowAction function.
     */
    private fun generateFunction(
        spec: FileSpec.Builder,
        workflowRegistry: PropertySpec,
        roundEnv: RoundEnvironment,
        workflowOwner: TypeElement,
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
            workflowOwner.workflowDialogEntryClass
        ).initializer(
            buildString {
                append("""${workflowRegistry.name}.dialogAction("$entryPointName") { sender, result, savedFlowState ->""")
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

            addParameter("factory", parcelableDialogFactoryClass)
            if (flowStateArguments.isEmpty()) {
                addStatement("${workflowEntry.name}(this, factory)")
            } else {
                addParameter("flowState", bundleClass)
                addStatement("${workflowEntry.name}(this, factory, flowState)")
            }
            returns(Unit::class.java)
        }.build()

        spec.addProperty(workflowEntry)
        spec.addFunction(extensionFunc)
    }

    /**
     * Generate ActivityResultAction function.
     */
    private fun generateFunction(
        spec: FileSpec.Builder,
        workflowRegistry: PropertySpec,
        roundEnv: RoundEnvironment,
        workflowOwner: TypeElement,
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
            workflowOwner.workflowActivityEntryClass
        ).initializer(
            buildString {
                append("""${workflowRegistry.name}.activityResultAction("$entryPointName") { sender, result, data, savedFlowState ->""")
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

            addParameter("intent", intentClass)
            addParameter(
                ParameterSpec.builder("options", bundleClass.copy(nullable = true))
                    .defaultValue("null")
                    .build()
            )
            if (flowStateArguments.isEmpty()) {
                addStatement("${workflowEntry.name}(this, intent, options)")
            } else {
                addParameter("flowState", bundleClass)
                addStatement("${workflowEntry.name}(this, intent, options, flowState)")
            }
            returns(Unit::class.java)
        }.build()

        spec.addProperty(workflowEntry)
        spec.addFunction(extensionFunc)
    }

    /**
     * Generate ActivityResultAction function.
     */
    private fun generateFunction(
        spec: FileSpec.Builder,
        workflowRegistry: PropertySpec,
        roundEnv: RoundEnvironment,
        workflowOwner: TypeElement,
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
            workflowOwner.workflowRuntimePermissionEntryClass
        ).initializer(
            buildString {
                append("""${workflowRegistry.name}.requestPermissionsAction("$entryPointName") { sender, _, grantResults, savedFlowState ->""")
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
                ParameterSpec.builder("permissions", stringList)
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
                addParameter("flowState", bundleClass)
                addStatement("${workflowEntry.name}(this, permissions.toList(), flowState)")
            }
            returns(Unit::class.java)
        }.build()

        spec.addProperty(workflowEntry)
        spec.addFunction(extensionFunc)
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