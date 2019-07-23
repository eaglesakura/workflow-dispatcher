package com.eaglesakura.firearm.experimental.workflow.internal

import com.eaglesakura.firearm.experimental.workflow.dialog.DialogResult
import kotlinx.android.parcel.Parcelize

/**
 * Dialog is cancel.
 */
@Parcelize
internal class CanceledDialogResult : DialogResult() {
    override val selected: Selection
        get() = Selection.Cancel
}