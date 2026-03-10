package com.bayramenu.partner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.model.Order

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {
    private val orders = mutableListOf<Order>()
    fun updateOrders(newOrders: List<Order>) { orders.clear(); orders.addAll(newOrders); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OrderViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false))
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) = holder.bind(orders[position])
    override fun getItemCount() = orders.size
    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(order: Order) {
            view.findViewById<TextView>(android.R.id.text1).text = "Order ID: ${order.orderId}"
            view.findViewById<TextView>(android.R.id.text2).text = "Total: ${order.totalAmount} ETB"
        }
    }
}
