package com.bayramenu.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bayramenu.shared.model.MenuItem
import com.bayramenu.shared.model.CartItem
import com.bayramenu.shared.repository.CartManager

class MenuAdapter : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {
    private val items = mutableListOf<MenuItem>()

    fun submitList(newItems: List<MenuItem>) { items.clear(); items.addAll(newItems); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MenuViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false))
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: MenuItem) {
            itemView.findViewById<TextView>(R.id.tvMenuName).text = item.name
            itemView.findViewById<TextView>(R.id.tvMenuDesc).text = item.description
            itemView.findViewById<TextView>(R.id.tvMenuPrice).text = "${item.price} ETB"
            
            itemView.findViewById<Button>(R.id.btnAdd).setOnClickListener {
                CartManager.addItem(CartItem(item.id, item.name, item.price, 1))
                Toast.makeText(itemView.context, "Added to Cart!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
