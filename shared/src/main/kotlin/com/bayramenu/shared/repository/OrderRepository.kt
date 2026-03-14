package com.bayramenu.shared.repository
import com.bayramenu.shared.model.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val dbUrl = "https://bayraeats-default-rtdb.europe-west1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(dbUrl).getReference("orders")

    suspend fun placeOrder(order: Order): String {
        val key = db.push().key ?: return ""
        db.child(key).setValue(order.copy(orderId = key)).await()
        return key
    }

    fun listenForOrders(restaurantId: String, onOrdersReceived: (List<Order>) -> Unit) {
        db.orderByChild("restaurantId").equalTo(restaurantId).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                onOrdersReceived(s.children.mapNotNull { it.getValue(Order::class.java) })
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        })
    }

    fun listenForAvailableDeliveries(onOrdersReceived: (List<Order>) -> Unit) {
        db.orderByChild("status").equalTo(OrderStatus.ACCEPTED.name).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                onOrdersReceived(s.children.mapNotNull { it.getValue(Order::class.java) })
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        })
    }

    fun observeOrder(orderId: String): Flow<Order?> = callbackFlow {
        val listener = db.child(orderId).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) { trySend(s.getValue(Order::class.java)) }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        })
        awaitClose { db.child(orderId).removeEventListener(listener) }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        db.child(orderId).child("status").setValue(status).await()
    }

    suspend fun claimOrder(orderId: String, driverId: String) {
        val updates = mapOf("driverId" to driverId, "status" to OrderStatus.OUT_FOR_DELIVERY)
        db.child(orderId).updateChildren(updates).await()
    }

    suspend fun updateDriverLocation(orderId: String, lat: Double, lng: Double) {
        db.child(orderId).child("driverLat").setValue(lat)
        db.child(orderId).child("driverLng").setValue(lng).await()
    }

    fun getDriverEarningsStream(driverId: String): Flow<Double> = callbackFlow {
        val listener = db.orderByChild("driverId").equalTo(driverId).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                val total = s.children.filter { it.child("status").getValue(String::class.java) == OrderStatus.DELIVERED.name }
                             .sumOf { it.child("deliveryFee").getValue(Double::class.java) ?: 0.0 }
                trySend(total)
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        })
        awaitClose { db.removeEventListener(listener) }
    }
    
    fun getMyOrders(customerId: String): Flow<List<Order>> = callbackFlow {
        val listener = db.orderByChild("customerId").equalTo(customerId).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                trySend(s.children.mapNotNull { it.getValue(Order::class.java) }.reversed())
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        })
        awaitClose { db.removeEventListener(listener) }
    }
}
