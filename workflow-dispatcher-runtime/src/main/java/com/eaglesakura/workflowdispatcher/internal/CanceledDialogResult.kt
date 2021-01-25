package com.eaglesakura.workflowdispatcher.internal

import com.eaglesakura.workflowdispatcher.dialog.DialogResult
import kotlinx.parcelize.Parcelize

/**
 * Dialog is cancel.
 */
@Parcelize
internal class CanceledDialogResult : DialogResult() {
    override val selected: Selection
        get() = Selection.Cancel
}
