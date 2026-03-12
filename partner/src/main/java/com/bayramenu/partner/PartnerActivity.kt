package com.bayramenu.partner

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

class PartnerActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner)

        adapter = OrderAdapter { order -> acceptOrder(order) }
        val rv = findViewById<RecyclerView>(R.id.rvOrders)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        orderRepository.listenForOrders("RESTAURANT_ID_123") { orders -> 
            adapter.updateOrders(orders) 
        }
    }

    private fun acceptOrder(order: Order) {
        lifecycleScope.launch {
            try {
                orderRepository.updateOrderStatus(order.orderId, OrderStatus.ACCEPTED)
                Toast.makeText(this@PartnerActivity, "Order Accepted!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@PartnerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
