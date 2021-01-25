package com.eaglesakura.workflowdispatcher.internal

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow

fun <T : Any> parseSavedStateBundle(bundle: Bundle?, key: String, nullable: Boolean): T {
    try {
        return requireNotNull(bundle?.get(key) as? T?) {
            "Invalid parseSavedStateBundle[\"$key\"]"
        }
    } catch (e: NullPointerException) {
        if (nullable) {
            return null as T
        }
        throw e
    }
}

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

    fun add(self: Any, fragment: Fragment, tag: String) {
        requireFragmentManager(self).commitNow(allowStateLoss = true) {
            add(fragment, tag)
        }
    }

    fun remove(self: Fragment) {
        self.parentFragmentManager.commitNow(allowStateLoss = true) {
            remove(self)
        }
    }
}
