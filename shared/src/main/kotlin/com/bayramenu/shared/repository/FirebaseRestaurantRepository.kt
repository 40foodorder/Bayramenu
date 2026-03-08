package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Restaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRestaurantRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RestaurantRepository {

    private val collection = firestore.collection("restaurants")

    override fun getRestaurantsStream(): Flow<List<Restaurant>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val restaurants = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Restaurant::class.java)?.copy(id = doc.id)
                }
                trySend(restaurants).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun getRestaurantById(id: String): Restaurant? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Restaurant::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }
}
