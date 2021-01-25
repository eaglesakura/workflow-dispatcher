package com.eaglesakura.workflowdispatcher.internal

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import java.util.concurrent.atomic.AtomicInteger

object InternalWorkflowUtils {
    /**
     * Require receiver class.
     */
    fun requireParent(self: Fragment): Any {
        return if (self.parentFragment != null) {
            self.requireParentFragment()
        } else {
            self.requireActivity()
        }
    }

    fun requireFragmentManager(self: Any): FragmentManager {
        return when (self) {
            is FragmentActivity -> self.supportFragmentManager
            is Fragment -> self.childFragmentManager
            else -> TODO("not supported: ${self.javaClass.name}")
        }
    }

    fun show(self: Any, fragment: DialogFragment, tag: String) {
        fragment.show(requireFragmentManager(self), "$tag#${commitNumber.incrementAndGet()}")
    }

    fun add(self: Any, fragment: Fragment, tag: String) {
        requireFragmentManager(self).commitNow(allowStateLoss = true) {
            add(fragment, "$tag#${commitNumber.incrementAndGet()}")
        }
    }

    fun remove(self: Fragment) {
        self.parentFragmentManager.commit {
            remove(self)
        }
    }

    private val commitNumber = AtomicInteger()
}
