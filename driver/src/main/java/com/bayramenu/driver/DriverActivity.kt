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
    private var activeOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "BayraPrestige_v230"
        setContentView(R.layout.activity_driver)

        map = findViewById(R.id.map)
        val sw = findViewById<SwitchMaterial>(R.id.swOnline)
        val tvE = findViewById<TextView>(R.id.tvEarnings)
        
        map?.controller?.setZoom(15.0)
        map?.controller?.setCenter(GeoPoint(6.0206, 37.5534))

        // TACTICAL CHECK: Ensure UID exists
        val uid = userRepo.getCurrentUserId()
        if (uid == null) {
            Toast.makeText(this, "ERROR: Driver Not Logged In!", Toast.LENGTH_LONG).show()
            sw.isEnabled = false
        }

        sw.setOnCheckedChangeListener { _, isChecked ->
            val currentUid = uid ?: return@setOnCheckedChangeListener
            
            lifecycleScope.launch {
                try {
                    // 1. Update Status in Cloud
                    userRepo.updateDriverStatus(currentUid, isChecked)
                    
                    if (isChecked) {
                        Toast.makeText(this@DriverActivity, "BEACON ONLINE", Toast.LENGTH_SHORT).show()
                        orderRepo.listenForAvailableDeliveries { orders ->
                            runOnUiThread { drawRadarPins(orders) }
                        }
                    } else {
                        Toast.makeText(this@DriverActivity, "OFFLINE", Toast.LENGTH_SHORT).show()
                        map?.overlays?.clear()
                        map?.invalidate()
                    }
                } catch (e: Exception) {
                    sw.isChecked = false
                    Toast.makeText(this@DriverActivity, "Database Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Live Earnings Stream with Error Catching
        if (uid != null) {
            lifecycleScope.launch {
                orderRepo.getDriverEarningsStream(uid)
                    .catch { e -> android.util.Log.e("DriverLog", "Earnings Error", e) }
                    .collect { total ->
                        tvE.text = "$total ETB"
                    }
            }
        }
    }

    private fun drawRadarPins(orders: List<Order>) {
        map?.overlays?.clear()
        orders.forEach { order ->
            val m = Marker(map).apply {
                position = GeoPoint(order.restaurantLat, order.restaurantLng)
                title = "Order: ${order.totalAmount} ETB"
                setOnMarkerClickListener { _, _ ->
                    activeOrder = order
                    findViewById<View>(R.id.llControls).visibility = View.VISIBLE
                    true
                }
            }
            map?.overlays?.add(m)
        }
        map?.invalidate()
    }

    override fun onResume() { super.onResume(); map?.onResume() }
    override fun onPause() { super.onPause(); map?.onPause() }
}
