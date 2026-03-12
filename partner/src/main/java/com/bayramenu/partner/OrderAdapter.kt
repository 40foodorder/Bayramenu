package com.bayramenu.partner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.model.Order

class OrderAdapter(private val onAccept: (Order) -> Unit = {}) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {
    private val orders = mutableListOf<Order>()
    fun updateOrders(newOrders: List<Order>) { orders.clear(); orders.addAll(newOrders); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OrderViewHolder(
        LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
    )
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
        holder.itemView.setOnClickListener { onAccept(order) }
    }
    override fun getItemCount() = orders.size
    class OrderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(order: Order) {
            itemView.findViewById<TextView>(android.R.id.text1).text = "${order.customerName} - ${order.customerPhone}"
            itemView.findViewById<TextView>(android.R.id.text2).text = "Total: ${order.totalAmount} ETB | Status: ${order.status}"
        }
    }
}
