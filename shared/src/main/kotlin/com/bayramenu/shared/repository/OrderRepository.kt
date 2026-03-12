package com.bayramenu.shared.repository
import com.bayramenu.shared.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
class OrderRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val collection = firestore.collection("orders")
    suspend fun placeOrder(order: Order): String {
        val doc = collection.document(); doc.set(order.copy(orderId = doc.id)).await(); return doc.id
    }
    fun listenForOrders(restaurantId: String, onOrdersReceived: (List<Order>) -> Unit) {
        collection.whereEqualTo("restaurantId", restaurantId)
            .addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                onOrdersReceived(orders)
            }
    }
    // DRIVER RADAR: Only show orders that are actually cooked and ready
    fun listenForAvailableDeliveries(onOrdersReceived: (List<Order>) -> Unit) {
        collection.whereEqualTo("status", OrderStatus.ACCEPTED.name) // In production, change to READY_FOR_PICKUP
            .addSnapshotListener { snapshot, _ ->
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                onOrdersReceived(orders)
            }
    }
    fun observeOrder(orderId: String): Flow<Order?> = callbackFlow {
        val sub = collection.document(orderId).addSnapshotListener { snapshot, _ -> trySend(snapshot?.toObject(Order::class.java)) }
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
    fun getDriverEarningsStream(driverId: String): Flow<Double> = callbackFlow {
        val sub = collection.whereEqualTo("driverId", driverId).whereEqualTo("status", OrderStatus.DELIVERED.name)
            .addSnapshotListener { snapshot, _ -> trySend(snapshot?.documents?.sumOf { it.getDouble("deliveryFee") ?: 0.0 } ?: 0.0) }
        awaitClose { sub.remove() }
    }
}
