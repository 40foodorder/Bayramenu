package com.bayramenu.driver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.Driver
import com.bayramenu.shared.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterDriverActivity : AppCompatActivity() {
    private val userRepo = UserRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_driver)
        findViewById<Button>(R.id.btnRegisterDriver).setOnClickListener {
            val name = findViewById<EditText>(R.id.etDriverName).text.toString()
            val vehicle = findViewById<EditText>(R.id.etVehicleType).text.toString()
            val plate = findViewById<EditText>(R.id.etPlateNumber).text.toString()
            val uid = userRepo.getCurrentUserId() ?: return@setOnClickListener
            lifecycleScope.launch {
                try {
                    userRepo.saveDriverProfile(Driver(uid, name, vehicle, plate))
                    startActivity(Intent(this@RegisterDriverActivity, DriverActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterDriverActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
