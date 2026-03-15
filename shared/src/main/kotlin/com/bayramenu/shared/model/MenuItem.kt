package com.bayramenu.shared.model
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MenuItem(
    val id: String = "",
    val name: String = "Unknown Food",
    val price: Double = 0.0,
    val description: String = "",
    val isAvailable: Boolean = true,
    val imageurl: String = "" // Standardized to your DB's lowercase version
)
