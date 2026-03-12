package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Cart
import com.bayramenu.shared.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CartManager {
    private val _cart = MutableStateFlow(Cart())
    val cart: StateFlow<Cart> = _cart

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

    fun removeItem(foodId: String) {
        val currentCart = _cart.value
        currentCart.items.remove(foodId)
        _cart.value = currentCart.copy()
    }

    fun updateQuantity(foodId: String, delta: Int) {
        val currentCart = _cart.value
        val item = currentCart.items[foodId] ?: return
        item.quantity += delta
        if (item.quantity <= 0) currentCart.items.remove(foodId)
        _cart.value = currentCart.copy()
    }

    fun clearCart() { _cart.value = Cart() }
    fun getItemCount(): Int = _cart.value.items.values.sumOf { it.quantity }
}
