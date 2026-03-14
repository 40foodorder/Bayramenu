package com.bayramenu.shared.repository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class VerificationRepository {
    // TACTICAL FIX: Explicitly pointing to the Europe-West1 server
    private val dbUrl = "https://bayraeats-default-rtdb.europe-west1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(dbUrl).getReference("verifications")

    suspend fun savePin(phone: String, pin: String) {
        withTimeout(15000) {
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
