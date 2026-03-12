package com.bayramenu.partner
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.*
import com.bayramenu.shared.model.OrderStatus
import kotlinx.coroutines.launch
class PartnerActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val restRepo = RestaurantRepository()
    private val userRepo = UserRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val uid = userRepo.getCurrentUserId() ?: userRepo.loginAnonymously()
            val hotel = restRepo.getRestaurantByOwner(uid)
            if (hotel == null) {
                startActivity(Intent(this@PartnerActivity, RegisterHotelActivity::class.java))
                finish()
            } else {
                showDashboard(uid)
            }
        }
    }
    private fun showDashboard(hotelId: String) {
        setContentView(R.layout.activity_partner)
        findViewById<Button>(R.id.btnGoToMenu).setOnClickListener {
            startActivity(Intent(this, AddMenuItemActivity::class.java).putExtra("REST_ID", hotelId))
        }
        val rv = findViewById<RecyclerView>(R.id.rvOrders)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = OrderAdapter { order -> 
            lifecycleScope.launch { orderRepo.updateOrderStatus(order.orderId, OrderStatus.ACCEPTED) }
        }
        rv.adapter = adapter
        orderRepo.listenForOrders(hotelId) { orders -> adapter.updateOrders(orders) }
    }
}
