package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Driver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun loginAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: throw Exception("Login failed")
    }

    // NEW: Save Driver Profile
    suspend fun saveDriverProfile(driver: Driver) {
        firestore.collection("drivers").document(driver.uid).set(driver).await()
    }

    // NEW: Check if Profile exists
    suspend fun getDriverProfile(uid: String): Driver? {
        return firestore.collection("drivers").document(uid).get().await().toObject(Driver::class.java)
    }
}
