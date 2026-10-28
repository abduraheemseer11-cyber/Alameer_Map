package com.nextgis.mobile.device

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Device(
    @DocumentId
    var id: String = "",

    var userId: String = "",

    // بيانات الشبكة
    var networkName: String = "",
    var networkNumber: String = "",
    var networkIp: String = "",

    // بيانات الجهاز
    var routerType: String = "",
    var username: String = "root",
    var password: String = "",

    // بيانات العميل
    var clientName: String = "",
    var clientNumber: String = "",

    // الموقع
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    @ServerTimestamp
    var createdAt: Date? = null
)
