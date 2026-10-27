package com.nextgis.mobile.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * شاشة تسجيل الدخول - مبنية برمجياً (بدون XML)
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // إذا كان المستخدم مسجلاً مسبقاً، انتقل مباشرة إلى الشاشة الرئيسية
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        buildUiProgrammatically()
    }

    /**
     * بناء واجهة تسجيل الدخول برمجياً
     */
    private fun buildUiProgrammatically() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(64, 200, 64, 64)
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // العنوان
        val title = TextView(this).apply {
            text = "خريطة شبكة الأمير"
            textSize = 24f
            setTextColor(Color.parseColor("#1976D2"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        // حقل البريد الإلكتروني
        emailInput = EditText(this).apply {
            hint = "البريد الإلكتروني"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 20) }
        }
        layout.addView(emailInput)

        // حقل كلمة المرور
        passwordInput = EditText(this).apply {
            hint = "كلمة المرور"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 20) }
        }
        layout.addView(passwordInput)

        // نص الأخطاء
        errorText = TextView(this).apply {
            setTextColor(Color.RED)
            gravity = Gravity.CENTER
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 20) }
        }
        layout.addView(errorText)

        // شريط التحميل
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
        layout.addView(progressBar)

        // زر تسجيل الدخول
        loginButton = Button(this).apply {
            text = "تسجيل الدخول"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 20, 0, 10) }
            setOnClickListener { loginUser() }
        }
        layout.addView(loginButton)

        // زر إنشاء حساب
        registerButton = Button(this).apply {
            text = "إنشاء حساب جديد"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { registerUser() }
        }
        layout.addView(registerButton)

        val scrollView = ScrollView(this).apply {
            addView(layout)
        }

        setContentView(scrollView)
    }

    private fun loginUser() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (!validateInputs(email, password)) return

        showLoading(true)
        errorText.visibility = View.GONE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    goToMain()
                } else {
                    showError(task.exception?.localizedMessage ?: "فشل تسجيل الدخول")
                }
            }
    }

    private fun registerUser() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (!validateInputs(email, password)) return

        showLoading(true)
        errorText.visibility = View.GONE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    goToMain()
                } else {
                    showError(task.exception?.localizedMessage ?: "فشل إنشاء الحساب")
                }
            }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            showError("الرجاء إدخال البريد الإلكتروني")
            return false
        }
        if (TextUtils.isEmpty(password)) {
            showError("الرجاء إدخال كلمة المرور")
            return false
        }
        if (password.length < 6) {
            showError("كلمة المرور يجب أن تكون 6 أحرف على الأقل")
            return false
        }
        return true
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        loginButton.isEnabled = !loading
        registerButton.isEnabled = !loading
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
