package com.bayramenu.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val orderId: String = "",
    val customerId: String = "",
    val restaurantId: String = "",
    val total: Double = 0.0,
    val status: String = "PENDING"
)
