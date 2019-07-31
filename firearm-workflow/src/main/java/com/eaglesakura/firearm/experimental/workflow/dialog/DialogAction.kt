package com.eaglesakura.firearm.experimental.workflow.dialog

import android.os.Bundle
import androidx.annotation.UiThread
import androidx.core.os.bundleOf
import com.eaglesakura.armyknife.android.extensions.assertUIThread
import com.eaglesakura.firearm.experimental.workflow.WorkflowRegistry
import com.eaglesakura.firearm.experimental.workflow.internal.WorkflowDialogFragment

/**
 * Show dialogAction flow.
 * This is Functional object.
 * see test codes.
 *
 * e.g.
 *
 * val workflowRegistry = WorkflowRegistry.of(ExampleFragment::class)
 *
 * workflowRegistry.dialogAction("errorHandle") { sender, dialogResult, savedFlowState ->
 *      when(dialogResult.selected) {
 *          Selection.Positive -> sender.selectPositive()
 *          else -> sender.selectElse()
 *      }
 * }
 *
 * @see WorkflowRegistry
 * @see DialogResult
 */
class DialogAction<T : Any> internal constructor(
    private val registry: WorkflowRegistry<T>,
    private val id: Any,
    private val requestCode: Int,
    private val onDismissHandler: (sender: T, result: DialogResult, savedFlowState: Bundle?) -> Unit
) {

    /**
     * Show dialogAction.
     */
    @UiThread
    operator fun invoke(sender: T, factory: ParcelableDialogFactory, flowState: Bundle? = null) {
        assertUIThread()

        val holder = registry.getStateHolder(sender)
        if (flowState != null) {
            registry.setExtra(
                holder, id, bundleOf(
                    Pair(EXTRA_STATE, flowState)
                )
            )
        }

        val fragmentManager = registry.getProvider(sender).childFragmentManager

        val fragment =
            WorkflowDialogFragment(
                requestCode = requestCode,
                factory = factory
            )
        fragment.showNow(
            fragmentManager,
            "DialogAction@${(sender as Any).javaClass.name}@$id"
        )
    }

    internal fun onDismiss(sender: T, result: DialogResult) {
        val holder = registry.getStateHolder(sender)
        val extra = registry.getExtra(holder, id)
        try {
            onDismissHandler(sender, result, extra?.getBundle(EXTRA_STATE))
        } finally {
            registry.removeExtra(holder, id)
        }
    }

    companion object {
        private const val EXTRA_STATE = "dialogAction.state"
    }
}