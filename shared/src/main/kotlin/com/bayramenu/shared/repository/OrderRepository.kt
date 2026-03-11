package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    
    suspend fun placeOrder(order: Order): String {
        val doc = firestore.collection("orders").document()
        doc.set(order.copy(orderId = doc.id)).await()
        return doc.id
    }

    fun listenForOrders(restaurantId: String, onOrdersReceived: (List<Order>) -> Unit) {
        firestore.collection("orders")
            .whereEqualTo("restaurantId", restaurantId)
            .addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                onOrdersReceived(orders)
            }
    }

    // NEW: For Drivers to see orders ready for pickup
    fun listenForAvailableDeliveries(onOrdersReceived: (List<Order>) -> Unit) {
        firestore.collection("orders")
            .whereEqualTo("status", OrderStatus.ACCEPTED.name) // Or "PREPARING"
            .addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                onOrdersReceived(orders)
            }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        firestore.collection("orders").document(orderId).update("status", newStatus.name).await()
    }
}
