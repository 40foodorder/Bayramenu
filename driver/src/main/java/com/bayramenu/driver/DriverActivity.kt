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
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DriverActivity : AppCompatActivity() {
    private val orderRepository = OrderRepository()
    private val userRepository = UserRepository()
    
    private var map: MapView? = null
    private var btnNavigate: FloatingActionButton? = null
    private var btnClaim: Button? = null
    private var llControls: LinearLayout? = null
    private var activeOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = MapConstants.USER_AGENT
        setContentView(R.layout.activity_driver)
        
        map = findViewById(R.id.map)
        btnNavigate = findViewById(R.id.btnNavigate)
        btnClaim = findViewById(R.id.btnClaim)
        llControls = findViewById(R.id.llControls)
        
        initMap()

        orderRepository.listenForAvailableDeliveries { orders -> drawRadarPins(orders) }

        btnClaim?.setOnClickListener {
            val driverId = userRepository.getCurrentUserId() ?: "unknown_driver"
            activeOrder?.let { order ->
                lifecycleScope.launch {
                    try {
                        orderRepository.claimOrder(order.orderId, driverId)
                        Toast.makeText(this@DriverActivity, "MISSION STARTED", Toast.LENGTH_SHORT).show()
                        btnClaim?.visibility = View.GONE // Hide claim after success
                    } catch (e: Exception) {
                        Toast.makeText(this@DriverActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnNavigate?.setOnClickListener {
            activeOrder?.let { order ->
                val lat = if (order.status == OrderStatus.OUT_FOR_DELIVERY) order.customerLat else order.restaurantLat
                val lng = if (order.status == OrderStatus.OUT_FOR_DELIVERY) order.customerLng else order.restaurantLng
                NavigationEngine.launchNavigation(this, lat, lng)
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
            val m = map ?: return@forEach
            val marker = Marker(m)
            marker.position = GeoPoint(order.restaurantLat, order.restaurantLng)
            marker.title = "New Job Available"
            marker.setOnMarkerClickListener { _, _ ->
                activeOrder = order
                llControls?.visibility = View.VISIBLE
                true
            }
            map?.overlays?.add(marker)
        }
        map?.invalidate()
    }

    override fun onResume() { super.onResume(); map?.onResume() }
    override fun onPause() { super.onPause(); map?.onPause() }
}
