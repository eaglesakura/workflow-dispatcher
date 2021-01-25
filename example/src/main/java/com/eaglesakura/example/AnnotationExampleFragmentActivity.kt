package com.eaglesakura.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AnnotationExampleFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(AnnotationExampleFragment(), "AnnotationExampleFragment")
                commit()
            }
        }
    }
}
