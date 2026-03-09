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
    fun submitList(newItems: List<Restaurant>) { items.clear(); items.addAll(newItems); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RestaurantViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false))
    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        fun bind(restaurant: Restaurant) {
            tvName.text = restaurant.name
            tvAddress.text = restaurant.address
            tvStatus.text = if (restaurant.isOpen) "● Open" else "● Closed"
            tvStatus.setTextColor(if (restaurant.isOpen) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        }
    }
}
