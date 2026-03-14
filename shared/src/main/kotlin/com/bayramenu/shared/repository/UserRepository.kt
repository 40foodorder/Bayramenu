package com.bayramenu.shared.repository
import com.bayramenu.shared.model.Driver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    private val dbUrl = "https://bayraeats-default-rtdb.europe-west1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(dbUrl)

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun loginAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: throw Exception("Login failed")
    }

    suspend fun saveDriverProfile(driver: Driver) {
        db.getReference("drivers").child(driver.uid).setValue(driver).await()
    }

    suspend fun updateDriverStatus(uid: String, online: Boolean) {
        db.getReference("drivers").child(uid).child("isOnline").setValue(online).await()
    }

    suspend fun getDriverProfile(uid: String): Driver? {
        return db.getReference("drivers").child(uid).get().await().getValue(Driver::class.java)
    }

    fun logout() = auth.signOut()
}
