package com.eaglesakura.firearm.experimental.workflow

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eaglesakura.armyknife.android.junit4.extensions.instrumentationBlockingTest
import com.eaglesakura.armyknife.android.junit4.extensions.makeActivity
import com.eaglesakura.armyknife.android.junit4.extensions.makeFragment
import com.eaglesakura.workflowdispatcher.WorkflowRegistry
import com.eaglesakura.workflowdispatcher.dialog.AlertDialogFactory
import com.eaglesakura.workflowdispatcher.dialog.DialogResult
import com.eaglesakura.workflowdispatcher.workflowRequiredModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkflowRegistryDialogTest {

    @Test
    fun showDialog_activity() = instrumentationBlockingTest {
        val activity = makeActivity()

        withContext(Dispatchers.Main) {
            TestingDialogActivityWorkflow.startTest(activity)
        }

        delay(250)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // click OK.
        delay(500)
        onView(withText("OK")).perform(ViewActions.click())

        // click Cancel.
        delay(500)
        onView(withText("Cancel")).perform(ViewActions.click())

        delay(500)
    }

    @Test
    fun showDialog_fragment() = instrumentationBlockingTest {
        val fragment = makeFragment(WorkflowRegistryFragment::class)
        delay(500)
        val activity = fragment.requireActivity()

        withContext(Dispatchers.Main) {
            TestingDialogWorkflow.startTest(fragment)
        }

        delay(250)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // click OK.
        delay(500)
        onView(withText("OK")).perform(ViewActions.click())

        // click Cancel.
        delay(500)
        onView(withText("Cancel")).perform(ViewActions.click())

        delay(500)
    }

    @Test
    fun showDialog_restoreState() = instrumentationBlockingTest {
        val fragment = makeFragment(WorkflowRegistryFragment::class)
        delay(500)
        val activity = fragment.requireActivity()

        withContext(Dispatchers.Main) {
            TestingWithStateFlow.startTest(fragment)
        }

        delay(250)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // click OK.
        delay(500)
        onView(withText("OK")).perform(ViewActions.click())

        delay(500)
    }
}

internal class WorkflowRegistryFragment : Fragment() {
    init {
        workflowRequiredModule(
            TestingWithStateFlow,
            TestingDialogWorkflow
        )
    }
}

private object TestingWithStateFlow {

    val workflowRegistry = WorkflowRegistry.of(WorkflowRegistryFragment::class)

    fun startTest(fragment: WorkflowRegistryFragment) {
        showDialog_withState(
            fragment,
            AlertDialogFactory.Builder().apply {
                title = "title"
                message = "Dialog 1"
                positiveButton = "OK"
                negativeButton = "Cancel"
            }.build(),
            bundleOf(
                Pair("key", "value")
            )
        )
    }

    private val showDialog_withState =
        workflowRegistry.dialogAction("showDialog_withState") { sender, dialogResult, savedFlowState ->
            assertNotNull(savedFlowState)
            assertEquals("value", savedFlowState!!.get("key"))

            sender.requireActivity().finish()
        }
}

private object TestingDialogWorkflow {

    val workflowRegistry = WorkflowRegistry.of(WorkflowRegistryFragment::class)

    fun startTest(fragment: WorkflowRegistryFragment) {
        showDialog_clickOk(
            fragment,
            AlertDialogFactory.Builder().apply {
                title = "title"
                message = "Dialog 1"
                positiveButton = "OK"
                negativeButton = "Cancel"
            }.build()
        )
    }

    private val showDialog_clickOk =
        workflowRegistry.dialogAction("showDialog_clickOk") { sender, dialogResult, _ ->
            assertEquals(DialogResult.Selection.Positive, dialogResult.selected)

            // next
            showDialog_clickCancel(
                sender,
                AlertDialogFactory.Builder().apply {
                    title = "title"
                    message = "Dialog 2"
                    positiveButton = "OK"
                    negativeButton = "Cancel"
                }.build()
            )
        }

    private val showDialog_clickCancel =
        workflowRegistry.dialogAction("showDialog_clickCancel") { sender, dialogResult, _ ->
            assertEquals(DialogResult.Selection.Negative, dialogResult.selected)
            sender.requireActivity().finish()
        }
}

private object TestingDialogActivityWorkflow {

    val workflowRegistry = WorkflowRegistry.of(AppCompatActivity::class)

    fun startTest(activity: AppCompatActivity) {
        showDialog_clickOk(
            activity,
            AlertDialogFactory.Builder().apply {
                title = "title"
                message = "Dialog 1"
                positiveButton = "OK"
                negativeButton = "Cancel"
            }.build()
        )
    }

    private val showDialog_clickOk =
        workflowRegistry.dialogAction("showDialog_clickOk") { sender, dialogResult, _ ->
            assertEquals(DialogResult.Selection.Positive, dialogResult.selected)

            // next
            showDialog_clickCancel(
                sender,
                AlertDialogFactory.Builder().apply {
                    title = "title"
                    message = "Dialog 2"
                    positiveButton = "OK"
                    negativeButton = "Cancel"
                }.build()
            )
        }

    private val showDialog_clickCancel =
        workflowRegistry.dialogAction("showDialog_clickCancel") { sender, dialogResult, _ ->
            assertEquals(DialogResult.Selection.Negative, dialogResult.selected)
            sender.finish()
        }
}
