package com.bayramenu.app

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.repository.OrderRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOrder).setOnClickListener {
            val testOrder = Order(
                customerId = "user_123",
                restaurantId = "RESTAURANT_ID_123",
                totalAmount = 150.0
            )
            
            lifecycleScope.launch {
                try {
                    val orderId = orderRepository.placeOrder(testOrder)
                    Toast.makeText(this@MainActivity, "Order placed: $orderId", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
