@file:Suppress("unused")

package com.eaglesakura.workflowdispatcher.internal.armyknife_jetpack

import android.os.Handler
import android.os.Looper
import androidx.annotation.UiThread

/**
 * Handler for UI Thread.
 *
 * e.g.)
 * UIHandler.post {
 *      // do something on UI thread.
 * }
 *
 * @author @eaglesakura
 * @link https://github.com/eaglesakura/armyknife-jetpack
 */
private val UIHandler = Handler(Looper.getMainLooper())

/**
 * robolectric runtime is true.
 *
 * @author @eaglesakura
 * @link https://github.com/eaglesakura/armyknife-jetpack
 */
private val robolectric: Boolean = try {
    Class.forName("org.robolectric.Robolectric")
    true
} catch (err: ClassNotFoundException) {
    false
}

/**
 * Call function from UI-Thread in Android Device.
 * If you call this function from the Worker-Thread, then throw Error.
 *
 * e.g.)
 * @UiTHread
 * fun onClick() {
 *      assertUIThread()    // throw error on worker thread.
 * }
 *
 * @author @eaglesakura
 * @link https://github.com/eaglesakura/army-knife
 */
@UiThread
internal fun assertUIThread() {
    if (robolectric) {
        return
    }

    if (Thread.currentThread() != Looper.getMainLooper().thread) {
        throw Error("Thread[${Thread.currentThread()}] is not UI")
    }
}
