package com.bayramenu.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.CartManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        val restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()
        val adapter = CartAdapter()
        findViewById<RecyclerView>(R.id.rvCartItems).apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            this.adapter = adapter
        }
        lifecycleScope.launch {
            CartManager.cart.collect { cart ->
                adapter.setData(cart.items.values.toList())
                if (cart.items.isEmpty()) finish()
            }
        }
        findViewById<Button>(R.id.btnProceedCheckout).setOnClickListener {
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("RESTAURANT_ID", restaurantId)
            startActivity(intent)
        }
    }
}
