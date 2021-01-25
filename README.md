# Wofkflow Dispatcher

Let's delete `onActivityResult` `onRequestPermissionsResult` and `DialogListener` from Activity(or Fragment) !

Let's delete temporary values !


# How to Installation

1. Add 'workflow-dispatcher-runtime' module,
1. Add 'workflow-dispatcher-processor' kapt module.

```groovy
// add bintray repository
repositories {
    maven { url "https://dl.bintray.com/eaglesakura/maven/" }
}
apply plugin: 'kotlin-kapt'

// add runtime library, annotation processor.
dependencies {
    def workflow_dispatcher_version = "1.0.0"
    implementation("com.eaglesakura.workflowdispatcher:workflow-dispatcher-runtime:${workflow_dispatcher_version}") {
        exclude(group: "androidx.appcompat")
        exclude(group: "androidx.lifecycle")
    }
    kapt("com.eaglesakura.workflowdispatcher:workflow-dispatcher-processor:${workflow_dispatcher_version}")
}
```

# Samples

* All annotations and use case sample
    * [AnnotationExampleActivity](example/src/main/java/com/eaglesakura/example/AnnotationExampleActivity.kt)
    * [ExampleAfterActivity](example/src/main/java/com/eaglesakura/example/ExampleAfterActivity.kt)

# startActivityForResult dispatcher

## Before

without Workflow Dispatcher,

If use only Android SDK and AndroidX library.

```kotlin
class ExampleBeforeActivity : AppCompatActivity() {

    private var tempDialogStartDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null) {
            // ...
            // show browser.
            // save state
            tempDialogStartDate = startDate
            startActivityForResult(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://google.com")
                ), REQUEST_SHOW_BROWSER
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("startDate", tempDialogStartDate)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tempDialogStartDate = savedInstanceState.getSerializable("startDate") as? Date
    }

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

    companion object {
        const val REQUEST_SHOW_BROWSER = 0x0011
    }
}
```

## After

with Workflow Dispatcher

```kotlin

class ExampleAfterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState == null) {
            // show browser
            // ExampleAfterActivity.showWebsite() is generated by kapt(workflow-dispatcher-processor).
            // auto save/restore/clear `startDate` state.
            showWebsite(
                Intent(Intent.ACTION_VIEW,Uri.parse("https://google.com")),
                startDate = Date()
            )

            // without state
            // showWebsiteWithoutState(Intent(Intent.ACTION_VIEW,Uri.parse("https://google.com")))
        }
    }

    @OnActivityResultFlow("showWebsite")
    fun onShowWebsiteOnChromeResult(result: ActivityResult, startDate: Date) {
        // show toast
        Toast.makeText(this, "startDate='$startDate'", Toast.LENGTH_SHORT).show()
    }

    // If you not need state.
    @OnActivityResultFlow("showWebsiteWithoutState")
    fun onShowWebsiteOnChromeResultWithoutState(result: ActivityResult) {
    }
}
```

# requestPermissions dispatcher

## Before

```kotlin

class ExampleBeforeActivity : AppCompatActivity() {

    private var tempDialogStartDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(savedInstanceState == null) {
            // save state
            // get runtime permission.
            tempDialogStartDate = Date()
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSION
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("startDate", tempDialogStartDate)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tempDialogStartDate = savedInstanceState.getSerializable("startDate") as? Date
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION -> {
                if (!grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                    // permission all granted!!
                    // read temporary data, clear temporary
                    val startDate = tempDialogStartDate!!
                    tempDialogStartDate = null
                    Toast.makeText(this, "startDate='$startDate'", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    companion object {
        const val REQUEST_PERMISSION = 0x0010
    }
}
```

## After

```kotlin

class ExampleAfterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState == null) {
            // show runtime-permission daialog
            // ExampleAfterActivity.requestExamplePermissions() is generated by kapt(workflow-dispatcher-processor).
            // auto save/restore/clear `startDate` state.
            requestExamplePermissions(startDate = Date())
        }
    }

    @OnRuntimePermissionResultFlow(
        "requestExamplePermissions",
        [android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE]
    )
    fun onRequestExamplePermissions(result: RuntimePermissionResult, startDate: Date) {
        when {
            result.allGranted -> {
                // show toast with state
                Toast.makeText(this, "done workflow, startDate='$startDate'", Toast.LENGTH_SHORT).show()
            }
            else -> finish()
        }
    }
}
```

# DialogFragment dispatcher

## Before

```kotlin

class ExampleBeforeActivity : AppCompatActivity() {

    private var tempDialogStartDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(savedInstanceState == null) {
            // save state
            // Show alert dialog.
            val startDate = Date()
            tempDialogStartDate = startDate
            
            ExampleAlertDialogFragment(startDate)
                        .show(supportFragmentManager,"ExampleAlertDialogFragment")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("startDate", tempDialogStartDate)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tempDialogStartDate = savedInstanceState.getSerializable("startDate") as? Date
    }

    private fun onClickStartButton() {
        // Positive button click
        // read temporary data, clear temporary
        val startDate = tempDialogStartDate!!
        tempDialogStartDate = null
        Toast.makeText(this, "startDate='$startDate'", Toast.LENGTH_SHORT).show()
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
                .setPositiveButton("Positive") { _, _ ->
                    // click positive button
                    (requireActivity() as ExampleBeforeActivity).onClickStartButton()
                }
                .setNegativeButton("Negative") { _, _ ->
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
}
```

## After

```kotlin

class ExampleAfterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // show AlertDialog
            // ExampleAfterActivity.showStartDialog() is generated by kapt(workflow-dispatcher-processor).
            // auto save/restore/clear `startDate` state.
            val startDate = Date()
            showStartDialog(AlertDialogFactory.Builder().also { builder ->
                builder.message = "Start workflow, startDate='$startDate'"
                builder.positiveButton = "Positive"
                builder.negativeButton = "Negative"
            }.build(), startDate)
        } else {
            Toast.makeText(this, "ReBuild Activity", Toast.LENGTH_SHORT).show()
        }
    }
    
    @OnDialogResultFlow("showStartDialog")
    fun onShowStartDialogResult(result: DialogResult, startDate: Date) {
        when (result.selected) {
            DialogResult.Selection.Positive -> requestExamplePermissions(startDate = startDate)
            else -> finish()
        }
    }
}
```

## If You need custom Dialog

If you need Custmized dialog, than use 'ParcelableDialogFactory' interface.

see [AlertDialogFactory.kt](workflow-dispatcher-runtime/src/main/java/com/eaglesakura/workflowdispatcher/AlertDialogFactory.kt)

```kotlin
@Parcelize
open class MyCustomDialogFactory : ParcelableDialogFactory {
    override fun onCreateDialog(fragment: DialogFragment, savedInstanceState: Bundle?): Dialog {
        // create your dialog.
    }

    override fun getResult(fragment: DialogFragment,dialogInterface: DialogInterface): DialogResult? {
        // create result instance or null.
    }
}
```

# Q&A

## Support Activity rebuild?

YES.
`Workflow Dispatcher` has been supported to the Activity rebuild when Orientation changed(Low memory, or such else).

You need not save temporary values, and restore temporary values.

## Support Process reboot?

[Will support later](https://github.com/eaglesakura/workflow-dispatcher/issues/3)

## I can't understand your English.

Sorry, You can PULL REQUEST.

[日本語のドキュメントはこちら](https://eaglesakura.hatenablog.com/)

# LICENSE

MIT LICENSE

```
The MIT License (MIT)

Copyright (c) 2020 @eaglesakura

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```