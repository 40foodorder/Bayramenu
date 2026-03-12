package com.bayramenu.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.RestaurantRepository
import com.bayramenu.shared.model.Restaurant
import com.google.android.material.chip.ChipGroup
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val repository = RestaurantRepository()
    private lateinit var adapter: RestaurantAdapter
    private var observationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        adapter = RestaurantAdapter { restaurant ->
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("RESTAURANT_ID", restaurant.id)
            startActivity(intent)
        }

        findViewById<RecyclerView>(R.id.rvRestaurants).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = this@MainActivity.adapter
        }

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            val category = chip?.text?.toString() ?: "All"
            observeRestaurants(category)
        }

        observeRestaurants("All")
    }

    private fun observeRestaurants(category: String) {
        observationJob?.cancel() // Stop previous filter
        val pb = findViewById<ProgressBar>(R.id.progressBar)
        pb.visibility = View.VISIBLE
        
        observationJob = lifecycleScope.launch {
            repository.getRestaurantsStream(category).collect { restaurants ->
                pb.visibility = View.GONE
                adapter.submitList(restaurants)
            }
        }
    }
}
