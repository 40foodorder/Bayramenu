package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Restaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class RestaurantRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val collection = firestore.collection("restaurants")

    fun getRestaurantsStream(): Flow<List<Restaurant>> = callbackFlow {
        val sub = collection.addSnapshotListener { snapshot, _ ->
            val list = snapshot?.documents?.mapNotNull { it.toObject(Restaurant::class.java)?.copy(id = it.id) } ?: emptyList()
            trySend(list)
        }
        awaitClose { sub.remove() }
    }

    // NEW: Link a user to a specific restaurant
    suspend fun createRestaurant(restaurant: Restaurant, ownerId: String) {
        val data = hashMapOf(
            "name" to restaurant.name,
            "address" to restaurant.address,
            "isOpen" to restaurant.isOpen,
            "ownerId" to ownerId,
            "rating" to 4.0
        )
        collection.document(ownerId).set(data).await()
    }

    suspend fun getRestaurantByOwner(ownerId: String): Restaurant? {
        return collection.document(ownerId).get().await().toObject(Restaurant::class.java)
    }
}
