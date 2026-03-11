package com.bayramenu.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderItem
import com.bayramenu.shared.repository.CartManager
import com.bayramenu.shared.repository.OrderRepository
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {

    private val orderRepository = OrderRepository()
    private var restaurantId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()

        val tvDetails = findViewById<TextView>(R.id.tvOrderDetails)
        val btnPay = findViewById<Button>(R.id.btnPay)

        val currentCart = CartManager.cart.value
        val subtotal = currentCart.getTotal()
        
        // MVP: Hardcoded delivery fee for now (Phase 10 will add GPS calculator)
        val deliveryFee = 35.0 
        val finalTotal = subtotal + deliveryFee

        // Build Summary Text
        val summary = StringBuilder()
        currentCart.items.values.forEach { item ->
            summary.append("${item.quantity}x ${item.name} - ${item.price * item.quantity} ETB\n")
        }
        summary.append("\nSubtotal: $subtotal ETB")
        summary.append("\nDelivery: $deliveryFee ETB")
        summary.append("\n\nTotal to Pay: $finalTotal ETB")

        tvDetails.text = summary.toString()

        btnPay.setOnClickListener {
            // In Phase 8.5 we will trigger Chapa SDK here. 
            // For now, we simulate a successful payment and push the order.
            finalizeOrder(finalTotal)
        }
    }

    private fun finalizeOrder(total: Double) {
        val cartItems = CartManager.cart.value.items.values.map {
            OrderItem(foodId = it.foodId, name = it.name, price = it.price, quantity = it.quantity)
        }

        val order = Order(
            customerId = "user_demo_1", // MVP hardcoded user
            restaurantId = restaurantId,
            items = cartItems,
            totalAmount = total,
            deliveryFee = 35.0,
            chapaTransactionId = "CHAPA_SIM_999" // Simulated Payment
        )

        lifecycleScope.launch {
            try {
                val orderId = orderRepository.placeOrder(order)
                Toast.makeText(this@CheckoutActivity, "Payment Success! Order #$orderId sent to kitchen.", Toast.LENGTH_LONG).show()
                CartManager.clearCart() // Clear memory
                finish() // Return to Menu
            } catch (e: Exception) {
                Toast.makeText(this@CheckoutActivity, "Order Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
