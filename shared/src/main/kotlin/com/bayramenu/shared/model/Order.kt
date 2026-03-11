package com.bayramenu.shared.model

enum class OrderStatus { PENDING, ACCEPTED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED }

data class Order(
    val orderId: String = "",
    val customerId: String = "",
    val restaurantId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val chapaTransactionId: String = "",
    val status: OrderStatus = OrderStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    // Imperial Coordinate Precision
    val restaurantLat: Double = 6.0206, // Default Arba Minch
    val restaurantLng: Double = 37.5534,
    val customerLat: Double = 0.0,
    val customerLng: Double = 0.0,
    val driverId: String? = null
)

data class OrderItem(
    val foodId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
)
