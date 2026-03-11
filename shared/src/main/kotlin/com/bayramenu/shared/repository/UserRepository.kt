package com.bayramenu.shared.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class UserRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun loginAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: throw Exception("Login failed")
    }

    fun logout() = auth.signOut()
}
