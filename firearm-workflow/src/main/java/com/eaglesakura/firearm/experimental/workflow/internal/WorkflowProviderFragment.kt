package com.eaglesakura.firearm.experimental.workflow.internal

import android.content.Intent
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.transaction
import com.eaglesakura.firearm.experimental.workflow.WorkflowRegistry

internal class WorkflowProviderFragment<T : Any> : Fragment() {

    /**
     * Owner object.
     */
    private val owner: T
        @Suppress("UNCHECKED_CAST")
        get() {
            return when {
                parentFragment != null -> requireParentFragment() as T
                else -> requireActivity() as T
            }
        }

    /**
     * Workflow state
     */
    val state: WorkflowStateHolder by lazy { WorkflowStateHolder.from(this) }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val self = this.owner
        val registry =
            WorkflowRegistry.of(self)
        registry.onRequestPermissionsResult(self, requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val self = this.owner
        val registry =
            WorkflowRegistry.of(self)
        registry.onActivityResult(self, requestCode, resultCode, data)
    }

    companion object {
        private const val FRAGMENT_TAG = "com.eaglesakura.WorkflowProviderFragment"

        @UiThread
        private fun <T : Any> fragmentOf(fragment: Fragment): WorkflowProviderFragment<T> {
            fragment.childFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                ?.also {
                    @Suppress("UNCHECKED_CAST")
                    return it as WorkflowProviderFragment<T>
                }

            val child =
                WorkflowProviderFragment<T>()
            fragment.childFragmentManager.transaction(now = true) {
                add(
                    child,
                    FRAGMENT_TAG
                )
            }
            return child
        }

        @UiThread
        private fun <T : Any> activityOf(activity: FragmentActivity): WorkflowProviderFragment<T> {
            activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                ?.also {
                    @Suppress("UNCHECKED_CAST")
                    return it as WorkflowProviderFragment<T>
                }

            val child =
                WorkflowProviderFragment<T>()
            activity.supportFragmentManager.transaction(now = true) {
                add(
                    child,
                    FRAGMENT_TAG
                )
            }
            return child
        }

        @UiThread
        fun <T : Any> of(self: T): WorkflowProviderFragment<T> {
            return when (self) {
                is Fragment -> fragmentOf(
                    self
                )
                is FragmentActivity -> activityOf(
                    self
                )
                else -> throw IllegalArgumentException("Unsupported component='${self.javaClass.name}'")
            }
        }
    }
}