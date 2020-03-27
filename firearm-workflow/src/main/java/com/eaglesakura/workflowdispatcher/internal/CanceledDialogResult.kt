package com.eaglesakura.workflowdispatcher.internal

import com.eaglesakura.workflowdispatcher.dialog.DialogResult
import kotlinx.android.parcel.Parcelize

/**
 * Dialog is cancel.
 */
@Parcelize
internal class CanceledDialogResult : DialogResult() {
    override val selected: Selection
        get() = Selection.Cancel
}