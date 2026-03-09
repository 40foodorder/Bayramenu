package com.bayramenu.shared.model

data class CartItem(
    val foodId: String,
    val name: String,
    val price: Double,
    var quantity: Int
)

data class Cart(
    val items: MutableMap<String, CartItem> = mutableMapOf()
) {
    fun getTotal(): Double = items.values.sumOf { it.price * it.quantity }
}
