package com.nextgis.mobile.device

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DeviceRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection get() = firestore.collection(COLLECTION_NAME)

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun addDevice(
        device: Device,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = currentUserId
        if (userId == null) {
            onError("يجب تسجيل الدخول أولاً")
            return
        }

        val deviceToSave = device.copy(userId = userId)
        collection.add(deviceToSave)
            .addOnSuccessListener { docRef -> onSuccess(docRef.id) }
            .addOnFailureListener { e -> onError(e.message ?: "خطأ غير معروف") }
    }

    fun getMyDevices(
        onSuccess: (List<Device>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = currentUserId
        if (userId == null) {
            onError("يجب تسجيل الدخول أولاً")
            return
        }

        collection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val devices = snapshot.toObjects(Device::class.java)
                    .sortedByDescending { it.createdAt?.time ?: 0L }
                onSuccess(devices)
            }
            .addOnFailureListener { e -> onError(e.message ?: "خطأ غير معروف") }
    }

    fun listenToMyDevices(
        onDevices: (List<Device>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val userId = currentUserId
        if (userId == null) {
            onError("يجب تسجيل الدخول أولاً")
            return null
        }

        return collection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e.message ?: "خطأ غير معروف")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val devices = snapshot.toObjects(Device::class.java)
                        .sortedByDescending { it.createdAt?.time ?: 0L }
                    onDevices(devices)
                }
            }
    }

    fun updateDevice(
        device: Device,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (device.id.isEmpty()) {
            onError("معرف الجهاز مفقود")
            return
        }
        if (currentUserId == null) {
            onError("يجب تسجيل الدخول أولاً")
            return
        }

        collection.document(device.id)
            .set(device)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "خطأ غير معروف") }
    }

    fun deleteDevice(
        deviceId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (currentUserId == null) {
            onError("يجب تسجيل الدخول أولاً")
            return
        }

        collection.document(deviceId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "خطأ غير معروف") }
    }

    companion object {
        private const val COLLECTION_NAME = "devices"
    }
}
