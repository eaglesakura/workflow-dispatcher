package com.eaglesakura.firearm.experimental.workflow.activity

import android.app.Activity
import android.content.Intent

/**
 * wrap onActivityResult() handler params.
 */
data class ActivityResult(
    /**
     * Result Value
     */
    val result: Int,

    /**
     * Result data.
     */
    val data: Intent?
) {
    /**
     * check this.result == Activity.RESULT_OK
     */
    val ok: Boolean
        get() = result == Activity.RESULT_OK

    /**
     * returns non-null intent.
     */
    fun requireIntent(): Intent =
        requireNotNull(data) {
            "data is null, result='$result'"
        }
}