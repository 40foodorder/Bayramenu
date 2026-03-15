package com.bayramenu.app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.repository.UserRepository
import com.bayramenu.shared.repository.VerificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerificationActivity : AppCompatActivity() {
    private val vRepo = VerificationRepository()
    private val uRepo = UserRepository()
    
    private var etPin: EditText? = null
    private var btnValidate: Button? = null
    private var tvTimer: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        // Initialize Views with Null-Safety
        etPin = findViewById(R.id.etPinInput)
        btnValidate = findViewById(R.id.btnValidate)
        tvTimer = findViewById(R.id.tvTimer)

        val prefs = getSharedPreferences("user_prefs", 0)
        val phone = prefs.getString("temp_phone", "") ?: ""

        // Start 10-Minute Sentry Timer
        object : CountDownTimer(600000, 1000) {
            override fun onTick(ms: Long) {
                val mins = ms / 60000
                val secs = (ms % 60000) / 1000
                tvTimer?.text = String.format("%02d:%02d", mins, secs)
            }
            override fun onFinish() {
                Toast.makeText(this@VerificationActivity, "Session Expired", Toast.LENGTH_LONG).show()
                finish()
            }
        }.start()

        btnValidate?.setOnClickListener {
            val inputPin = etPin?.text.toString().trim()
            if (inputPin.length < 6) {
                Toast.makeText(this, "Enter 6 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnValidate?.isEnabled = false
            
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val isValid = vRepo.validatePin(phone, inputPin)
                    
                    withContext(Dispatchers.Main) {
                        if (isValid) {
                            Toast.makeText(this@VerificationActivity, "ACCESS GRANTED", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@VerificationActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            btnValidate?.isEnabled = true
                            Toast.makeText(this@VerificationActivity, "INVALID PIN", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnValidate?.isEnabled = true
                        Toast.makeText(this@VerificationActivity, "Sync Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}