package com.eaglesakura.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eaglesakura.firearm.experimental.workflow.activity.ActivityResult
import com.eaglesakura.firearm.experimental.workflow.annotations.OnActivityResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnDialogResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnRuntimePermissionResultFlow
import com.eaglesakura.firearm.experimental.workflow.dialog.AlertDialogFactory
import com.eaglesakura.firearm.experimental.workflow.dialog.DialogResult
import com.eaglesakura.firearm.experimental.workflow.permission.RuntimePermissionResult
import java.util.Date

class ExampleAfterActivity : AppCompatActivity() {

    init {
        loadWorkflowModules()
    }

    /**
     * Workflow Step 1, Show Dialog.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val startDate = Date()
            showStartDialog(AlertDialogFactory.Builder().also { builder ->
                builder.message = "Start workflow, startDate='$startDate'"
                builder.positiveButton = "Start"
                builder.negativeButton = "Abort"
            }.build(), startDate)
        } else {
            Toast.makeText(this, "ReBuild Activity", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Workflow Step 2, on Dialog dismiss, Request runtime permission.
     */
    @OnDialogResultFlow("showStartDialog")
    fun onShowStartDialogResult(result: DialogResult, startDate: Date) {
        when (result.selected) {
            DialogResult.Selection.Positive -> requestExamplePermissions(startDate = startDate)
            else -> finish()
        }
    }

    /**
     * Workflow Step 3, permission granted, Show Website on Chrome.
     */
    @OnRuntimePermissionResultFlow(
        "requestExamplePermissions",
        [android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE]
    )
    fun onRequestExamplePermissions(result: RuntimePermissionResult, startDate: Date) {
        when {
            result.allGranted -> showWebsite(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://google.com")
                ), startDate = startDate
            )
            else -> finish()
        }
    }

    /**
     * Workflow Step 4, close browser, show temporary data.
     */
    @OnActivityResultFlow("showWebsite")
    fun onShowWebsiteOnChromeResult(result: ActivityResult, startDate: Date) {
        // show toast
        Toast.makeText(this, "done workflow, startDate='$startDate'", Toast.LENGTH_SHORT)
            .show()
    }
}