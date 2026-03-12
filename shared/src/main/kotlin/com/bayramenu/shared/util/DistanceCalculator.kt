package com.bayramenu.shared.util
import kotlin.math.*
object DistanceCalculator {
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
    fun calculateDeliveryFee(distanceKm: Double): Double {
        val baseFee = 25.0
        val perKm = 12.0
        return baseFee + (distanceKm * perKm)
    }
}
