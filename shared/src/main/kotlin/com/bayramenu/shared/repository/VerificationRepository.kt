package com.bayramenu.shared.repository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class VerificationRepository {
    private val db = FirebaseDatabase.getInstance().getReference("verifications")

    suspend fun savePin(phone: String, pin: String) {
        // We add a 10-second timeout so it doesn't hang forever if the DB is offline
        withTimeout(10000) {
            val data = mapOf("pin" to pin, "timestamp" to System.currentTimeMillis())
            db.child(phone.replace("+", "")).setValue(data).await()
        }
    }

    suspend fun validatePin(phone: String, inputPin: String): Boolean {
        return try {
            val snapshot = db.child(phone.replace("+", "")).get().await()
            val pin = snapshot.child("pin").getValue(String::class.java)
            pin == inputPin
        } catch (e: Exception) { false }
    }
}
