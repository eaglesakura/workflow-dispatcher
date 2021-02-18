package com.eaglesakura.workflowdispatcher.activity

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.ActivityResult

/**
 * result is 'RESULT_OK'.
 */
val ActivityResult.ok: Boolean
    get() = this.resultCode == RESULT_OK

@Deprecated("replace to 'resultCode'", ReplaceWith("resultCode"))
val ActivityResult.result: Int
    get() = this.resultCode

/**
 * returns non-null intent.
 */
@Deprecated("replace to 'requireData'", ReplaceWith("requireData"))
fun ActivityResult.requireIntent(): Intent =
    requireNotNull(data) {
        "data is null, result='$resultCode'"
    }

/**
 * returns non-null intent.
 */
fun ActivityResult.requireData(): Intent =
    requireNotNull(data) {
        "data is null, result='$resultCode'"
    }
