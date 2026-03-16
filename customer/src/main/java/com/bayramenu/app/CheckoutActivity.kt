package com.bayramenu.app
import android.content.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.*
import com.bayramenu.shared.repository.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CheckoutActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val userRepo = UserRepository()
    
    // TACTICAL FIX: 60-second timeout for slow Render wake-up
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        val restId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()
        val btnPay = findViewById<Button>(R.id.btnPay)
        val total = CartManager.cart.value.getTotal() + 35.0

        btnPay.setOnClickListener {
            btnPay.isEnabled = false
            btnPay.text = "Waking up server..."
            initializeChapa(total, restId)
        }
    }

    private fun initializeChapa(amount: Double, restId: String) {
        val prefs = getSharedPreferences("user_prefs", 0)
        val name = prefs.getString("name", "User") ?: "User"
        val email = prefs.getString("email", "test@bayramenu.com") ?: "test@bayramenu.com"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("amount", amount); put("email", email)
                    put("firstName", name); put("lastName", "Customer")
                    put("tx_ref", "TX-\${System.currentTimeMillis()}")
                }

                val request = Request.Builder()
                    .url("https://bayramenu.onrender.com/pay")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (!response.isSuccessful) throw Exception("Server Error: \${response.code}")
                    
                    val checkoutUrl = JSONObject(body!!).getJSONObject("data").getString("checkout_url")
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@CheckoutActivity, PaymentActivity::class.java)
                        intent.putExtra("CHECKOUT_URL", checkoutUrl)
                        startActivityForResult(intent, 99)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CheckoutActivity, "Error: \${e.message}", Toast.LENGTH_LONG).show()
                    findViewById<Button>(R.id.btnPay).isEnabled = true
                    findViewById<Button>(R.id.btnPay).text = "RETRY"
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99 && resultCode == RESULT_OK) finalizeOrder()
    }

    private fun finalizeOrder() {
        val restId = intent.getStringExtra("RESTAURANT_ID") ?: ""
        val total = CartManager.cart.value.getTotal() + 35.0
        lifecycleScope.launch {
            orderRepo.placeOrder(Order(
                customerId = userRepo.getCurrentUserId() ?: "guest",
                restaurantId = restId, totalAmount = total, status = "PENDING"
            ))
            CartManager.clearCart()
            finish()
        }
    }
}
