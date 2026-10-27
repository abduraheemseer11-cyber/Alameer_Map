package com.nextgis.mobile.activity

import android.app.Activity
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : Activity() {

    private var emailInput: EditText? = null
    private var passwordInput: EditText? = null
    private var loginButton: Button? = null
    private var registerButton: Button? = null
    private var googleButton: Button? = null
    private var progressBar: ProgressBar? = null
    private var errorText: TextView? = null

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager by lazy { CredentialManager.create(this) }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser != null) {
            goToMain()
            return
        }
        buildUi()
    }

    private fun buildUi() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(60, 120, 60, 60)
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        val title = TextView(this).apply {
            text = "خريطة شبكة الأمير"
            textSize = 26f
            setTextColor(Color.parseColor("#1976D2"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 10)
        }
        layout.addView(title)

        val subtitle = TextView(this).apply {
            text = "تسجيل الدخول للمتابعة"
            textSize = 14f
            setTextColor(Color.parseColor("#757575"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 50)
        }
        layout.addView(subtitle)

        emailInput = EditText(this).apply {
            hint = "البريد الإلكتروني"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setSingleLine()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 20) }
        }
        layout.addView(emailInput)

        passwordInput = EditText(this).apply {
            hint = "كلمة المرور"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 20) }
        }
        layout.addView(passwordInput)

        errorText = TextView(this).apply {
            setTextColor(Color.RED)
            gravity = Gravity.CENTER
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 15) }
        }
        layout.addView(errorText)

        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(0, 10, 0, 10)
            }
        }
        layout.addView(progressBar)

        loginButton = Button(this).apply {
            text = "تسجيل الدخول"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 20, 0, 10) }
            setOnClickListener { loginUser() }
        }
        layout.addView(loginButton)

        registerButton = Button(this).apply {
            text = "إنشاء حساب جديد"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { registerUser() }
        }
        layout.addView(registerButton)

        // زر Google
        googleButton = Button(this).apply {
            text = "تسجيل الدخول باستخدام Google"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 10, 0, 10) }
            setOnClickListener { signInWithGoogle() }
        }
        layout.addView(googleButton)

        val scrollView = ScrollView(this).apply { addView(layout) }
        setContentView(scrollView)
    }

    private fun loginUser() {
        val email = emailInput?.text?.toString()?.trim() ?: ""
        val password = passwordInput?.text?.toString()?.trim() ?: ""
        if (!validateInputs(email, password)) return
        showLoading(true); errorText?.visibility = View.GONE
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) goToMain()
                else showError(task.exception?.localizedMessage ?: "فشل تسجيل الدخول")
            }
    }

    private fun registerUser() {
        val email = emailInput?.text?.toString()?.trim() ?: ""
        val password = passwordInput?.text?.toString()?.trim() ?: ""
        if (!validateInputs(email, password)) return
        showLoading(true); errorText?.visibility = View.GONE
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) goToMain()
                else showError(task.exception?.localizedMessage ?: "فشل إنشاء الحساب")
            }
    }

    private fun signInWithGoogle() {
        showLoading(true); errorText?.visibility = View.GONE
        coroutineScope.launch {
            try {
                // ملاحظة: R.string.default_web_client_id يتم إنشاؤه تلقائياً بواسطة إضافة google-services
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(com.nextgis.mobile.R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(this@LoginActivity, request)
                handleSignInResult(result.credential)
            } catch (e: GetCredentialException) {
                showLoading(false)
                showError("Google Sign-In error: ${e.message}")
            }
        }
    }

    private fun handleSignInResult(credential: androidx.credentials.Credential) {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        showLoading(false)
                        if (task.isSuccessful) goToMain()
                        else showError(task.exception?.localizedMessage ?: "فشل تسجيل الدخول عبر Google")
                    }
            } catch (e: Exception) {
                showLoading(false)
                showError("Google token error: ${e.message}")
            }
        } else {
            showLoading(false)
            showError("نوع بيانات الاعتماد غير مدعوم")
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) { showError("الرجاء إدخال البريد الإلكتروني"); return false }
        if (TextUtils.isEmpty(password)) { showError("الرجاء إدخال كلمة المرور"); return false }
        if (password.length < 6) { showError("كلمة المرور يجب أن تكون 6 أحرف على الأقل"); return false }
        return true
    }

    private fun showLoading(loading: Boolean) {
        progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        loginButton?.isEnabled = !loading
        registerButton?.isEnabled = !loading
        googleButton?.isEnabled = !loading
    }

    private fun showError(message: String) {
        errorText?.text = message
        errorText?.visibility = View.VISIBLE
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
