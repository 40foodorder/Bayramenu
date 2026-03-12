package com.bayramenu.shared.model
data class Restaurant(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val imageUrl: String = "",
    val rating: Float = 4.0f,
    val isOpen: Boolean = true,
    val category: String = "All",
    val lat: Double = 6.0206,
    val lng: Double = 37.5534
)
