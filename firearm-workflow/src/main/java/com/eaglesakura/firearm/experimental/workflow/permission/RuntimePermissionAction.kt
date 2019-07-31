package com.eaglesakura.firearm.experimental.workflow.permission

import android.os.Bundle
import androidx.annotation.UiThread
import androidx.core.os.bundleOf
import com.eaglesakura.armyknife.android.extensions.assertUIThread
import com.eaglesakura.firearm.experimental.workflow.WorkflowRegistry

class RuntimePermissionAction<T : Any> internal constructor(
    private val registry: WorkflowRegistry<T>,
    private val id: Any,
    private val requestCode: Int,
    private val onRequestPermissionsResultHandler: (sender: T, permissions: List<String>, grantResults: List<Int>, savedFlowState: Bundle?) -> Unit
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
                holder, id, bundleOf(
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
                permissions,
                grantResults,
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