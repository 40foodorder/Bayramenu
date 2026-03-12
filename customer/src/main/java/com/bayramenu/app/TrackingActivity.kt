package com.bayramenu.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.map.MapConstants
import com.bayramenu.shared.repository.OrderRepository
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
        map.controller.setZoom(16.0)
        driverMarker = Marker(map).apply { title = "Driver Location" }
        map.overlays.add(driverMarker)

        lifecycleScope.launch {
            orderRepository.observeOrder(orderId).collect { order ->
                order?.let {
                    val pos = GeoPoint(it.driverLat, it.driverLng)
                    driverMarker.position = pos
                    map.controller.animateTo(pos)
                    findViewById<TextView>(R.id.tvStatus).text = "Status: ${it.status}"
                    map.invalidate()
                }
            }
        }
    }
}
