package com.nextgis.mobile.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.nextgis.mobile.R

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

        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)
        progressBar = findViewById(R.id.progress_bar)
        errorText = findViewById(R.id.error_text)

        loginButton.setOnClickListener { loginUser() }
        registerButton.setOnClickListener { registerUser() }
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
