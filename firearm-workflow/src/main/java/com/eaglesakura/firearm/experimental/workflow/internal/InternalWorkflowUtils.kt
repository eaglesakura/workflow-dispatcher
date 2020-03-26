package com.eaglesakura.firearm.experimental.workflow.internal

import android.os.Bundle

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