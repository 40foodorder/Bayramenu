package com.bayramenu.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import com.bayramenu.shared.repository.CartManager
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.repository.UserRepository
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val userRepo = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        val restId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()
        
        findViewById<Button>(R.id.btnPay).setOnClickListener {
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val name = prefs.getString("name", "Guest") ?: "Guest"
            val phone = prefs.getString("phone", "") ?: ""

            val order = Order(
                customerId = userRepo.getCurrentUserId() ?: "guest",
                customerName = name,
                customerPhone = phone,
                restaurantId = restId,
                totalAmount = CartManager.cart.value.getTotal() + 35.0,
                status = OrderStatus.PENDING
            )

            lifecycleScope.launch {
                val orderId = orderRepo.placeOrder(order)
                CartManager.clearCart()
                val intent = Intent(this@CheckoutActivity, TrackingActivity::class.java)
                intent.putExtra("ORDER_ID", orderId)
                startActivity(intent)
                finish()
            }
        }
    }
}
