package com.bayramenu.driver
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.*
import com.bayramenu.shared.repository.*
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DriverActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val userRepo = UserRepository()
    private var map: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "BayraPrestige_v230"
        setContentView(R.layout.activity_driver)
        map = findViewById(R.id.map)
        val sw = findViewById<SwitchMaterial>(R.id.swOnline)
        val tvE = findViewById<TextView>(R.id.tvEarnings)
        map?.controller?.setZoom(15.0)
        map?.controller?.setCenter(GeoPoint(6.0206, 37.5534))

        val uid = userRepo.getCurrentUserId() ?: ""
        sw.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                userRepo.updateDriverStatus(uid, isChecked)
                if (isChecked) orderRepo.listenForAvailableDeliveries { drawRadarPins(it) }
                else { map?.overlays?.clear(); map?.invalidate() }
            }
        }
        lifecycleScope.launch {
            orderRepo.getDriverEarningsStream(uid).collect { tvE.text = "$it ETB" }
        }
    }

    private fun drawRadarPins(orders: List<Order>) {
        map?.overlays?.clear()
        orders.forEach { order ->
            val m = Marker(map).apply {
                position = GeoPoint(order.restaurantLat, order.restaurantLng)
                title = "Job: ${order.totalAmount} ETB"
            }
            map?.overlays?.add(m)
        }
        map?.invalidate()
    }
}
