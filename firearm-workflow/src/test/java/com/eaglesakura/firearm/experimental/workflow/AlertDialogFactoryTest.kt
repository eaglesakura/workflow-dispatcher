package com.eaglesakura.firearm.experimental.workflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eaglesakura.firearm.experimental.workflow.dialog.AlertDialogFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertDialogFactoryTest {

    @Test
    fun build() {
        val factory = AlertDialogFactory.Builder().apply {
            title = "title"
            message = "message"
            cancelable = false
            positiveButton = "positiveButton"
            negativeButton = "negativeButton"
            neutralButton = "neutralButton"
        }.build()

        assertEquals("title", factory.title)
        assertEquals("message", factory.message)
        assertEquals("positiveButton", factory.positiveButton)
        assertEquals("negativeButton", factory.negativeButton)
        assertEquals("neutralButton", factory.neutralButton)
        assertFalse(factory.cancelable)
    }
}