package com.bayramenu.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import com.bayramenu.shared.repository.CartManager
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CheckoutActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val userRepo = UserRepository()
    private val client = OkHttpClient()
    private val RENDER_URL = "https://bayramenu.onrender.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        val restId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()
        
        val tvDetails = findViewById<TextView>(R.id.tvOrderDetails)
        val btnPay = findViewById<Button>(R.id.btnPay)

        val total = CartManager.cart.value.getTotal() + 35.0
        tvDetails.text = "Order Total: $total ETB"

        btnPay.setOnClickListener {
            btnPay.isEnabled = false
            btnPay.text = "Initializing Payment..."
            initializeChapaPayment(total, restId)
        }
    }

    private fun initializeChapaPayment(amount: Double, restId: String) {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val name = prefs.getString("name", "Customer") ?: "Customer"
        val email = "customer@bayramenu.com" // Placeholder for Chapa requirements

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("amount", amount)
                    put("email", email)
                    put("firstName", name)
                    put("lastName", "User")
                    put("tx_ref", "TX-${System.currentTimeMillis()}")
                    put("orderId", "PENDING")
                }

                val request = Request.Builder()
                    .url("$RENDER_URL/pay")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    val chapaData = JSONObject(body ?: "")
                    val checkoutUrl = chapaData.getJSONObject("data").getString("checkout_url")

                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@CheckoutActivity, PaymentActivity::class.java)
                        intent.putExtra("CHECKOUT_URL", checkoutUrl)
                        startActivityForResult(intent, 99)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CheckoutActivity, "Payment Init Error", Toast.LENGTH_LONG).show()
                    findViewById<Button>(R.id.btnPay).isEnabled = true
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99 && resultCode == RESULT_OK) {
            finalizeOrder()
        }
    }

    private fun finalizeOrder() {
        val restId = intent.getStringExtra("RESTAURANT_ID") ?: ""
        val total = CartManager.cart.value.getTotal() + 35.0
        val order = Order(
            customerId = userRepo.getCurrentUserId() ?: "guest",
            restaurantId = restId,
            totalAmount = total,
            status = OrderStatus.PENDING
        )
        lifecycleScope.launch {
            val orderId = orderRepo.placeOrder(order)
            CartManager.clearCart()
            startActivity(Intent(this@CheckoutActivity, TrackingActivity::class.java).putExtra("ORDER_ID", orderId))
            finish()
        }
    }
}
