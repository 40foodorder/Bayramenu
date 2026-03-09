package com.bayramenu.partner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.model.Order

class PartnerActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Listen for orders and explicitly type the callback
        orderRepository.listenForOrders("RESTAURANT_ID_123") { order: Order ->
            Log.d("PartnerApp", "New Order Received: ${order.orderId}")
        }
    }
}
