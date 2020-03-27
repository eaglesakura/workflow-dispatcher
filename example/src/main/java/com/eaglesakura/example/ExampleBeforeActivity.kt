package com.eaglesakura.example

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import java.util.Date

class ExampleBeforeActivity : AppCompatActivity() {

    private var tempDialogStartDate: Date? = null

    /**
     * Workflow Step 1, Show Dialog.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            // show dialog
            val startDate = Date()
            ExampleAlertDialogFragment(startDate).show(
                supportFragmentManager,
                "ExampleAlertDialogFragment"
            )

            // save state
            tempDialogStartDate = startDate
        } else {
            Toast.makeText(this, "ReBuild Activity", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("startDate", tempDialogStartDate)

        Toast.makeText(this, "onSaveInstanceState", Toast.LENGTH_SHORT).show()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tempDialogStartDate = savedInstanceState.getSerializable("startDate") as? Date
        Toast.makeText(this, "onRestoreInstanceState", Toast.LENGTH_SHORT).show()
    }

    /**
     * Workflow Step 2, on Dialog dismiss, Request runtime permission.
     */
    private fun onClickStartButton() {
        // get runtime permission.
        requestPermissions(
            arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION
        )
    }

    /**
     * Workflow Step 3, permission granted, Show Website on Chrome.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION -> {
                if (!grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                    // all permission granted.
                    // show browser.
                    startActivityForResult(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://google.com")
                        ), REQUEST_SHOW_BROWSER
                    )
                } else {
                    finish()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * Workflow Step 4, close browser, show temporary data,
     *
     * are you tired?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SHOW_BROWSER -> {
                // read temporary data, clear temporary
                val startDate = tempDialogStartDate!!
                tempDialogStartDate = null

                // show toast
                Toast.makeText(this, "done workflow, startDate='$startDate'", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    class ExampleAlertDialogFragment : DialogFragment {
        constructor()
        constructor(startDate: Date) {
            arguments = bundleOf(
                ARGUMENT_START_DATE to startDate
            )
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val startDate = requireArguments().getSerializable(ARGUMENT_START_DATE) as Date

            return AlertDialog.Builder(requireContext())
                .setMessage("Start workflow, startDate='$startDate'")
                .setPositiveButton("Start") { _, _ ->
                    // click positive button
                    (requireActivity() as ExampleBeforeActivity).onClickStartButton()
                }
                .setNegativeButton("Abort") { _, _ ->
                    // click negative button
                    requireActivity().finish()
                }
                .setOnCancelListener {
                    // cancel.
                    requireActivity().finish()
                }
                .create()
        }

        companion object {
            private const val ARGUMENT_START_DATE = "ARGUMENT_START_DATE"
        }
    }

    companion object {
        const val REQUEST_PERMISSION = 0x0010
        const val REQUEST_SHOW_BROWSER = 0x0011
    }
}