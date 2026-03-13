package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Driver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun getPhone(): String? = auth.currentUser?.phoneNumber

    suspend fun loginAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: throw Exception("Login failed")
    }

    suspend fun saveDriverProfile(driver: Driver) {
        firestore.collection("drivers").document(driver.uid).set(driver).await()
    }

    suspend fun updateDriverStatus(uid: String, online: Boolean) {
        firestore.collection("drivers").document(uid).update("isOnline", online).await()
    }

    suspend fun getDriverProfile(uid: String): Driver? {
        return firestore.collection("drivers").document(uid).get().await().toObject(Driver::class.java)
    }

    fun logout() = auth.signOut()
}
