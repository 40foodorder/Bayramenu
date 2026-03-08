package com.bayramenu.shared.model

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val imageUrl: String = "",
    val rating: Float = 0.0f,
    val isOpen: Boolean = true
)
