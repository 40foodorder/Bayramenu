package com.bayramenu.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.model.CartItem
import com.bayramenu.shared.repository.CartManager

class CartAdapter : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    private var items = listOf<CartItem>()
    fun setData(newItems: List<CartItem>) { items = newItems; notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CartViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false))
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
    class CartViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(item: CartItem) {
            itemView.findViewById<TextView>(R.id.tvCartItemName).text = item.name
            itemView.findViewById<TextView>(R.id.tvCartItemQty).text = item.quantity.toString()
            itemView.findViewById<TextView>(R.id.tvCartItemPrice).text = "${item.price * item.quantity} ETB"
            itemView.findViewById<Button>(R.id.btnPlus).setOnClickListener { CartManager.updateQuantity(item.foodId, 1) }
            itemView.findViewById<Button>(R.id.btnMinus).setOnClickListener { CartManager.updateQuantity(item.foodId, -1) }
        }
    }
}
