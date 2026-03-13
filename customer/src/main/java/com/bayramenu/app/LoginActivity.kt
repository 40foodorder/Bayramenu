package com.bayramenu.app
import android.content.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.repository.VerificationRepository
import kotlinx.coroutines.*
import okhttp3.*

class LoginActivity : AppCompatActivity() {
    private val vRepo = VerificationRepository()
    private val client = OkHttpClient()
    private val botToken = "8790130934:AAGY-hz-FXWEzvukQ-qEJ_mzv0qRjKY0s3g"
    private val chatId = "5232430147" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btn = findViewById<Button>(R.id.btnRequestAccess)
        val etName = findViewById<EditText>(R.id.etRegName)
        val etPhone = findViewById<EditText>(R.id.etRegPhone)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)

        btn.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "DEBUG: Fields are empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "DEBUG: Starting Handshake...", Toast.LENGTH_SHORT).show()
            
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val pin = (100000..999999).random().toString()
                    
                    // 1. Save to Firestore
                    vRepo.savePin(phone, pin)
                    
                    // 2. Send Telegram
                    val text = "🚨 New Registry\nName: $name\nPhone: $phone\nPIN: $pin"
                    val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute()

                    withContext(Dispatchers.Main) {
                        getSharedPreferences("user_prefs", 0).edit()
                            .putString("temp_phone", phone)
                            .putString("name", name)
                            .putString("email", email).apply()
                        
                        Toast.makeText(this@LoginActivity, "DEBUG: Opening Verification...", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, VerificationActivity::class.java)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "CRASH PREVENTED: ${e.message}", Toast.LENGTH_LONG).show()
                        android.util.Log.e("BayraError", "Error", e)
                    }
                }
            }
        }
    }
}
