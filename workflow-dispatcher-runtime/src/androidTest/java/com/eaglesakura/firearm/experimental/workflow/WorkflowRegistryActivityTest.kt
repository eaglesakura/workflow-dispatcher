package com.eaglesakura.firearm.experimental.workflow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eaglesakura.armyknife.android.extensions.UIHandler
import com.eaglesakura.armyknife.android.junit4.extensions.compatibleBlockingTest
import com.eaglesakura.armyknife.android.junit4.extensions.makeActivity
import com.eaglesakura.armyknife.android.junit4.extensions.makeFragment
import com.eaglesakura.workflowdispatcher.WorkflowRegistry
import com.eaglesakura.workflowdispatcher.workflowRequiredModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkflowRegistryActivityTest {
    @Test
    fun startActivity_activity() = compatibleBlockingTest {
        val activity = makeActivity(FirstActivity::class)

        delay(1000)

        withContext(Dispatchers.Main) {
            SendActivityBootFlow.startWithSavedState(activity)
        }

        delay(1000)

        assertTrue(activity.isFinishing)
    }

    @Test
    fun startActivity_activity_withoutState() = compatibleBlockingTest {
        val activity = makeActivity(FirstActivity::class)

        delay(1000)

        withContext(Dispatchers.Main) {
            SendActivityBootFlow.startWithoutState(activity)
        }

        delay(1000)

        assertTrue(activity.isFinishing)
    }

    @Test
    fun startActivity_fragment() = compatibleBlockingTest {
        val fragment = makeFragment(FirstFragment::class)

        delay(1000)
        val activity = fragment.requireActivity()

        withContext(Dispatchers.Main) {
            SendActivityBootFlowByFragment.startWithSavedState(fragment)
        }

        delay(1000)

        assertTrue(activity.isFinishing)
    }

    @Test
    fun startActivity_fragment_withoutState() = compatibleBlockingTest {
        val fragment = makeFragment(FirstFragment::class)

        delay(1000)
        val activity = fragment.requireActivity()

        withContext(Dispatchers.Main) {
            SendActivityBootFlowByFragment.startWithoutState(fragment)
        }

        delay(1000)

        assertTrue(activity.isFinishing)
    }
}

internal class FirstActivity : AppCompatActivity() {
    init {
        workflowRequiredModule(SendActivityBootFlow)
    }
}

internal object SendActivityBootFlow {
    private val workflowRegistry = WorkflowRegistry.of(FirstActivity::class)

    fun startWithSavedState(activity: FirstActivity) {
        bootSecondActivity(
            sender = activity,
            intent = Intent(activity, SecondActivity::class.java),
            flowState = bundleOf(Pair("savedKey", "savedValue"))
        )
    }

    fun startWithoutState(activity: FirstActivity) {
        bootSecondActivityWithoutState(
            sender = activity,
            intent = Intent(activity, SecondActivity::class.java),
            options = null
        )
    }

    private val bootSecondActivity =
        workflowRegistry.activityResultAction("bootSecondActivity") { sender, result, savedFlowState ->
            assertEquals(Activity.RESULT_OK, result.result)
            val data = result.data
            assertNotNull(data)
            assertEquals("value", data?.getStringExtra("key"))
            assertNotNull(savedFlowState)
            assertEquals("savedValue", savedFlowState?.get("savedKey"))

            sender.finish()
        }

    private val bootSecondActivityWithoutState =
        workflowRegistry.activityResultAction("bootSecondActivityWithoutState") { sender, result, savedFlowState ->
            assertEquals(Activity.RESULT_OK, result.result)
            val data = result.data
            assertNotNull(data)
            assertEquals("value", data?.getStringExtra("key"))
            assertNull(savedFlowState)

            sender.finish()
        }
}

internal class FirstFragment : Fragment() {
    init {
        workflowRequiredModule(
            SendActivityBootFlowByFragment
        )
    }
}

internal object SendActivityBootFlowByFragment {
    private val workflowRegistry = WorkflowRegistry.of(FirstFragment::class)

    fun startWithSavedState(fragment: FirstFragment) {
        bootSecondActivity(
            sender = fragment,
            intent = Intent(fragment.requireActivity(), SecondActivity::class.java),
            options = null,
            flowState = bundleOf(Pair("savedKey", "savedValue"))
        )
    }

    fun startWithoutState(fragment: FirstFragment) {
        bootSecondActivityWithoutState(
            sender = fragment,
            intent = Intent(fragment.requireActivity(), SecondActivity::class.java),
            options = null,
            flowState = null
        )
    }

    private val bootSecondActivity =
        workflowRegistry.activityResultAction("bootSecondActivity") { sender, result, savedFlowState ->
            val data = result.data
            assertEquals(Activity.RESULT_OK, result.result)
            assertNotNull(data)
            assertEquals("value", data?.getStringExtra("key"))
            assertEquals("savedValue", savedFlowState?.get("savedKey"))

            sender.requireActivity().finish()
        }

    private val bootSecondActivityWithoutState =
        workflowRegistry.activityResultAction("bootSecondActivityWithoutState") { sender, result, savedFlowState ->
            val data = result.data

            assertEquals(Activity.RESULT_OK, result.result)
            assertNotNull(data)
            assertEquals("value", data?.getStringExtra("key"))
            assertNull(savedFlowState)

            sender.requireActivity().finish()
        }
}

internal class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UIHandler.postDelayed(
            {
                setResult(
                    RESULT_OK,
                    Intent().also {
                        it.putExtra("key", "value")
                    }
                )
                finish()
            },
            100
        )
    }
}
