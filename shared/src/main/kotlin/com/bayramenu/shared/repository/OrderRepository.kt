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

    fun listenForAvailableDeliveries(onOrdersReceived: (List<Order>) -> Unit) {
        firestore.collection("orders")
            .whereEqualTo("status", OrderStatus.ACCEPTED.name)
            .addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                onOrdersReceived(orders)
            }
    }

    // TACTICAL HANDOVER: Assign Driver and Update Status
    suspend fun claimOrder(orderId: String, driverId: String) {
        firestore.collection("orders").document(orderId).update(
            "driverId", driverId,
            "status", OrderStatus.OUT_FOR_DELIVERY.name
        ).await()
    }

    suspend fun completeDelivery(orderId: String) {
        firestore.collection("orders").document(orderId).update("status", OrderStatus.DELIVERED.name).await()
    }
}
