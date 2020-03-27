package com.eaglesakura.firearm.experimental.workflow

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.eaglesakura.armyknife.android.junit4.extensions.instrumentationBlockingTest
import com.eaglesakura.armyknife.android.junit4.extensions.makeActivity
import com.eaglesakura.armyknife.android.junit4.extensions.makeFragment
import com.eaglesakura.workflowdispatcher.WorkflowRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkflowRegistryRuntimePermissionTest {

    @Rule
    @JvmField
    internal val permissionRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Test
    fun runtimePermission_activity() = instrumentationBlockingTest {
        val activity = makeActivity()

        withContext(Dispatchers.Main) {
            AppCompatActivityRuntimePermissionWorkflow.start(activity)
        }

        while (!activity.isFinishing) {
            yield()
        }
    }

    @Test
    fun runtimePermission_fragment() = instrumentationBlockingTest {
        val fragment = makeFragment(Fragment::class)
        delay(100)
        val activity = fragment.requireActivity()

        withContext(Dispatchers.Main) {
            FragmentRuntimePermissionWorkflow.start(fragment)
        }

        while (!activity.isFinishing) {
            yield()
        }
    }
}

internal object AppCompatActivityRuntimePermissionWorkflow {
    private val workflowRegistry = WorkflowRegistry.of(AppCompatActivity::class)

    fun start(activity: AppCompatActivity) =
        requestRuntimePermissionImpl(
            sender = activity,
            permissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            flowState = bundleOf(Pair("savedKey", "savedValue"))
        )

    private val requestRuntimePermissionImpl =
        workflowRegistry.requestPermissionsAction("requestRuntimePermissionImpl") { sender, result, savedFlowState ->
            val permissions = result.permissions
            val grantResults = result.granted

            assertEquals(1, permissions.size)
            assertEquals(grantResults.size, permissions.size)
            assertEquals(Manifest.permission.WRITE_EXTERNAL_STORAGE, permissions[0])
            assertEquals(PackageManager.PERMISSION_GRANTED, grantResults[0])
            assertNotNull(savedFlowState)
            assertEquals("savedValue", savedFlowState?.getString("savedKey"))

            sender.finish()
        }
}

internal object FragmentRuntimePermissionWorkflow {
    private val workflowRegistry = WorkflowRegistry.of(Fragment::class)

    fun start(fragment: Fragment) =
        requestRuntimePermissionImpl(
            sender = fragment,
            permissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            flowState = bundleOf(Pair("savedKey", "savedValue"))
        )

    private val requestRuntimePermissionImpl =
        workflowRegistry.requestPermissionsAction("requestRuntimePermissionImpl") { sender, result, savedFlowState ->
            val permissions = result.permissions
            val grantResults = result.granted

            assertEquals(1, permissions.size)
            assertEquals(grantResults.size, permissions.size)
            assertEquals(Manifest.permission.WRITE_EXTERNAL_STORAGE, permissions[0])
            assertEquals(PackageManager.PERMISSION_GRANTED, grantResults[0])
            assertNotNull(savedFlowState)
            assertEquals("savedValue", savedFlowState?.getString("savedKey"))

            sender.requireActivity().finish()
        }
}