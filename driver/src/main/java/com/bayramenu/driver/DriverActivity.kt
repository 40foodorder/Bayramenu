package com.bayramenu.driver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.map.MapConstants
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.repository.UserRepository
import com.bayramenu.shared.util.NavigationEngine
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DriverActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val userRepo = UserRepository()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var map: MapView? = null
    private var llControls: LinearLayout? = null
    private var btnClaim: Button? = null
    private var btnComplete: Button? = null
    private var activeOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = MapConstants.USER_AGENT
        setContentView(R.layout.activity_driver)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        map = findViewById(R.id.map); llControls = findViewById(R.id.llControls)
        btnClaim = findViewById(R.id.btnClaim); btnComplete = findViewById(R.id.btnComplete)
        val tvEarnings = findViewById<TextView>(R.id.tvEarnings)

        initMap()
        orderRepo.listenForAvailableDeliveries { orders -> drawRadarPins(orders) }

        // START EARNINGS OBSERVER
        val driverId = userRepo.getCurrentUserId() ?: "driver_guest"
        lifecycleScope.launch {
            orderRepo.getDriverEarningsStream(driverId).collect { total ->
                tvEarnings.text = "\$total ETB"
            }
        }

        btnClaim?.setOnClickListener { checkLocationPermissions() }
        
        btnComplete?.setOnClickListener {
            activeOrder?.let { order ->
                lifecycleScope.launch {
                    orderRepo.updateOrderStatus(order.orderId, OrderStatus.DELIVERED)
                    Toast.makeText(this@DriverActivity, "WALLET UPDATED", Toast.LENGTH_SHORT).show()
                    llControls?.visibility = View.GONE
                }
            }
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else { claimAndStartBeacon() }
    }

    private fun claimAndStartBeacon() {
        val driverId = userRepo.getCurrentUserId() ?: "driver_guest"
        activeOrder?.let { order ->
            lifecycleScope.launch {
                try {
                    orderRepo.claimOrder(order.orderId, driverId)
                    btnClaim?.visibility = View.GONE
                    btnComplete?.visibility = View.VISIBLE
                    startRealTimeBeacon(order.orderId)
                } catch (e: Exception) { }
            }
        }
    }

    private fun startRealTimeBeacon(orderId: String) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    lifecycleScope.launch { orderRepo.updateDriverLocation(orderId, location.latitude, location.longitude) }
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
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
                title = "Delivery"
                setOnMarkerClickListener { _, _ ->
                    activeOrder = order; llControls?.visibility = View.VISIBLE; true
                }
            }
            map?.overlays?.add(marker)
        }
        map?.invalidate()
    }

    override fun onResume() { super.onResume(); map?.onResume() }
    override fun onPause() { super.onPause(); map?.onPause() }
}
