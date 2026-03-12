package com.bayramenu.driver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.map.MapConstants
import com.bayramenu.shared.model.Order
import com.bayramenu.shared.model.OrderStatus
import com.bayramenu.shared.repository.OrderRepository
import com.bayramenu.shared.repository.UserRepository
import com.google.android.gms.location.*
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
    private var activeOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = MapConstants.USER_AGENT
        setContentView(R.layout.activity_driver)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        map = findViewById(R.id.map); llControls = findViewById(R.id.llControls)
        
        initMap()
        orderRepo.listenForAvailableDeliveries { orders -> drawRadarPins(orders) }

        findViewById<Button>(R.id.btnClaim).setOnClickListener {
            checkLocationPermissions()
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            claimAndStartBeacon()
        }
    }

    private fun claimAndStartBeacon() {
        val driverId = userRepo.getCurrentUserId() ?: "driver_guest"
        activeOrder?.let { order ->
            lifecycleScope.launch {
                try {
                    orderRepo.claimOrder(order.orderId, driverId)
                    startRealTimeBeacon(order.orderId)
                    Toast.makeText(this@DriverActivity, "GPS BEACON ACTIVE", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { }
            }
        }
    }

    private fun startRealTimeBeacon(orderId: String) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    lifecycleScope.launch {
                        orderRepo.updateDriverLocation(orderId, location.latitude, location.longitude)
                    }
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
                title = "Delivery Job"
                setOnMarkerClickListener { _, _ ->
                    activeOrder = order; llControls?.visibility = View.VISIBLE; true
                }
            }
            map?.overlays?.add(marker)
        }
        map?.invalidate()
    }
}
