package com.bayramenu.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.MenuRepository
import com.bayramenu.shared.repository.CartManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {
    private val repository = MenuRepository()
    private val adapter = MenuAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()
        
        val rvMenu = findViewById<RecyclerView>(R.id.rvMenu)
        val cvCartSummary = findViewById<View>(R.id.cvCartSummary)
        val tvCount = findViewById<TextView>(R.id.tvCartCount)
        val tvTotal = findViewById<TextView>(R.id.tvCartTotal)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)
        
        rvMenu.layoutManager = LinearLayoutManager(this)
        rvMenu.adapter = adapter

        lifecycleScope.launch {
            adapter.submitList(repository.getMenu(restaurantId))
        }

        lifecycleScope.launch {
            CartManager.cart.collect { cart ->
                if (cart.items.isNotEmpty()) {
                    cvCartSummary.visibility = View.VISIBLE
                    tvCount.text = "${CartManager.getItemCount()} Items"
                    tvTotal.text = "${cart.getTotal()} ETB"
                } else {
                    cvCartSummary.visibility = View.GONE
                }
            }
        }

        btnCheckout.setOnClickListener {
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("RESTAURANT_ID", restaurantId)
            startActivity(intent)
        }
    }
}
