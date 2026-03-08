package com.bayramenu.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.model.Restaurant

class RestaurantAdapter : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    private val items = mutableListOf<Restaurant>()

    fun submitList(newItems: List<Restaurant>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(restaurant: Restaurant) {
            tvName.text = restaurant.name
            
            if (restaurant.isOpen) {
                tvStatus.text = "Open"
                tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
            } else {
                tvStatus.text = "Closed"
                tvStatus.setTextColor(Color.parseColor("#F44336")) // Red
            }
        }
    }
}
