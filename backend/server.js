const express = require('express');
const axios = require('axios');
const admin = require('firebase-admin');
require('dotenv').config();

const app = express();
app.use(express.json());

// Initialize Firebase
try {
    admin.initializeApp({
        credential: admin.credential.cert(JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)),
    });
    console.log("Firebase Admin Initialized");
} catch (e) { console.log("Firebase Error:", e.message); }

// HOME PAGE (Prevents "Cannot GET /")
app.get('/', (req, res) => {
    res.send("<h1>Bayramenu Core: ONLINE</h1><p>Status: Listening for Payments</p>");
});

// CHAPA PAYMENT INITIALIZATION
app.post('/pay', async (req, res) => {
    console.log("Payment request received for:", req.body.amount);
    const { amount, email, firstName, lastName, tx_ref } = req.body;
    
    try {
        const response = await axios.post('https://api.chapa.co/v1/transaction/initialize', {
            amount, currency: "ETB", email, first_name: firstName, last_name: lastName,
            tx_ref, callback_url: "https://bayramenu.onrender.com/webhook",
            return_url: "bayramenu://payment-success"
        }, {
            headers: { Authorization: `Bearer ${process.env.CHAPA_SECRET_KEY}` }
        });
        res.json(response.data);
    } catch (e) {
        console.log("Chapa Error:", e.response ? e.response.data : e.message);
        res.status(500).json({ error: "Failed to contact Chapa" });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
