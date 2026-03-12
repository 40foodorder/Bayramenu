package com.bayramenu.shared.model

data class Driver(
    val uid: String = "",
    val name: String = "",
    val vehicleType: String = "",
    val plateNumber: String = "",
    val isOnline: Boolean = true
)
