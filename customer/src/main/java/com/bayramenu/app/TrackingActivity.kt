package com.bayramenu.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.map.MapConstants
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.repository.UserRepository
import com.bayramenu.shared.model.OrderStatus
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlinx.coroutines.launch

class TrackingActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val userRepo = UserRepository()
    private lateinit var map: MapView
    private lateinit var driverMarker: Marker
    private var isDriverInfoLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = MapConstants.USER_AGENT
        setContentView(R.layout.activity_tracking)

        val orderId = intent.getStringExtra("ORDER_ID") ?: return finish()
        map = findViewById(R.id.mapTracking)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvDriverName = findViewById<TextView>(R.id.tvDriverName)
        val tvVehicle = findViewById<TextView>(R.id.tvVehicleInfo)
        val llDriverInfo = findViewById<LinearLayout>(R.id.llDriverInfo)
        val vDivider = findViewById<View>(R.id.vDivider)
        
        map.controller.setZoom(16.0)
        driverMarker = Marker(map).apply { title = "Courier" }
        map.overlays.add(driverMarker)

        lifecycleScope.launch {
            orderRepo.observeOrder(orderId).collect { order ->
                order?.let {
                    // Update Status
                    tvStatus.text = "Status: ${it.status}"

                    // Update Map Position
                    if (it.driverLat != 0.0) {
                        val pos = GeoPoint(it.driverLat, it.driverLng)
                        driverMarker.position = pos
                        map.controller.animateTo(pos)
                        map.invalidate()
                    }

                    // Fetch Driver Profile once a driver claims it
                    if (!it.driverId.isNullOrEmpty() && !isDriverInfoLoaded) {
                        val profile = userRepo.getDriverProfile(it.driverId!!)
                        profile?.let { p ->
                            tvDriverName.text = p.name
                            tvVehicle.text = "${p.vehicleType} • ${p.plateNumber}"
                            llDriverInfo.visibility = View.VISIBLE
                            vDivider.visibility = View.VISIBLE
                            isDriverInfoLoaded = true
                        }
                    }

                    // Success State
                    if (it.status == OrderStatus.DELIVERED) {
                        tvStatus.text = "DELIVERED! Enjoy your food!"
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    }
                }
            }
        }
    }
}
