package com.bayramenu.driver

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bayramenu.shared.map.MapConstants
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.util.NavigationEngine
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DriverActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    private lateinit var map: MapView
    private lateinit var btnNavigate: FloatingActionButton
    private var activeOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = MapConstants.USER_AGENT
        setContentView(R.layout.activity_driver)
        
        btnNavigate = findViewById(R.id.btnNavigate)
        initMap()

        orderRepository.listenForAvailableDeliveries { orders ->
            drawRadarPins(orders)
        }

        btnNavigate.setOnClickListener {
            activeOrder?.let { order ->
                // Dynamic Targeting: Restaurant (Pickup) vs Customer (Destination)
                val targetLat = if (order.status == OrderStatus.ACCEPTED) order.restaurantLat else order.customerLat
                val targetLng = if (order.status == OrderStatus.ACCEPTED) order.restaurantLng else order.customerLng
                
                NavigationEngine.launchNavigation(this, targetLat, targetLng)
            }
        }
    }

    private fun initMap() {
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        map.controller.setCenter(GeoPoint(6.0206, 37.5534))
    }

    private fun drawRadarPins(orders: List<Order>) {
        map.overlays.clear()
        orders.forEach { order ->
            val marker = Marker(map)
            marker.position = GeoPoint(order.restaurantLat, order.restaurantLng)
            marker.title = "Restaurant Pickup"
            marker.setOnMarkerClickListener { _, _ ->
                activeOrder = order
                btnNavigate.visibility = View.VISIBLE
                Toast.makeText(this, "Target Locked: Order ${order.orderId}", Toast.LENGTH_SHORT).show()
                true
            }
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}
