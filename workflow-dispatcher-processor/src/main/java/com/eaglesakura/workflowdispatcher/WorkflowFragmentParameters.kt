package com.eaglesakura.workflowdispatcher

import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType

/**
 * Mustache template data.
 *
 * @see "templates/dialog_dispatcher_fragment.mustache"
 */
internal data class WorkflowFragmentParameters(
    val packageName: String,
    val className: String,
    val savedStateList: List<SavedState>,
    val callbackClassName: String,
    val callbackMethodName: String,
    val entryPointFunctionName: String,

    /**
     * Required permissions for OnRuntimePermissionResultFlow
     */
    val permissions: List<String> = emptyList(),
) {

    /**
     * Saved state parameter for Workflow fragment.
     * e.g.)
     * // generated code.
     * intState: Int
     *  get() = requireArguments().get("intState") as Int
     */
    data class SavedState(
        val name: String,
        val typeName: String,
        val nullable: Boolean,
    )

    companion object {
        fun parseSavedState(receiver: ExecutableElement, defaultArguments: Int): List<SavedState> {
            return receiver
                .parameters
                .subList(defaultArguments, receiver.parameters.size)
                .map { elem ->
                    SavedState(
                        name = elem.simpleName.toString(),
                        typeName = (elem.asType() as DeclaredType).toString(),
                        nullable = elem.getAnnotation(org.jetbrains.annotations.Nullable::class.java) != null,
                    )
                }
        }
    }
}
