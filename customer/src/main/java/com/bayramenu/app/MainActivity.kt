package com.bayramenu.app
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.repository.RestaurantRepository
import com.bayramenu.shared.model.Restaurant
import com.google.android.material.chip.ChipGroup
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val repository = RestaurantRepository()
    private lateinit var adapter: RestaurantAdapter
    private var observationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
            adapter = RestaurantAdapter { restaurant ->
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("RESTAURANT_ID", restaurant.id)
                startActivity(intent)
            }
            findViewById<RecyclerView>(R.id.rvRestaurants)?.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                this.adapter = this@MainActivity.adapter
            }
            findViewById<View>(R.id.navHistory)?.setOnClickListener {
                startActivity(Intent(this, OrderHistoryActivity::class.java))
            }
            findViewById<ChipGroup>(R.id.chipGroup)?.setOnCheckedChangeListener { group, checkedId ->
                val chip = group.findViewById<Chip>(checkedId)
                observeRestaurants(chip?.text?.toString() ?: "All")
            }
            observeRestaurants("All")
        } catch (e: Exception) {
            Log.e("BayraCrash", "Init Error", e)
        }
    }

    private fun observeRestaurants(category: String) {
        observationJob?.cancel()
        val pb = findViewById<ProgressBar>(R.id.progressBar)
        pb?.visibility = View.VISIBLE
        observationJob = lifecycleScope.launch {
            repository.getRestaurantsStream(category)
                .catch { pb?.visibility = View.GONE }
                .collect { restaurants ->
                    pb?.visibility = View.GONE
                    adapter.submitList(restaurants)
                }
        }
    }
}
