package com.bayramenu.driver

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import kotlinx.coroutines.launch

class DriverActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    // Reusing the partner adapter style for speed in MVP
    private lateinit var adapter: DeliveryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        adapter = DeliveryAdapter { order -> acceptDelivery(order) }

        val rv = findViewById<RecyclerView>(R.id.rvDeliveries)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // Listen for all orders waiting for a driver
        orderRepository.listenForAvailableDeliveries { orders -> 
            adapter.updateOrders(orders) 
        }
    }

    private fun acceptDelivery(order: Order) {
        lifecycleScope.launch {
            try {
                // In Phase 11, we will attach the driverId here too
                orderRepository.updateOrderStatus(order.orderId, OrderStatus.OUT_FOR_DELIVERY)
                Toast.makeText(this@DriverActivity, "Delivery Accepted! Open Maps.", Toast.LENGTH_SHORT).show()
                // Navigation Intent will go here
            } catch (e: Exception) {
                Toast.makeText(this@DriverActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
