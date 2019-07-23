package com.eaglesakura.firearm.experimental.workflow.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
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
class AlertDialogFactory internal constructor(
    internal val title: String?,

    internal val message: String?,

    internal val positiveButton: String?,

    internal val negativeButton: String?,

    internal val neutralButton: String?,

    internal val cancelable: Boolean

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
        }.create()
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
                cancelable = this.cancelable
            )
        }
    }
}