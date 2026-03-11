package com.bayramenu.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.repository.UserRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Auto-login check
        if (userRepository.getCurrentUserId() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val pbAuth = findViewById<ProgressBar>(R.id.pbAuth)

        btnLogin.setOnClickListener {
            pbAuth.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            
            lifecycleScope.launch {
                try {
                    userRepository.loginAnonymously()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    pbAuth.visibility = View.GONE
                    btnLogin.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Auth Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
