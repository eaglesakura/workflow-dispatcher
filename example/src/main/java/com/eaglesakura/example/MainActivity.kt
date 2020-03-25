package com.eaglesakura.example

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eaglesakura.firearm.experimental.workflow.annotations.OnActivityResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnDialogResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.OnRuntimePermissionResultFlow
import com.eaglesakura.firearm.experimental.workflow.annotations.WorkflowOwner
import com.eaglesakura.firearm.experimental.workflow.dialog.AlertDialogFactory
import com.eaglesakura.firearm.experimental.workflow.dialog.DialogResult

@WorkflowOwner
class MainActivity : AppCompatActivity() {

    init {
        loadWorkflowModules()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showExampleDialog(AlertDialogFactory.Builder().also { builder ->
            builder.message = "Start Runtime-Permission!!"
            builder.positiveButton = "Start"
            builder.negativeButton = "Abort"
        }.build())

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
    internal fun onShowExampleDialogWithStateResult(result: DialogResult, savedFlowState: Bundle?) {
    }

    /**
     * Done runtime permission result.
     */
    @OnRuntimePermissionResultFlow(
        "runtimePermissionFlow",
        [android.Manifest.permission.WRITE_EXTERNAL_STORAGE]
    )
    internal fun onRuntimePermissionResult(grantResults: List<Int>) {
        if (grantResults.count { it == PackageManager.PERMISSION_DENIED } > 0) {
            // Not granted...
            finish()
            return
        }

        activityResultFlow(Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")))
    }

    /**
     * Done runtime permission result.
     */
    @OnRuntimePermissionResultFlow(
        "runtimePermissionWithStateFlow",
        [android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE]
    )
    internal fun onRuntimePermissionWithStateResult(
        grantResults: List<Int>,
        savedFlowState: Bundle?
    ) {
    }

    /**
     * Done activity result.
     */
    @OnActivityResultFlow("activityResultFlow")
    internal fun onActivityResultFlowResult(result: Int, data: Intent?) {
        Toast.makeText(this, "Done All Flow!!", Toast.LENGTH_LONG).show()
    }

    @OnActivityResultFlow("activityResultFlowWithState")
    internal fun onActivityResultWithStateFlowResult(
        result: Int,
        data: Intent?,
        savedFlowState: Bundle?
    ) {
    }

}
