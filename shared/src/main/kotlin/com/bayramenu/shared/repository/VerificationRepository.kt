package com.bayramenu.shared.repository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class VerificationRepository {
    // Note: If your database is NOT in us-central1, you might need the URL in the getInstance() call
    private val db = FirebaseDatabase.getInstance().getReference("verifications")

    suspend fun savePin(phone: String, pin: String) {
        val data = mapOf(
            "pin" to pin,
            "timestamp" to System.currentTimeMillis()
        )
        // RTDB uses .child().setValue()
        db.child(phone.replace("+", "")).setValue(data).await()
    }

    suspend fun validatePin(phone: String, inputPin: String): Boolean {
        val snapshot = db.child(phone.replace("+", "")).get().await()
        if (!snapshot.exists()) return false
        
        val pin = snapshot.child("pin").getValue(String::class.java)
        val ts = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
        
        return pin == inputPin && (System.currentTimeMillis() - ts < 600000)
    }
}
