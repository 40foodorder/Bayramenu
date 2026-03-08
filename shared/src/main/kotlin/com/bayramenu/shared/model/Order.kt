package com.bayramenu.shared.model

enum class OrderStatus { PENDING, ACCEPTED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED }

data class Order(
    val orderId: String = "",
    val customerId: String = "",
    val restaurantId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

data class OrderItem(
    val foodId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
)
