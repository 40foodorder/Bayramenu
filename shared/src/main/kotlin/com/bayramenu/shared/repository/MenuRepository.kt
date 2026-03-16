package com.bayramenu.shared.repository
import com.bayramenu.shared.model.MenuItem
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class MenuRepository {
    private val dbUrl = "https://bayraeats-default-rtdb.europe-west1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(dbUrl).getReference("restaurants")

    suspend fun getMenu(restaurantId: String): List<MenuItem> {
        return try {
            val snapshot = db.child(restaurantId).child("menu").get().await()
            snapshot.children.mapNotNull { 
                it.getValue(MenuItem::class.java)?.copy(id = it.key ?: "") 
            }
        } catch (e: Exception) { emptyList() }
    }

    // RESTORED: This allows the Partner to save food items
    suspend fun addMenuItem(restaurantId: String, item: MenuItem) {
        db.child(restaurantId).child("menu").push().setValue(item).await()
    }
}
