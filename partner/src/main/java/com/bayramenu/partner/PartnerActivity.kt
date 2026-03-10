package com.bayramenu.partner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.model.Order

class PartnerActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    private val adapter = OrderAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner)
        val rv = findViewById<RecyclerView>(R.id.rvOrders)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        orderRepository.listenForOrders("RESTAURANT_ID_123") { order -> adapter.updateOrders(listOf(order)) }
    }
}
