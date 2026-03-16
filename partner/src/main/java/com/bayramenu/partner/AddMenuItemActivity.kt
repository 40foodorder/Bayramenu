package com.bayramenu.partner

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bayramenu.shared.model.MenuItem
import com.bayramenu.shared.repository.MenuRepository
import kotlinx.coroutines.launch

class AddMenuItemActivity : AppCompatActivity() {
    private val menuRepository = MenuRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_menu)

        val restId = intent.getStringExtra("REST_ID") ?: ""
        val etName = findViewById<EditText>(R.id.etFoodName)
        val etPrice = findViewById<EditText>(R.id.etFoodPrice)
        val etDesc = findViewById<EditText>(R.id.etFoodDesc)
        val btnSave = findViewById<Button>(R.id.btnSaveFood)

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val desc = etDesc.text.toString()

            if (name.isEmpty() || price <= 0 || restId.isEmpty()) {
                Toast.makeText(this, "Validation Failed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    menuRepository.addMenuItem(restId, MenuItem(name = name, price = price, description = desc))
                    Toast.makeText(this@AddMenuItemActivity, "Food Added!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@AddMenuItemActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
