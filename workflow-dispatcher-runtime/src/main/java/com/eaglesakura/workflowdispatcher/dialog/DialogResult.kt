package com.eaglesakura.workflowdispatcher.dialog

import android.os.Parcelable
import com.eaglesakura.workflowdispatcher.internal.CanceledDialogResult

/**
 * Dialog selected object.
 */
abstract class DialogResult : Parcelable {
    /**
     * User selection.
     * @see Selection
     */
    abstract val selected: Selection

    enum class Selection {
        /**
         * Positive button.
         */
        Positive,

        /**
         * Negative button.
         */
        Negative,

        /**
         * Neutral button.
         */
        Neutral,

        /**
         * Canceled or other.
         */
        Cancel,
    }

    companion object {
        /**
         * Dialog is canceled.
         */
        val CANCELED: DialogResult = CanceledDialogResult()
    }
}
