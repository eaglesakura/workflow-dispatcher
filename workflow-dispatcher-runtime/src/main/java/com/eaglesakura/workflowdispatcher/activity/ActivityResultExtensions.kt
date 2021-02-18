package com.eaglesakura.workflowdispatcher.activity

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.result.ActivityResult

val ActivityResult.ok
    get() = this.resultCode == RESULT_OK

/**
 * returns non-null intent.
 */
fun ActivityResult.requireIntent(): Intent =
    requireNotNull(data) {
        "data is null, result='$resultCode'"
    }
