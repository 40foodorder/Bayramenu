package com.bayramenu.shared.map

import org.osmdroid.util.BoundingBox

object MapConstants {
    // Imperial Geography Lock: Horn of Africa
    val HORN_OF_AFRICA_BOUNDS = BoundingBox(18.0, 51.5, 1.5, 33.0)
    
    // User-Agent Masking
    const val USER_AGENT = "BayraPrestige_v230"
    
    // Google Tile Source URL (Roadmap)
    const val GOOGLE_ROADS_URL = "https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}"
}
