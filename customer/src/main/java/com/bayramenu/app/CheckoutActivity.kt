package com.bayramenu.app

import android.content.Intent
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
import com.bayramenu.shared.repository.UserRepository
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    private val userRepo = UserRepository()
    private var restaurantId: String = ""
    private var totalToPay: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()
        val tvDetails = findViewById<TextView>(R.id.tvOrderDetails)
        val btnPay = findViewById<Button>(R.id.btnPay)

        val cart = CartManager.cart.value
        totalToPay = cart.getTotal() + 35.0 // Subtotal + Delivery

        tvDetails.text = "Items: ${CartManager.getItemCount()}\nTotal: $totalToPay ETB"

        btnPay.setOnClickListener {
            // Step 1: Open Chapa WebView
            val intent = Intent(this, PaymentActivity::class.java)
            // Note: In production, generate this URL via your backend/Chapa API
            intent.putExtra("CHECKOUT_URL", "https://checkout.chapa.co/checkout/payment-demo") 
            startActivityForResult(intent, 99)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99 && resultCode == RESULT_OK) {
            finalizeOrder()
        }
    }

    private fun finalizeOrder() {
        val order = Order(
            customerId = userRepo.getCurrentUserId() ?: "guest",
            restaurantId = restaurantId,
            totalAmount = totalToPay,
            status = com.bayramenu.shared.model.OrderStatus.PENDING,
            chapaTransactionId = "TXN_${System.currentTimeMillis()}"
        )
        lifecycleScope.launch {
            val orderId = orderRepository.placeOrder(order)
            CartManager.clearCart()
            val intent = Intent(this@CheckoutActivity, TrackingActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            startActivity(intent)
            finish()
        }
    }
}
