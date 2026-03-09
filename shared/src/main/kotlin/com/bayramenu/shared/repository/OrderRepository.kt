package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    
    suspend fun placeOrder(order: Order): String {
        val doc = firestore.collection("orders").document()
        doc.set(order.copy(orderId = doc.id)).await()
        return doc.id
    }

    fun listenForOrders(restaurantId: String, onOrderReceived: (Order) -> Unit) {
        firestore.collection("orders")
            .whereEqualTo("restaurantId", restaurantId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documents?.forEach { doc ->
                    doc.toObject(Order::class.java)?.let { onOrderReceived(it) }
                }
            }
    }
}
