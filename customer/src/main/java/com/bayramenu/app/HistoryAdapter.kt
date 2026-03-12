package com.bayramenu.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.model.Order

class HistoryAdapter(private val onClick: (Order) -> Unit) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private val orders = mutableListOf<Order>()
    fun submitList(newOrders: List<Order>) { orders.clear(); orders.addAll(newOrders); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = HistoryViewHolder(
        LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
    )
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
        holder.itemView.setOnClickListener { onClick(order) }
    }
    override fun getItemCount() = orders.size
    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(order: Order) {
            itemView.findViewById<TextView>(android.R.id.text1).text = "Order #\${order.orderId.take(5)}"
            itemView.findViewById<TextView>(android.R.id.text2).text = "Status: \${order.status} | Total: \${order.totalAmount} ETB"
        }
    }
}
