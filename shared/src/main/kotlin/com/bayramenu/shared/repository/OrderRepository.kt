package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class OrderRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val collection = firestore.collection("orders")

    suspend fun placeOrder(order: Order): String {
        val doc = collection.document()
        doc.set(order.copy(orderId = doc.id)).await()
        return doc.id
    }

    fun listenForOrders(restaurantId: String, onOrdersReceived: (List<Order>) -> Unit) {
        collection.whereEqualTo("restaurantId", restaurantId)
            .addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                onOrdersReceived(orders)
            }
    }

    fun listenForAvailableDeliveries(onOrdersReceived: (List<Order>) -> Unit) {
        collection.whereEqualTo("status", OrderStatus.ACCEPTED.name)
            .addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                onOrdersReceived(orders)
            }
    }

    // NEW: Real-time Financial Stream for Drivers
    fun getDriverEarningsStream(driverId: String): Flow<Double> = callbackFlow {
        val sub = collection.whereEqualTo("driverId", driverId)
            .whereEqualTo("status", OrderStatus.DELIVERED.name)
            .addSnapshotListener { snapshot, _ ->
                val total = snapshot?.documents?.sumOf { it.getDouble("deliveryFee") ?: 0.0 } ?: 0.0
                trySend(total)
            }
        awaitClose { sub.remove() }
    }

    fun observeOrder(orderId: String): Flow<Order?> = callbackFlow {
        val sub = collection.document(orderId).addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.toObject(Order::class.java))
        }
        awaitClose { sub.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        collection.document(orderId).update("status", newStatus.name).await()
    }

    suspend fun claimOrder(orderId: String, driverId: String) {
        collection.document(orderId).update("driverId", driverId, "status", OrderStatus.OUT_FOR_DELIVERY.name).await()
    }

    suspend fun updateDriverLocation(orderId: String, lat: Double, lng: Double) {
        collection.document(orderId).update("driverLat", lat, "driverLng", lng).await()
    }
}
