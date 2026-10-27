package com.nextgis.mobile.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

/**
 * نسخة تشخيصية من شاشة تسجيل الدخول.
 * تستخدم Activity العادي (بدون AppCompat) لتجنب مشاكل الثيم.
 */
class LoginActivity : Activity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("=== onCreate started ===")

        try {
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundColor(Color.WHITE)
                setPadding(50, 50, 50, 50)
            }

            statusText = TextView(this).apply {
                text = "LOGIN SCREEN\n\nStep 1: Activity created ✓\n\nWaiting for Firebase (2s)..."
                textSize = 16f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
            }
            layout.addView(statusText)

            setContentView(layout)
            log("setContentView called successfully")

            // انتظر ثانيتين ثم اختبر Firebase (لنرى إن كانت الشاشة ظهرت)
            statusText.postDelayed({ testFirebase() }, 2000)

        } catch (e: Exception) {
            log("Exception in onCreate: ${e.message}\n${e.stackTraceToString()}")
        }
    }

    private fun testFirebase() {
        log("testFirebase started")
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            log("FirebaseAuth.getInstance() OK")

            val user = auth.currentUser
            log("currentUser.uid = ${user?.uid}")

            statusText.text = "LOGIN SCREEN\n\nStep 1: Activity ✓\nStep 2: Firebase ✓\n\nUser: ${user?.uid ?: "null (not logged in)"}\n\nFirebase works!"

        } catch (e: Exception) {
            log("Firebase exception: ${e.message}\n${e.stackTraceToString()}")
            statusText.text = "FIREBASE ERROR:\n\n${e.javaClass.simpleName}\n\n${e.message}"
        }
    }

    private fun log(msg: String) {
        try {
            val file = java.io.File(getExternalFilesDir(null), "login_debug.txt")
            file.appendText("${System.currentTimeMillis()}: $msg\n")
        } catch (e: Exception) {
            // تجاهل أخطاء السجل
        }
    }
}
