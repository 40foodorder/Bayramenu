package com.bayramenu.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val webView = findViewById<WebView>(R.id.webViewChapa)
        val progressBar = findViewById<ProgressBar>(R.id.pbPayment)
        
        // MVP: Using Chapa's direct hosted checkout URL
        // In Production: You would call Chapa API to get a dynamic link
        val checkoutUrl = intent.getStringExtra("CHECKOUT_URL") ?: "https://checkout.chapa.co/"

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.contains("bayramenu://payment-success")) {
                    setResult(RESULT_OK)
                    finish()
                    return true
                }
                return false
            }
        }
        webView.loadUrl(checkoutUrl)
    }
}
