package com.eaglesakura.workflowdispatcher.permission

import android.content.pm.PackageManager

/**
 * Runtime Permission value.
 */
data class RuntimePermissionResult(
    /**
     * Request permissions.
     */
    val permissions: List<String>,

    /**
     * Granted result
     */
    val granted: List<Int>
) {
    /**
     * All granted values.
     */
    val allGranted: Boolean
        get() = granted.count { it != PackageManager.PERMISSION_GRANTED } == 0
}