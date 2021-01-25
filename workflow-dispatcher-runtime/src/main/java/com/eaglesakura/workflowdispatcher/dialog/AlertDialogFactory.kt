package com.eaglesakura.workflowdispatcher.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * Dialog creator with androidx.AlertDialog
 *
 * e.g.)
 *
 * fun makeDialogFactory(): AlertDialogFactory {
 *      return AlertDialogFactory.Builder().apply { builder ->
 *          message = "This is a"
 *          positiveButton = "Pen"
 *          negativeButton = "Apple"
 *      }.build()
 * }
 *
 * @see androidx.appcompat.app.AlertDialog
 */
@Parcelize
open class AlertDialogFactory protected constructor(
    val title: String?,

    val message: String?,

    val positiveButton: String?,

    val negativeButton: String?,

    val neutralButton: String?,

    val cancelable: Boolean,

    val notFocusable: Boolean
) : ParcelableDialogFactory {

    @IgnoredOnParcel
    private var selection: DialogResult.Selection? = null

    override fun onCreateDialog(fragment: DialogFragment, savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(fragment.requireActivity()).apply {
            title?.also { setTitle(it) }
            message?.also { setMessage(it) }
            positiveButton?.also {
                setPositiveButton(it) { _, _ ->
                    selection =
                        DialogResult.Selection.Positive
                }
            }
            negativeButton?.also {
                setNegativeButton(it) { _, _ ->
                    selection =
                        DialogResult.Selection.Negative
                }
            }
            neutralButton?.also {
                setNeutralButton(it) { _, _ ->
                    selection =
                        DialogResult.Selection.Neutral
                }
            }
            fragment.isCancelable = cancelable
        }.create().also { dialog ->
            if (notFocusable) {
                dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            }
        }
    }

    override fun getResult(
        fragment: DialogFragment,
        dialogInterface: DialogInterface
    ): DialogResult? {
        return if (selection == null) {
            null
        } else {
            Result(selection!!)
        }
    }

    @Parcelize
    internal class Result(
        override val selected: Selection
    ) : DialogResult()

    class Builder {
        /**
         * Optional
         * Dialog's title.
         */
        var title: String? = null

        /**
         * Optional
         * Dialog's message.
         */
        var message: String? = null

        /**
         * Positive button message.
         *
         * e.g.) "OK"
         */
        var positiveButton: String? = null

        /**
         * Negative button message.
         *
         * e.g.) "Leave"
         */
        var negativeButton: String? = null

        /**
         * Neutral button message.
         */
        var neutralButton: String? = null

        /**
         * cancelable dialogAction.
         */
        var cancelable: Boolean = true

        /**
         * If this value set 'true',
         * then 'WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE' add to Dialog's window.
         * This option use to Immersive Mode.
         */
        var notFocusable: Boolean = false

        /**
         * Build DialogFactory.
         *
         * @see ParcelableDialogFactory
         */
        fun build(): AlertDialogFactory {
            return AlertDialogFactory(
                title = this.title,
                message = this.message,
                positiveButton = this.positiveButton,
                negativeButton = this.negativeButton,
                neutralButton = this.neutralButton,
                cancelable = this.cancelable,
                notFocusable = this.notFocusable
            )
        }
    }
}
