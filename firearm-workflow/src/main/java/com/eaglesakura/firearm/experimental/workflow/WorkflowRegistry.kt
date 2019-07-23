package com.eaglesakura.firearm.experimental.workflow

import android.content.Intent
import android.os.Bundle
import com.eaglesakura.firearm.experimental.workflow.activity.ActivityResultAction
import com.eaglesakura.firearm.experimental.workflow.dialog.DialogAction
import com.eaglesakura.firearm.experimental.workflow.dialog.DialogResult
import com.eaglesakura.firearm.experimental.workflow.internal.WorkflowDialogFragment
import com.eaglesakura.firearm.experimental.workflow.internal.WorkflowProviderFragment
import com.eaglesakura.firearm.experimental.workflow.internal.WorkflowStateHolder
import com.eaglesakura.firearm.experimental.workflow.permission.RuntimePermissionAction
import kotlin.reflect.KClass

/**
 * firearm-workflow as `Future` for Android components.
 *
 * DON'T use in Fragment(or Activity) instance. Use in `object`(Companion, Global, singleton, or such else) instance.
 *
 * Use cases
 *
 *  - Show Dialog with DialogFragment
 *  - Activity for Result
 *  - Runtime permission
 *
 * e.g.)
 * class ExampleFragment: Fragment() {
 *      fun onClickButton() {
 *          ExampleFragmentWorkflow.showDialog(
 *              this,
 *              AlertDialogFactory.Builder().apply {
 *                  title = "title"
 *                  message = "Are you ready?"
 *                  positiveButton = "YES"
 *                  negativeButton = "NO"
 *              }.build()
 *      }
 * }
 *
 * internal object ExampleFragmentWorkflow {
 *      private val workflowRegistry = WorkflowRegistry.of(ExampleFragment::class)
 *
 *      val showDialog = workflowRegistry.dialogAction("check") { fragment, dialogResult, savedFlowState ->
 *          // do selected...
 *          when(dialogResult.selected) {
 *              Positive -> // do positive button.
 *              Negative -> do negative button
 *              else -> do other.
 *          }
 *      }
 * }
 */
class WorkflowRegistry<T : Any>(
    internal val clazz: KClass<T>
) {

    /**
     * map of <requestCode, HandlerFunction>
     */
    private val onActivityResultHandlers: MutableMap<Int, (sender: T, requestCode: Int, result: Int, data: Intent?) -> Unit> =
        mutableMapOf()

    /**
     * map of <requestCode, HandlerFunction>
     */
    private val onRequestPermissionsResultHandlers: MutableMap<Int, (sender: T, permissions: List<String>, grantResults: List<Int>) -> Unit> =
        mutableMapOf()

    internal fun getExtra(state: WorkflowStateHolder, flowId: Any): Bundle? {
        return state.handle.get<Bundle>("internal.workflow.$flowId")
    }

    internal fun setExtra(state: WorkflowStateHolder, flowId: Any, bundle: Bundle) {
        state.handle.set("internal.workflow.$flowId", bundle)
    }

    internal fun removeExtra(state: WorkflowStateHolder, flowId: Any) {
        state.handle.remove<Bundle>("internal.workflow.$flowId")
    }

    internal fun getProvider(owner: T): WorkflowProviderFragment<T> {
        return WorkflowProviderFragment.of(owner)
    }

    internal fun getStateHolder(owner: T) = getProvider(owner).state

    private fun assertCanCreate(id: Any, requestCode: Int) {
        require(
            !onActivityResultHandlers.contains(requestCode) &&
                    !onActivityResultHandlers.contains(requestCode)
        ) {
            "Illegal action ID='$id'"
        }
    }

    /**
     * new Dialog and receive.
     */
    fun dialogAction(
        id: Any,
        onDismissHandler: (sender: T, result: DialogResult, savedFlowState: Bundle?) -> Unit
    ): DialogAction<T> {
        val requestCode = makeRequestCode(id)
        val result = DialogAction(
            registry = this,
            id = id,
            requestCode = requestCode,
            onDismissHandler = onDismissHandler
        )

        assertCanCreate(id, requestCode)

        onActivityResultHandlers[requestCode] = { sender, _, activityResult, data ->
            val dialogResult = WorkflowDialogFragment.getResult(activityResult, data)
            result.onDismiss(sender, dialogResult)
        }
        return result
    }

    /**
     * startActivityForResult with handler.
     */
    fun activityResultAction(
        id: Any,
        onActivityResultHandler: (sender: T, result: Int, data: Intent?, savedFlowState: Bundle?) -> Unit
    ): ActivityResultAction<T> {
        val requestCode = makeRequestCode(id)
        val result = ActivityResultAction(
            registry = this,
            id = id,
            requestCode = requestCode,
            onActivityResultHandler = onActivityResultHandler
        )
        assertCanCreate(id, requestCode)
        onActivityResultHandlers[requestCode] = { sender, _, activityResult, data ->
            result.onActivityResult(sender, activityResult, data)
        }
        return result
    }

    /**
     * requestPermissions action.
     *
     * @see android.Manifest
     * @see android.content.pm.PackageManager.PERMISSION_GRANTED
     * @see android.content.pm.PackageManager.PERMISSION_DENIED
     */
    fun requestPermissionsAction(
        id: Any,
        onRequestPermissionsResultHandler: (sender: T, permissions: List<String>, grantResults: List<Int>, savedFlowState: Bundle?) -> Unit
    ): RuntimePermissionAction<T> {
        val requestCode = makeRequestCode(id)
        val result = RuntimePermissionAction(
            registry = this,
            id = id,
            requestCode = requestCode,
            onRequestPermissionsResultHandler = onRequestPermissionsResultHandler
        )
        assertCanCreate(id, requestCode)
        onRequestPermissionsResultHandlers[requestCode] = { sender, permissions, grantResults ->
            result.onRequestPermissionsResult(sender, permissions, grantResults)
        }
        return result
    }

    /**
     * Delegate onActivityResult
     */
    internal fun onActivityResult(
        sender: T,
        requestCode: Int,
        result: Int,
        data: Intent?
    ): Boolean {
        onActivityResultHandlers[requestCode]?.also { handler ->
            handler(sender, requestCode, result, data)
            return true
        }
        return false
    }

    /**
     * Delegate onRequestPermissionResult
     */
    internal fun onRequestPermissionsResult(
        sender: T,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        onRequestPermissionsResultHandlers[requestCode]?.also { handler ->
            handler(sender, permissions.toList(), grantResults.toList())
            return true
        }
        return false
    }

    companion object {
        internal fun makeRequestCode(id: Any): Int = id.toString().hashCode() and 0x0000FFFF

        private val registries: MutableMap<KClass<*>, WorkflowRegistry<*>> = mutableMapOf()

        /**
         * get WorkflowRegistry from object.
         * Type<T> should extends [Fragment or FragmentActivity].
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> of(clazz: KClass<T>): WorkflowRegistry<T> {
            synchronized(registries) {
                registries[clazz]?.also { return it as WorkflowRegistry<T> }

                val result = WorkflowRegistry(clazz = clazz)
                registries[clazz] = result
                return result
            }
        }

        /**
         * get WorkflowRegistry from object.
         * Type<T> should extends [Fragment or FragmentActivity].
         */
        fun <T : Any> of(self: T): WorkflowRegistry<T> = of(self.javaClass.kotlin)
    }
}
