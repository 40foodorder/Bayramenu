package com.bayramenu.app
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
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
        val tvTotal = findViewById<TextView>(R.id.tvCartTotal)
        
        rvMenu.layoutManager = LinearLayoutManager(this)
        rvMenu.adapter = adapter

        // Load Menu with Toast reporting
        lifecycleScope.launch {
            Toast.makeText(this@MenuActivity, "Loading Menu...", Toast.LENGTH_SHORT).show()
            val items = repository.getMenu(restaurantId)
            if (items.isEmpty()) {
                Toast.makeText(this@MenuActivity, "No items found in DB!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MenuActivity, "Found ${items.size} items", Toast.LENGTH_SHORT).show()
                adapter.submitList(items)
            }
        }

        lifecycleScope.launch {
            CartManager.cart.collect { cart ->
                if (cart.items.isNotEmpty()) {
                    cvCartSummary.visibility = View.VISIBLE
                    tvTotal.text = "${cart.getTotal()} ETB"
                } else {
                    cvCartSummary.visibility = View.GONE
                }
            }
        }

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java).putExtra("RESTAURANT_ID", restaurantId))
        }
    }
}
