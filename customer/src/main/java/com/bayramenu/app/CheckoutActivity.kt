package com.bayramenu.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import com.bayramenu.shared.repository.CartManager
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.repository.UserRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CheckoutActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    private val userRepo = UserRepository()
    private var restaurantId: String = ""
    private var totalToPay: Double = 0.0
    private var userLat: Double = 6.0206 // Default
    private var userLng: Double = 37.5534

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()
        val tvDetails = findViewById<TextView>(R.id.tvOrderDetails)
        val btnPay = findViewById<Button>(R.id.btnPay)

        val cart = CartManager.cart.value
        totalToPay = cart.getTotal() + 35.0 

        tvDetails.text = "Items: ${CartManager.getItemCount()}\nTotal: $totalToPay ETB"

        // Capture GPS before payment
        captureLocation()

        btnPay.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("CHECKOUT_URL", "https://checkout.chapa.co/checkout/payment-demo") 
            startActivityForResult(intent, 99)
        }
    }

    private fun captureLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        lifecycleScope.launch {
            try {
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                location?.let {
                    userLat = it.latitude
                    userLng = it.longitude
                }
            } catch (e: Exception) { }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99 && resultCode == RESULT_OK) finalizeOrder()
    }

    private fun finalizeOrder() {
        val order = Order(
            customerId = userRepo.getCurrentUserId() ?: "guest",
            restaurantId = restaurantId,
            totalAmount = totalToPay,
            status = OrderStatus.PENDING,
            customerLat = userLat,
            customerLng = userLng,
            chapaTransactionId = "TXN_${System.currentTimeMillis()}"
        )
        lifecycleScope.launch {
            val orderId = orderRepository.placeOrder(order)
            CartManager.clearCart()
            val intent = Intent(this@CheckoutActivity, TrackingActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            startActivity(intent)
            finish()
        }
    }
}
