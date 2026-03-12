package com.bayramenu.shared.model

data class PaymentRequest(
    val amount: Double,
    val currency: String = "ETB",
    val email: String,
    val firstName: String,
    val lastName: String,
    val txRef: String,
    val callbackUrl: String = "https://checkout.chapa.co/checkout/test-payment-receipt",
    val returnUrl: String = "bayramenu://payment-success"
)
