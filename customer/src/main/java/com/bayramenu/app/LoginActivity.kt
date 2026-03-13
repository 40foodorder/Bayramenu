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
        findViewById<Button>(R.id.btnRequestAccess).setOnClickListener {
            val name = findViewById<EditText>(R.id.etRegName).text.toString()
            val phone = findViewById<EditText>(R.id.etRegPhone).text.toString()
            val email = findViewById<EditText>(R.id.etRegEmail).text.toString()
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) return@setOnClickListener
            val pin = (100000..999999).random().toString()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    vRepo.savePin(phone, pin)
                    sendTelegramSignal(name, phone, email, pin)
                    withContext(Dispatchers.Main) {
                        getSharedPreferences("user_prefs", 0).edit()
                            .putString("temp_phone", phone)
                            .putString("name", name)
                            .putString("email", email).apply()
                        startActivity(Intent(this@LoginActivity, VerificationActivity::class.java))
                    }
                } catch (e: Exception) { }
            }
        }
    }
    private fun sendTelegramSignal(name: String, phone: String, email: String, pin: String) {
        val text = "🚨 *New Registry Attempt*\n\nName: $name\nPhone: $phone\nEmail: $email\nPIN: *$pin*"
        val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text&parse_mode=Markdown"
        try { client.newCall(Request.Builder().url(url).build()).execute() } catch (e: Exception) {}
    }
}
