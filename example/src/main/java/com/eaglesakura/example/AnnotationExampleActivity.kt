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

class AnnotationExampleActivity : AppCompatActivity() {

    init {
        loadWorkflowModules()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val startDate = Date()
            showExampleDialogWithState(AlertDialogFactory.Builder().also { builder ->
                builder.message = "Start Runtime-Permission!!\nstartDate='$startDate'"
                builder.positiveButton = "Start"
                builder.negativeButton = "Abort"
            }.build(), startDate, "https://google.com")
        } else {
            Toast.makeText(this, "ReBuild Activity", Toast.LENGTH_SHORT).show()
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
            finish()
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
            finish()
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
        Toast.makeText(this, "Done All Flow!!", Toast.LENGTH_LONG).show()
    }

    @OnActivityResultFlow("activityResultFlowWithState")
    internal fun onActivityResultWithStateFlowResult(
        result: ActivityResult, startDate: Date
    ) {
        Toast.makeText(this, "startDate='$startDate'", Toast.LENGTH_LONG).show()
    }

}