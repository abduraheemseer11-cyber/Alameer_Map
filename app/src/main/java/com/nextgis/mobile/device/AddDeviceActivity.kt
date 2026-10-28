package com.nextgis.mobile.device

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class AddDeviceActivity : Activity() {

    private lateinit var etNetworkName: EditText
    private lateinit var etNetworkNumber: EditText
    private lateinit var etNetworkIp: EditText
    private lateinit var etRouterType: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etClientName: EditText
    private lateinit var etClientPhone: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSave: Button

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val deviceRepository = DeviceRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        buildUi()
    }

    private fun buildUi() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // العنوان
        val title = TextView(this).apply {
            text = "➕ إضافة جهاز جديد"
            textSize = 22f
            setTextColor(Color.parseColor("#1976D2"))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 30)
            setTypeface(null, Typeface.BOLD)
        }
        layout.addView(title)

        // قسم بيانات الشبكة
        layout.addView(createSectionHeader("🌐 بيانات الشبكة"))
        etNetworkName = createInput("اسم الشبكة (SSID)", layout)
        etNetworkNumber = createInput("رقم الشبكة *", layout, InputType.TYPE_CLASS_NUMBER)
        etNetworkIp = createInput("IP الشبكة (مثال: 192.168.1.1)", layout)

        // قسم بيانات الجهاز
        layout.addView(createSectionHeader("📶 بيانات الجهاز"))
        etRouterType = createInput("نوع الراوتر (مثال: MikroTik)", layout)
        etUsername = createInput("اسم المستخدم", layout).apply { setText("root") }
        etPassword = createInput(
            "كلمة المرور",
            layout,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        // قسم بيانات العميل
        layout.addView(createSectionHeader("👥 بيانات العميل"))
        etClientName = createInput("اسم العميل *", layout)
        etClientPhone = createInput("رقم هاتف العميل", layout, InputType.TYPE_CLASS_PHONE)

        // معلومات الموقع
        val locationInfo = TextView(this).apply {
            val latStr = if (latitude != 0.0) String.format("%.5f", latitude) else "غير محدد"
            val lonStr = if (longitude != 0.0) String.format("%.5f", longitude) else "غير محدد"
            text = "📍 الموقع: ($latStr, $lonStr)"
            textSize = 12f
            setTextColor(Color.parseColor("#666666"))
            setPadding(0, 30, 0, 20)
            gravity = Gravity.CENTER
        }
        layout.addView(locationInfo)

        // شريط التحميل
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER_HORIZONTAL }
        }
        layout.addView(progressBar)

        // زر الحفظ
        btnSave = Button(this).apply {
            text = "💾 حفظ الجهاز"
            setBackgroundColor(Color.parseColor("#1976D2"))
            setTextColor(Color.WHITE)
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 20, 0, 0) }
            setOnClickListener { saveDevice() }
        }
        layout.addView(btnSave)

        val scrollView = ScrollView(this).apply { addView(layout) }
        setContentView(scrollView)
    }

    private fun createSectionHeader(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.parseColor("#1976D2"))
            setPadding(0, 30, 0, 10)
            setTypeface(null, Typeface.BOLD)
        }
    }

    private fun createInput(
        hint: String,
        parent: LinearLayout,
        inputType: Int = InputType.TYPE_CLASS_TEXT
    ): EditText {
        val et = EditText(this).apply {
            this.hint = hint
            this.inputType = inputType
            setSingleLine()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 15) }
        }
        parent.addView(et)
        return et
    }

    private fun saveDevice() {
        val networkNumber = etNetworkNumber.text.toString().trim()
        val clientName = etClientName.text.toString().trim()

        if (networkNumber.isBlank()) {
            Toast.makeText(this, "⚠️ رقم الشبكة مطلوب", Toast.LENGTH_SHORT).show()
            etNetworkNumber.requestFocus()
            return
        }
        if (clientName.isBlank()) {
            Toast.makeText(this, "⚠️ اسم العميل مطلوب", Toast.LENGTH_SHORT).show()
            etClientName.requestFocus()
            return
        }
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "⚠️ الموقع غير محدد. افتح الخريطة أولاً لتحديد موقعك", Toast.LENGTH_LONG).show()
            return
        }

        val device = Device(
            networkName = etNetworkName.text.toString().trim(),
            networkNumber = networkNumber,
            networkIp = etNetworkIp.text.toString().trim(),
            routerType = etRouterType.text.toString().trim(),
            username = etUsername.text.toString().trim().ifBlank { "root" },
            password = etPassword.text.toString().trim(),
            clientName = clientName,
            clientNumber = etClientPhone.text.toString().trim(),
            latitude = latitude,
            longitude = longitude
        )

        showLoading(true)
        deviceRepository.addDevice(
            device = device,
            onSuccess = { _ ->
                showLoading(false)
                Toast.makeText(this, "✅ تم حفظ الجهاز بنجاح", Toast.LENGTH_LONG).show()
                setResult(RESULT_OK)
                finish()
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "❌ خطأ: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !loading
    }
}
