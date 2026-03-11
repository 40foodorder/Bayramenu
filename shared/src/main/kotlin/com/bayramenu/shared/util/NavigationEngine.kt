package com.bayramenu.shared.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object NavigationEngine {
    fun launchNavigation(context: Context, lat: Double, lng: Double) {
        // Primary Bridge: Google Maps App (Turn-by-Turn Mode)
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        
        // Fail-Safe check
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Secondary Bridge: Web Navigator
            val webIntent = Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving"))
            context.startActivity(webIntent)
        }
    }
}
