package com.bayramenu.partner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.Restaurant
import com.bayramenu.shared.repository.RestaurantRepository
import com.bayramenu.shared.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterHotelActivity : AppCompatActivity() {
    private val restRepo = RestaurantRepository()
    private val userRepo = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_hotel)

        findViewById<Button>(R.id.btnRegisterHotel).setOnClickListener {
            val name = findViewById<EditText>(R.id.etHotelName).text.toString()
            val addr = findViewById<EditText>(R.id.etHotelAddress).text.toString()
            val ownerId = userRepo.getCurrentUserId() ?: return@setOnClickListener

            lifecycleScope.launch {
                try {
                    restRepo.createRestaurant(Restaurant(name = name, address = addr), ownerId)
                    startActivity(Intent(this@RegisterHotelActivity, PartnerActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterHotelActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
