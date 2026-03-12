package com.bayramenu.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.map.MapConstants
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.model.OrderStatus
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlinx.coroutines.launch

class TrackingActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    private lateinit var map: MapView
    private lateinit var driverMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = MapConstants.USER_AGENT
        setContentView(R.layout.activity_tracking)

        val orderId = intent.getStringExtra("ORDER_ID") ?: return finish()
        map = findViewById(R.id.mapTracking)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        
        map.controller.setZoom(16.0)
        driverMarker = Marker(map).apply { title = "Courier" }
        map.overlays.add(driverMarker)

        lifecycleScope.launch {
            orderRepository.observeOrder(orderId).collect { order ->
                order?.let {
                    if (it.status == OrderStatus.DELIVERED) {
                        tvStatus.text = "FOOD DELIVERED! ENJOY!"
                        tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                        tvStatus.setTextColor(android.graphics.Color.WHITE)
                    } else {
                        val pos = GeoPoint(it.driverLat, it.driverLng)
                        driverMarker.position = pos
                        map.controller.animateTo(pos)
                        tvStatus.text = "Driver Status: ${it.status}"
                        map.invalidate()
                    }
                }
            }
        }
    }
}
