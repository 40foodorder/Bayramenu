package com.bayramenu.app
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.repository.UserRepository
import com.bayramenu.shared.repository.VerificationRepository
import kotlinx.coroutines.launch
class VerificationActivity : AppCompatActivity() {
    private val vRepo = VerificationRepository(); private val uRepo = UserRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
        val tvTimer = findViewById<TextView>(R.id.tvTimer)
        val etPin = findViewById<EditText>(R.id.etPinInput)
        val phone = getSharedPreferences("user_prefs", 0).getString("temp_phone", "") ?: ""

        object : CountDownTimer(600000, 1000) {
            override fun onTick(ms: Long) { tvTimer.text = "${ms/60000}:${String.format("%02d", (ms%60000)/1000)}" }
            override fun onFinish() { finish() }
        }.start()

        findViewById<Button>(R.id.btnValidate).setOnClickListener {
            lifecycleScope.launch {
                if (vRepo.validatePin(phone, etPin.text.toString())) {
                    uRepo.loginAnonymously()
                    startActivity(android.content.Intent(this@VerificationActivity, MainActivity::class.java))
                    finish()
                } else { Toast.makeText(this@VerificationActivity, "INVALID PIN", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}
