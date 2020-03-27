package com.eaglesakura.workflowdispatcher.internal

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.eaglesakura.workflowdispatcher.dialog.DialogResult
import com.eaglesakura.workflowdispatcher.dialog.ParcelableDialogFactory

internal class WorkflowDialogFragment : DialogFragment {

    constructor() : super()

    constructor(requestCode: Int, factory: ParcelableDialogFactory) : super() {
        arguments = bundleOf(
            Pair(ARGUMENT_REQUEST_CODE, requestCode),
            Pair(ARGUMENT_DIALOG_FACTORY, factory)
        )
    }

    lateinit var factory: ParcelableDialogFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireParentFragment()
        require(arguments?.getParcelable<ParcelableDialogFactory>(ARGUMENT_DIALOG_FACTORY) != null)
        require(arguments?.getInt(ARGUMENT_REQUEST_CODE, 0) != 0)
        this.factory = arguments?.getParcelable<ParcelableDialogFactory>(ARGUMENT_DIALOG_FACTORY)
            ?: throw IllegalArgumentException("ARGUMENT_DIALOG_FACTORY is null")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return factory.onCreateDialog(this, savedInstanceState)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Log.d("WorkflowDialogFragment", "onCancel($dialog)")
        requireParentFragment().onActivityResult(
            requireArguments().getInt(ARGUMENT_REQUEST_CODE, 0),
            Activity.RESULT_CANCELED,
            null
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d("WorkflowDialogFragment", "onDismiss($dialog)")
        factory.getResult(this, dialog)?.also { dialogResult ->
            requireParentFragment().onActivityResult(
                requireArguments().getInt(ARGUMENT_REQUEST_CODE, 0),
                Activity.RESULT_OK,
                Intent().also { it.putExtra(RESULT_DIALOG_RESULT, dialogResult) }
            )
        }
    }

    companion object {
        internal const val ARGUMENT_DIALOG_FACTORY = "internal.ARGUMENT_DIALOG_FACTORY"
        internal const val ARGUMENT_REQUEST_CODE = "internal.ARGUMENT_REQUEST_CODE"

        private const val RESULT_DIALOG_RESULT = "internal.RESULT_DIALOG_RESULT"

        /**
         * Get dialogAction's selected from Intent.
         * When canceled by system, then returns `Canceled` instance.
         *
         * @see DialogResult
         */
        fun getResult(result: Int, data: Intent?): DialogResult {
            if (result != Activity.RESULT_OK || data == null) {
                return CanceledDialogResult()
            }

            return data.getParcelableExtra(RESULT_DIALOG_RESULT)
                ?: throw IllegalArgumentException("DialogResult is not set.")
        }
    }
}