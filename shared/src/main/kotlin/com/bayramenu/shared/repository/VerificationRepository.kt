package com.bayramenu.shared.repository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
class VerificationRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val col = firestore.collection("verifications")
    suspend fun savePin(phone: String, pin: String) {
        col.document(phone).set(mapOf("pin" to pin, "timestamp" to System.currentTimeMillis())).await()
    }
    suspend fun validatePin(phone: String, inputPin: String): Boolean {
        val doc = col.document(phone).get().await()
        if (!doc.exists()) return false
        val pin = doc.getString("pin")
        val timestamp = doc.getLong("timestamp") ?: 0L
        return pin == inputPin && (System.currentTimeMillis() - timestamp < 600000)
    }
}
