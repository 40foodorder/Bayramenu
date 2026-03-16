const express = require('express');
const axios = require('axios');
const admin = require('firebase-admin');
require('dotenv').config();

const app = express();
app.use(express.json());

try {
    admin.initializeApp({
        credential: admin.credential.cert(JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)),
    });
    console.log("Firebase Admin Initialized");
} catch (e) { console.log("Firebase Init Warning:", e.message); }

app.get('/', (req, res) => {
    res.send("<h1>Bayramenu Core: ONLINE</h1>");
});

app.post('/pay', async (req, res) => {
    const { amount, email, firstName, lastName, tx_ref } = req.body;
    console.log(`LOG: Processing payment for ${firstName} - ${amount} ETB`);

    try {
        const response = await axios.post('https://api.chapa.co/v1/transaction/initialize', {
            amount,
            currency: "ETB",
            email,
            first_name: firstName,
            last_name: lastName,
            tx_ref,
            callback_url: "https://bayramenu.onrender.com/webhook",
            return_url: "bayramenu://payment-success"
        }, {
            headers: { 
                Authorization: `Bearer ${process.env.CHAPA_SECRET_KEY.trim()}` 
            }
        });
        
        res.json(response.data);
    } catch (e) {
        // This is the CRITICAL log line
        const errorDetail = e.response ? JSON.stringify(e.response.data) : e.message;
        console.error("CHAPA API ERROR:", errorDetail);
        
        res.status(500).json({ 
            error: "Chapa Error", 
            details: errorDetail 
        });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
