package com.bayramenu.app

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.MenuRepository
import com.bayramenu.shared.repository.CartManager
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {
    private val repository = MenuRepository()
    private val adapter = MenuAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: return finish()
        
        val rvMenu = findViewById<RecyclerView>(R.id.rvMenu)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)
        
        rvMenu.layoutManager = LinearLayoutManager(this)
        rvMenu.adapter = adapter

        // Fetch Menu
        lifecycleScope.launch {
            val items = repository.getMenu(restaurantId)
            adapter.submitList(items)
        }

        // Listen to Cart changes to update Checkout button price
        lifecycleScope.launch {
            CartManager.cart.collect { cart ->
                btnCheckout.text = "Checkout: ${cart.getTotal()} ETB"
            }
        }

        btnCheckout.setOnClickListener {
            Toast.makeText(this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show()
            // Phase 8 will go here
        }
    }
}
