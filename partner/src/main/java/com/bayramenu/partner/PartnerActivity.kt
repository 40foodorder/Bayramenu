package com.bayramenu.partner
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.bayramenu.shared.repository.*
import com.bayramenu.shared.model.OrderStatus
import kotlinx.coroutines.launch

class PartnerActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val restRepo = RestaurantRepository()
    private val userRepo = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner)
        lifecycleScope.launch {
            try {
                val uid = userRepo.getCurrentUserId() ?: userRepo.loginAnonymously()
                val hotel = restRepo.getRestaurantByOwner(uid)
                if (hotel == null) {
                    startActivity(Intent(this@PartnerActivity, RegisterHotelActivity::class.java))
                    finish()
                } else {
                    setupDashboard(uid)
                }
            } catch (e: Exception) {
                Toast.makeText(this@PartnerActivity, "Auth Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDashboard(hotelId: String) {
        findViewById<Button>(R.id.btnGoToMenu).setOnClickListener {
            startActivity(Intent(this, AddMenuItemActivity::class.java).putExtra("REST_ID", hotelId))
        }
        val rv = findViewById<RecyclerView>(R.id.rvOrders)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = OrderAdapter { order, nextStatus ->
            lifecycleScope.launch { orderRepo.updateOrderStatus(order.orderId, nextStatus) }
        }
        rv.adapter = adapter
        orderRepo.listenForOrders(hotelId) { orders -> adapter.updateOrders(orders) }
    }
}
