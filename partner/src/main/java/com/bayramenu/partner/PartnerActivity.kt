package com.bayramenu.partner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.repository.RestaurantRepository
import com.bayramenu.shared.repository.UserRepository
import kotlinx.coroutines.launch

class PartnerActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val restRepo = RestaurantRepository()
    private val userRepo = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val ownerId = userRepo.getCurrentUserId()
        if (ownerId == null) {
            lifecycleScope.launch {
                userRepo.loginAnonymously()
                checkHotelStatus()
            }
        } else {
            checkHotelStatus()
        }
    }

    private fun checkHotelStatus() {
        lifecycleScope.launch {
            val ownerId = userRepo.getCurrentUserId() ?: return@launch
            val hotel = restRepo.getRestaurantByOwner(ownerId)
            if (hotel == null) {
                startActivity(Intent(this@PartnerActivity, RegisterHotelActivity::class.java))
                finish()
            } else {
                showDashboard(ownerId)
            }
        }
    }

    private fun showDashboard(hotelId: String) {
        setContentView(R.layout.activity_partner)
        findViewById<Button>(R.id.btnGoToMenu).setOnClickListener {
            val intent = Intent(this, AddMenuItemActivity::class.java)
            intent.putExtra("REST_ID", hotelId)
            startActivity(intent)
        }
        val rv = findViewById<RecyclerView>(R.id.rvOrders)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = OrderAdapter { order -> 
            lifecycleScope.launch { orderRepo.updateOrderStatus(order.orderId, com.bayramenu.shared.model.OrderStatus.ACCEPTED) }
        }
        rv.adapter = adapter
        orderRepo.listenForOrders(hotelId) { orders -> adapter.updateOrders(orders) }
    }
}
