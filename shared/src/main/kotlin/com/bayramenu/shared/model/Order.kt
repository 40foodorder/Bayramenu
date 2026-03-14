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
    val status: String = "PENDING",
    val timestamp: Long = 0,
    val restaurantLat: Double = 6.0206,
    val restaurantLng: Double = 37.5534,
    val customerLat: Double = 6.0250,
    val customerLng: Double = 37.5600,
    val driverId: String? = null,
    val driverLat: Double = 0.0,
    val driverLng: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = ""
)

data class OrderItem(val foodId: String = "", val name: String = "", val price: Double = 0.0, val quantity: Int = 0)
