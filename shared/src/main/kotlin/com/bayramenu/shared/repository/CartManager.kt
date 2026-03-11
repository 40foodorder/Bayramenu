package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Cart
import com.bayramenu.shared.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CartManager {
    private val _cart = MutableStateFlow(Cart())
    val cart: StateFlow<Cart> = _cart

    fun clearCart() { _cart.value = Cart() }
    fun addItem(item: CartItem) {
        val currentCart = _cart.value
        val existing = currentCart.items[item.foodId]
        if (existing != null) {
            existing.quantity += 1
        } else {
            currentCart.items[item.foodId] = item
        }
        _cart.value = currentCart.copy()
    }
}
