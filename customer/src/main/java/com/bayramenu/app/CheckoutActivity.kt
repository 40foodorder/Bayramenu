package com.bayramenu.app
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.*
import com.bayramenu.shared.repository.*
import com.bayramenu.shared.util.DistanceCalculator
import kotlinx.coroutines.launch
class CheckoutActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val restRepo = RestaurantRepository()
    private val userRepo = UserRepository()
    private var deliveryFee = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        val restId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()
        val tv = findViewById<TextView>(R.id.tvOrderDetails)
        lifecycleScope.launch {
            val hotel = restRepo.getRestaurantById(restId) ?: return@launch
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val uLat = 6.0250; val uLng = 37.5600 // In prod, use real GPS from Phase 21
            val dist = DistanceCalculator.calculateDistanceKm(uLat, uLng, hotel.lat, hotel.lng)
            deliveryFee = DistanceCalculator.calculateDeliveryFee(dist)
            val total = CartManager.cart.value.getTotal() + deliveryFee
            tv.text = "Distance: ${"%.2f".format(dist)} KM\nFee: ${"%.2f".format(deliveryFee)} ETB\nTotal: ${"%.2f".format(total)} ETB"
            findViewById<Button>(R.id.btnPay).setOnClickListener {
                lifecycleScope.launch {
                    val order = Order(
                        customerId = userRepo.getCurrentUserId() ?: "guest",
                        customerName = prefs.getString("name", "Guest") ?: "Guest",
                        customerPhone = prefs.getString("phone", "") ?: "",
                        restaurantId = restId, totalAmount = total, deliveryFee = deliveryFee,
                        restaurantLat = hotel.lat, restaurantLng = hotel.lng,
                        status = OrderStatus.PENDING
                    )
                    val orderId = orderRepo.placeOrder(order)
                    CartManager.clearCart()
                    startActivity(Intent(this@CheckoutActivity, TrackingActivity::class.java).putExtra("ORDER_ID", orderId))
                    finish()
                }
            }
        }
    }
}
