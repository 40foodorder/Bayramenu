package com.bayramenu.app
import android.content.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.repository.UserRepository
import com.bayramenu.shared.repository.VerificationRepository
import kotlinx.coroutines.*
import okhttp3.*

class LoginActivity : AppCompatActivity() {
    private val vRepo = VerificationRepository()
    private val uRepo = UserRepository()
    private val client = OkHttpClient()
    private val botToken = "8790130934:AAGY-hz-FXWEzvukQ-qEJ_mzv0qRjKY0s3g"
    private val chatId = "5232430147" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btn = findViewById<Button>(R.id.btnLogin)
        val etName = findViewById<EditText>(R.id.etRegName)
        val etPhone = findViewById<EditText>(R.id.etRegPhone)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)

        btn.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btn.isEnabled = false
            Toast.makeText(this, "Authorizing Empire Access...", Toast.LENGTH_SHORT).show()
            
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    uRepo.loginAnonymously()
                    val pin = (100000..999999).random().toString()
                    vRepo.savePin(phone, pin)
                    
                    val text = "🚨 *Bayra Registry Alert*\n\nName: $name\nPhone: $phone\nPIN: *$pin*"
                    val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text&parse_mode=Markdown"
                    client.newCall(Request.Builder().url(url).build()).execute()

                    withContext(Dispatchers.Main) {
                        getSharedPreferences("user_prefs", 0).edit()
                            .putString("temp_phone", phone)
                            .putString("name", name)
                            .putString("email", email).apply()
                        
                        startActivity(Intent(this@LoginActivity, VerificationActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btn.isEnabled = true
                        Toast.makeText(this@LoginActivity, "Handshake Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
