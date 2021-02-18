package com.eaglesakura.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import com.eaglesakura.workflowdispatcher.OnActivityResultFlow
import com.eaglesakura.workflowdispatcher.OnDialogResultFlow
import com.eaglesakura.workflowdispatcher.OnRuntimePermissionResultFlow
import com.eaglesakura.workflowdispatcher.dialog.AlertDialogFactory
import com.eaglesakura.workflowdispatcher.dialog.DialogResult
import com.eaglesakura.workflowdispatcher.permission.RuntimePermissionResult
import java.util.Date

class AnnotationExampleFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val startDate = Date()
            showExampleDialogWithState(AlertDialogFactory.Builder().also { builder ->
                builder.message = "Start Runtime-Permission!!\nstartDate='$startDate'"
                builder.positiveButton = "Start"
                builder.negativeButton = "Abort"
            }.build(), startDate, "https://google.com")
        } else {
            Toast.makeText(requireActivity(), "ReBuild Activity", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Done dialog result.
     */
    @OnDialogResultFlow("showExampleDialog")
    internal fun onShowExampleDialogResult(result: DialogResult) {
        when (result.selected) {
            DialogResult.Selection.Positive -> {
                runtimePermissionFlow()
            }
        }
    }

    @OnDialogResultFlow("showExampleDialogWithState")
    internal fun onShowExampleDialogWithStateResult(
        result: DialogResult,
        startDate: Date,
        url: String
    ) {
        when (result.selected) {
            DialogResult.Selection.Positive -> {
                runtimePermissionWithStateFlow(url = url, startDate = startDate)
            }
        }
    }

    /**
     * Done runtime permission result.
     */
    @OnRuntimePermissionResultFlow(
        "runtimePermissionFlow",
        [android.Manifest.permission.WRITE_EXTERNAL_STORAGE]
    )
    internal fun onRuntimePermissionResult(result: RuntimePermissionResult) {
        if (!result.allGranted) {
            requireActivity().finish()
            return
        }

        activityResultFlow(Intent(Intent.ACTION_VIEW, Uri.parse("https://yahoo.com")))
    }

    /**
     * Done runtime permission result.
     */
    @OnRuntimePermissionResultFlow(
        "runtimePermissionWithStateFlow",
        [android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE]
    )
    internal fun onRuntimePermissionWithStateResult(
        result: RuntimePermissionResult,
        startDate: Date,
        url: String
    ) {
        if (!result.allGranted) {
            requireActivity().finish()
            return
        }

        activityResultFlowWithState(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)),
            startDate = startDate
        )
    }

    /**
     * Done activity result.
     */
    @OnActivityResultFlow("activityResultFlow")
    internal fun onActivityResultFlowResult(result: ActivityResult) {
        Toast.makeText(requireContext(), "Done All Flow!!", Toast.LENGTH_LONG).show()
    }

    @OnActivityResultFlow("activityResultFlowWithState")
    internal fun onActivityResultWithStateFlowResult(
        result: ActivityResult, startDate: Date
    ) {
        Toast.makeText(requireContext(), "startDate='$startDate'", Toast.LENGTH_LONG).show()
    }

}