package com.bayramenu.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.repository.UserRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (userRepository.getCurrentUserId() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish(); return
        }

        setContentView(R.layout.activity_login)
        val etName = findViewById<EditText>(R.id.etUserName)
        val etPhone = findViewById<EditText>(R.id.etUserPhone)
        val btn = findViewById<Button>(R.id.btnLogin)
        val pb = findViewById<ProgressBar>(R.id.pbAuth)

        btn.setOnClickListener {
            val name = etName.text.toString()
            val phone = etUserPhone.text.toString()
            if (name.isEmpty() || phone.isEmpty()) return@setOnClickListener

            pb.visibility = View.VISIBLE
            btn.isEnabled = false
            
            lifecycleScope.launch {
                try {
                    userRepository.loginAnonymously()
                    // Save identity to local storage
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("name", name).putString("phone", phone).apply()
                    
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    pb.visibility = View.GONE
                    btn.isEnabled = true
                }
            }
        }
    }
}
