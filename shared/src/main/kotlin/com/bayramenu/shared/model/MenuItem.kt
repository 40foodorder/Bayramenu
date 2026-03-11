package com.bayramenu.shared.model

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val isAvailable: Boolean = true
)
