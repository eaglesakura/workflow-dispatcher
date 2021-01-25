package com.eaglesakura.workflowdispatcher.permission

import android.os.Bundle
import androidx.annotation.UiThread
import androidx.core.os.bundleOf
import com.eaglesakura.workflowdispatcher.WorkflowRegistry
import com.eaglesakura.workflowdispatcher.internal.armyknife_jetpack.assertUIThread

class RuntimePermissionAction<T : Any> internal constructor(
    private val registry: WorkflowRegistry<T>,
    private val id: String,
    private val requestCode: Int,
    private val onRequestPermissionsResultHandler: (sender: T, result: RuntimePermissionResult, savedFlowState: Bundle?) -> Unit
) {

    /**
     * Show RuntimePermission dialog.
     */
    @UiThread
    operator fun invoke(sender: T, permissions: Collection<String>, flowState: Bundle? = null) {
        assertUIThread()

        val holder = registry.getStateHolder(sender)
        if (flowState != null) {
            registry.setExtra(
                holder, id,
                bundleOf(
                    Pair(EXTRA_STATE, flowState)
                )
            )
        }

        val provider = registry.getProvider(sender)
        provider.requestPermissions(permissions.toTypedArray(), requestCode)
    }

    internal fun onRequestPermissionsResult(
        sender: T,
        permissions: List<String>,
        grantResults: List<Int>
    ) {
        val holder = registry.getStateHolder(sender)
        val extra = registry.getExtra(holder, id)
        try {
            onRequestPermissionsResultHandler(
                sender,
                RuntimePermissionResult(
                    permissions = permissions,
                    granted = grantResults
                ),
                extra?.getBundle(EXTRA_STATE)
            )
        } finally {
            registry.removeExtra(holder, id)
        }
    }

    companion object {
        private const val EXTRA_STATE = "runtimePermissionAction.state"
    }
}
