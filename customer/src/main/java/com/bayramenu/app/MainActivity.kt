package com.bayramenu.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.RestaurantRepository
import com.bayramenu.shared.model.Restaurant
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val repository = RestaurantRepository()
    private lateinit var adapter: RestaurantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            findViewById<View>(R.id.btnHistory)?.setOnClickListener {
                startActivity(Intent(this, OrderHistoryActivity::class.java))
            }

            adapter = RestaurantAdapter { restaurant ->
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("RESTAURANT_ID", restaurant.id)
                startActivity(intent)
            }

            val rv = findViewById<RecyclerView>(R.id.rvRestaurants)
            rv?.layoutManager = LinearLayoutManager(this)
            rv?.adapter = adapter

            observeRestaurants()
        } catch (e: Exception) {
            android.util.Log.e("BayraCrash", "MainActivity error", e)
        }
    }

    private fun observeRestaurants() {
        val pb = findViewById<ProgressBar>(R.id.progressBar)
        pb?.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            repository.getRestaurantsStream("All")
                .catch { e -> 
                    pb?.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Data Error", Toast.LENGTH_SHORT).show()
                }
                .collect { restaurants ->
                    pb?.visibility = View.GONE
                    adapter.submitList(restaurants)
                }
        }
    }
}
