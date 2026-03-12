package com.bayramenu.shared.repository

import com.bayramenu.shared.model.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MenuRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    
    suspend fun getMenu(restaurantId: String): List<MenuItem> {
        return try {
            val snapshot = firestore.collection("restaurants").document(restaurantId).collection("menu").get().await()
            snapshot.documents.mapNotNull { it.toObject(MenuItem::class.java)?.copy(id = it.id) }
        } catch (e: Exception) { emptyList() }
    }

    // NEW: Partner capability to add food
    suspend fun addMenuItem(restaurantId: String, item: MenuItem) {
        firestore.collection("restaurants").document(restaurantId)
            .collection("menu").add(item).await()
    }
}
