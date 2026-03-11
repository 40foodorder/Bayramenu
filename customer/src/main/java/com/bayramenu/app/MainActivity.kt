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
import com.bayramenu.shared.repository.FirebaseRestaurantRepository
import com.bayramenu.shared.model.Restaurant
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val repository = FirebaseRestaurantRepository()
    private lateinit var adapter: RestaurantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvRestaurants = findViewById<RecyclerView>(R.id.rvRestaurants)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        
        adapter = RestaurantAdapter { restaurant ->
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("RESTAURANT_ID", restaurant.id)
            startActivity(intent)
        }

        rvRestaurants.layoutManager = LinearLayoutManager(this)
        rvRestaurants.adapter = adapter

        lifecycleScope.launch {
            try {
                repository.getRestaurantsStream().collect { restaurants: List<Restaurant> ->
                    progressBar.visibility = View.GONE
                    if (restaurants.isEmpty()) Toast.makeText(this@MainActivity, "No restaurants found!", Toast.LENGTH_SHORT).show()
                    else adapter.submitList(restaurants)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
            }
        }
    }
}
