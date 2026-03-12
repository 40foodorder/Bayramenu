package com.bayramenu.shared.repository
import com.bayramenu.shared.model.Restaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
class RestaurantRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val collection = firestore.collection("restaurants")
    fun getRestaurantsStream(category: String = "All"): Flow<List<Restaurant>> = callbackFlow {
        val query = if (category == "All") collection else collection.whereEqualTo("category", category)
        val sub = query.addSnapshotListener { snapshot, _ ->
            val list = snapshot?.documents?.mapNotNull { it.toObject(Restaurant::class.java)?.copy(id = it.id) } ?: emptyList()
            trySend(list)
        }
        awaitClose { sub.remove() }
    }
    suspend fun getRestaurantById(id: String): Restaurant? {
        return collection.document(id).get().await().toObject(Restaurant::class.java)?.copy(id = id)
    }
    suspend fun createRestaurant(restaurant: Restaurant, ownerId: String) {
        val data = hashMapOf(
            "name" to restaurant.name, "address" to restaurant.address, "isOpen" to restaurant.isOpen,
            "ownerId" to ownerId, "rating" to 4.0, "category" to restaurant.category,
            "lat" to restaurant.lat, "lng" to restaurant.lng
        )
        collection.document(ownerId).set(data).await()
    }
    suspend fun getRestaurantByOwner(ownerId: String): Restaurant? {
        val doc = collection.document(ownerId).get().await()
        return if (doc.exists()) doc.toObject(Restaurant::class.java)?.copy(id = doc.id) else null
    }
}
