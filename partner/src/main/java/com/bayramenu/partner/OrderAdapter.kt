package com.bayramenu.partner
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.model.*

class OrderAdapter(private val onAction: (Order, OrderStatus) -> Unit) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {
    private val orders = mutableListOf<Order>()
    fun updateOrders(newOrders: List<Order>) { orders.clear(); orders.addAll(newOrders); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OrderViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false))
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
        holder.itemView.setOnClickListener {
            val nextStatus = when(order.status) {
                OrderStatus.PENDING -> OrderStatus.ACCEPTED
                OrderStatus.ACCEPTED -> OrderStatus.PREPARING
                OrderStatus.PREPARING -> OrderStatus.DELIVERED
                else -> order.status
            }
            onAction(order, nextStatus)
        }
    }
    override fun getItemCount() = orders.size
    class OrderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(order: Order) {
            itemView.findViewById<TextView>(android.R.id.text1).text = "${order.customerName} - ${order.status}"
            itemView.findViewById<TextView>(android.R.id.text2).text = "Current Status: ${order.status.name}. Tap to advance."
        }
    }
}
