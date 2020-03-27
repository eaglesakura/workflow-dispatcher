package com.eaglesakura.firearm.experimental.workflow.internal

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

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

fun supportWorkflowOwner(fragment: Fragment) {
    // Type OK
}

fun supportWorkflowOwner(activity: FragmentActivity) {
    // Type OK
}