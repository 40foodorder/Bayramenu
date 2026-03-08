package com.bayramenu.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.FirebaseRestaurantRepository
import com.bayramenu.shared.model.Restaurant
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private val repository = FirebaseRestaurantRepository()
    private val adapter = RestaurantAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvRestaurants = findViewById<RecyclerView>(R.id.rvRestaurants)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        
        rvRestaurants.layoutManager = LinearLayoutManager(this)
        rvRestaurants.adapter = adapter

        lifecycleScope.launch {
            try {
                // We added the explicit type : List<Restaurant> here
                repository.getRestaurantsStream().collect { restaurants: List<Restaurant> ->
                    progressBar.visibility = View.GONE
                    if (restaurants.isEmpty()) {
                        Toast.makeText(this@MainActivity, "No restaurants found!", Toast.LENGTH_SHORT).show()
                    } else {
                        adapter.submitList(restaurants)
                    }
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("Bayramenu", "Error fetching data", e)
                Toast.makeText(this@MainActivity, "Error: \${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
