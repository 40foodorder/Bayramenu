package com.bayramenu.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.repository.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OrderHistoryActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    private val userRepository = UserRepository()
    private val adapter = HistoryAdapter { order ->
        val intent = Intent(this, TrackingActivity::class.java)
        intent.putExtra("ORDER_ID", order.orderId)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val rv = findViewById<RecyclerView>(R.id.rvHistory)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val userId = userRepository.getCurrentUserId() ?: return
        lifecycleScope.launch {
            orderRepository.getMyOrders(userId).collect { orders ->
                adapter.submitList(orders)
            }
        }
    }
}
