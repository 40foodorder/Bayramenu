package com.bayramenu.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.model.Restaurant

class RestaurantAdapter(private val onClick: (Restaurant) -> Unit) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {
    private val items = mutableListOf<Restaurant>()
    fun submitList(newItems: List<Restaurant>) { items.clear(); items.addAll(newItems); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RestaurantViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false))
    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val rest = items[position]
        holder.bind(rest)
        holder.itemView.setOnClickListener { onClick(rest) }
    }
    override fun getItemCount() = items.size
    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(restaurant: Restaurant) {
            itemView.findViewById<TextView>(R.id.tvName).text = restaurant.name
            itemView.findViewById<TextView>(R.id.tvAddress).text = restaurant.address
            val statusView = itemView.findViewById<TextView>(R.id.tvStatus)
            statusView.text = if (restaurant.isOpen) "● Open" else "● Closed"
            statusView.setTextColor(if (restaurant.isOpen) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        }
    }
}
