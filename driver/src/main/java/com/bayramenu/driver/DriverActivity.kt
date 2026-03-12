package com.bayramenu.driver

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.map.MapConstants
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.repository.UserRepository
import com.bayramenu.shared.util.NavigationEngine
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DriverActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    private val userRepository = UserRepository()
    private var map: MapView? = null
    private var llControls: LinearLayout? = null
    private var btnClaim: Button? = null
    private var btnComplete: Button? = null
    private var activeOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = MapConstants.USER_AGENT
        setContentView(R.layout.activity_driver)
        
        map = findViewById(R.id.map)
        llControls = findViewById(R.id.llControls)
        btnClaim = findViewById(R.id.btnClaim)
        btnComplete = findViewById(R.id.btnComplete)
        
        initMap()
        orderRepository.listenForAvailableDeliveries { orders -> drawRadarPins(orders) }

        btnClaim?.setOnClickListener {
            val driverId = userRepository.getCurrentUserId() ?: "driver_guest"
            activeOrder?.let { order ->
                lifecycleScope.launch {
                    orderRepository.claimOrder(order.orderId, driverId)
                    btnClaim?.visibility = View.GONE
                    btnComplete?.visibility = View.VISIBLE
                    startBeacon(order.orderId)
                }
            }
        }

        btnComplete?.setOnClickListener {
            activeOrder?.let { order ->
                lifecycleScope.launch {
                    orderRepository.updateOrderStatus(order.orderId, OrderStatus.DELIVERED)
                    Toast.makeText(this@DriverActivity, "MISSION ACCOMPLISHED", Toast.LENGTH_LONG).show()
                    llControls?.visibility = View.GONE
                    map?.overlays?.clear()
                    map?.invalidate()
                }
            }
        }
    }

    private fun startBeacon(orderId: String) {
        lifecycleScope.launch {
            var simLat = 6.0206
            while(true) {
                simLat += 0.0002
                orderRepository.updateDriverLocation(orderId, simLat, 37.5534)
                delay(4000)
            }
        }
    }

    private fun initMap() {
        map?.setMultiTouchControls(true)
        map?.controller?.setZoom(15.0)
        map?.controller?.setCenter(GeoPoint(6.0206, 37.5534))
    }

    private fun drawRadarPins(orders: List<Order>) {
        map?.overlays?.clear()
        orders.forEach { order ->
            val marker = Marker(map).apply {
                position = GeoPoint(order.restaurantLat, order.restaurantLng)
                title = "Pickup available"
                setOnMarkerClickListener { _, _ ->
                    activeOrder = order
                    llControls?.visibility = View.VISIBLE
                    true
                }
            }
            map?.overlays?.add(marker)
        }
        map?.invalidate()
    }
}
