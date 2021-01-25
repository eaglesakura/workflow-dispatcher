package com.eaglesakura.workflowdispatcher.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.DialogFragment

interface ParcelableDialogFactory : Parcelable {
    /**
     * Make new dialogAction.
     */
    fun onCreateDialog(fragment: DialogFragment, savedInstanceState: Bundle?): Dialog

    /**
     * Make selected parameter.
     */
    fun getResult(fragment: DialogFragment, dialogInterface: DialogInterface): DialogResult?
}
