package com.bayramenu.shared.repository
import com.bayramenu.shared.model.Restaurant
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class RestaurantRepository {
    private val dbUrl = "https://bayraeats-default-rtdb.europe-west1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(dbUrl).getReference("restaurants")

    fun getRestaurantsStream(category: String = "All"): Flow<List<Restaurant>> = callbackFlow {
        val listener = db.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Restaurant::class.java)?.copy(id = it.key ?: "") }
                trySend(if (category == "All") list else list.filter { it.category == category })
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) { close(error.toException()) }
        })
        awaitClose { db.removeEventListener(listener) }
    }

    suspend fun createRestaurant(restaurant: Restaurant, ownerId: String) {
        db.child(ownerId).setValue(restaurant).await()
    }

    suspend fun getRestaurantByOwner(ownerId: String): Restaurant? {
        return db.child(ownerId).get().await().getValue(Restaurant::class.java)
    }

    suspend fun getRestaurantById(id: String): Restaurant? {
        return db.child(id).get().await().getValue(Restaurant::class.java)?.copy(id = id)
    }
}
