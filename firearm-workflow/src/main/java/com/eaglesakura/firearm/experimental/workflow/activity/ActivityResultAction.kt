package com.eaglesakura.firearm.experimental.workflow.activity

import android.content.Intent
import android.os.Bundle
import androidx.annotation.UiThread
import androidx.core.os.bundleOf
import com.eaglesakura.armyknife.android.extensions.assertUIThread
import com.eaglesakura.firearm.experimental.workflow.WorkflowRegistry

/**
 * Show activityResultAction flow.
 * This is Functional object.
 * see test codes.
 *
 * e.g.
 *
 * val workflowRegistry = WorkflowRegistry.of(ExampleFragment::class)
 *
 * workflowRegistry.activityResultAction("pickPhoto") { sender, result, data, savedFlowState ->
 *      if(result != Activity.RESULT_OK) {
 *          return
 *      }
 *
 *      // do something.
 * }
 *
 * @see WorkflowRegistry
 */
class ActivityResultAction<T : Any> internal constructor(
    private val registry: WorkflowRegistry<T>,
    private val id: Any,
    private val requestCode: Int,
    private val onActivityResultHandler: (sender: T, result: Int, data: Intent?, savedFlowState: Bundle?) -> Unit
) {

    /**
     * startActivityForResult.
     */
    @UiThread
    operator fun invoke(
        sender: T,
        intent: Intent,
        options: Bundle? = null,
        flowState: Bundle? = null
    ) {
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
        val activity = provider.requireActivity()
        activity.startActivityFromFragment(provider, intent, requestCode, options)
    }

    internal fun onActivityResult(sender: T, result: Int, data: Intent?) {
        val holder = registry.getStateHolder(sender)
        val extra = registry.getExtra(holder, id)
        try {
            onActivityResultHandler(sender, result, data, extra?.getBundle(EXTRA_STATE))
        } finally {
            registry.removeExtra(holder, id)
        }
    }

    companion object {
        private const val EXTRA_STATE = "activityResultAction.state"
    }
}