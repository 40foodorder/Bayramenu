package com.bayramenu.driver
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.*
import com.bayramenu.shared.repository.*
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DriverActivity : AppCompatActivity() {
    private val orderRepo = OrderRepository()
    private val userRepo = UserRepository()
    private var map: MapView? = null
    private var llControls: View? = null
    private var activeOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "BayraPrestige_v230"
        setContentView(R.layout.activity_driver)

        map = findViewById(R.id.map)
        llControls = findViewById(R.id.llControls)
        val sw = findViewById<SwitchMaterial>(R.id.swOnline)
        val tvE = findViewById<TextView>(R.id.tvEarnings)
        
        map?.controller?.setZoom(15.0)
        map?.controller?.setCenter(GeoPoint(6.0206, 37.5534))

        val uid = userRepo.getCurrentUserId()
        if (uid == null) {
            Toast.makeText(this, "AUTHENTICATING...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch { userRepo.loginAnonymously() }
        }

        sw?.setOnCheckedChangeListener { _, isChecked ->
            val currentUid = userRepo.getCurrentUserId() ?: return@setOnCheckedChangeListener
            lifecycleScope.launch {
                try {
                    userRepo.updateDriverStatus(currentUid, isChecked)
                    if (isChecked) {
                        orderRepo.listenForAvailableDeliveries { orders ->
                            runOnUiThread { drawRadarPins(orders) }
                        }
                    } else {
                        map?.overlays?.clear()
                        map?.invalidate()
                        llControls?.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    sw.isChecked = false
                    Toast.makeText(this@DriverActivity, "Sync Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        uid?.let { id ->
            lifecycleScope.launch {
                orderRepo.getDriverEarningsStream(id)
                    .catch { }
                    .collect { total -> tvE?.text = "$total ETB" }
            }
        }
    }

    private fun drawRadarPins(orders: List<Order>) {
        val currentMap = map ?: return
        currentMap.overlays.clear()
        orders.forEach { order ->
            val m = Marker(currentMap).apply {
                position = GeoPoint(order.restaurantLat, order.restaurantLng)
                title = "Job: ${order.totalAmount} ETB"
                setOnMarkerClickListener { _, _ ->
                    activeOrder = order
                    llControls?.visibility = View.VISIBLE
                    true
                }
            }
            currentMap.overlays.add(m)
        }
        currentMap.invalidate()
    }

    override fun onResume() { super.onResume(); map?.onResume() }
    override fun onPause() { super.onPause(); map?.onPause() }
}
