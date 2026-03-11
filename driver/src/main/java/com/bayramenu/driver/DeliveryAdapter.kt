package com.bayramenu.driver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.model.Order

class DeliveryAdapter(private val onAccept: (Order) -> Unit) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {
    private val orders = mutableListOf<Order>()
    fun updateOrders(newOrders: List<Order>) { orders.clear(); orders.addAll(newOrders); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DeliveryViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false))
    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
        holder.itemView.setOnClickListener { onAccept(order) }
    }
    override fun getItemCount() = orders.size
    class DeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(order: Order) {
            itemView.findViewById<TextView>(android.R.id.text1).text = "Pickup: ${order.restaurantId}"
            itemView.findViewById<TextView>(android.R.id.text2).text = "Fee: ${order.deliveryFee} ETB"
        }
    }
}
