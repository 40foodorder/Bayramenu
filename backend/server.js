const express = require('express');
const axios = require('axios');
const admin = require('firebase-admin');
require('dotenv').config();

const app = express();
app.use(express.json());

// Initialize Firebase Admin (Using Environment Variables for security)
admin.initializeApp({
  credential: admin.credential.cert(JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)),
  databaseURL: `https://${process.env.FIREBASE_PROJECT_ID}.firebaseio.com`
});

const db = admin.firestore();

// 1. CHAPA INITIALIZE PAYMENT
app.post('/pay', async (req, res) => {
    const { amount, email, firstName, lastName, tx_ref, orderId } = req.body;
    try {
        const response = await axios.post('https://api.chapa.co/v1/transaction/initialize', {
            amount, currency: "ETB", email, first_name: firstName, last_name: lastName,
            tx_ref, callback_url: process.env.WEBHOOK_URL,
            return_url: "bayramenu://payment-success",
            "customization[title]": "Bayramenu Order",
            "meta[order_id]": orderId
        }, {
            headers: { Authorization: `Bearer ${process.env.CHAPA_SECRET_KEY}` }
        });
        res.json(response.data);
    } catch (e) { res.status(500).json({ error: e.message }); }
});

// 2. CHAPA WEBHOOK (Verifies Payment & Notifies Partner)
app.post('/webhook', async (req, res) => {
    const data = req.body;
    // Chapa sends a hash, but for MVP we check status
    if (data.status === 'success') {
        const orderId = data.tx_ref; // Assuming tx_ref is the orderId
        const orderRef = db.collection('orders').document(orderId);
        
        await orderRef.update({ status: 'ACCEPTED', chapaTransactionId: data.transaction_id });
        
        // TRIGGER NOTIFICATION TO PARTNER
        const orderDoc = await orderRef.get();
        const restaurantId = orderDoc.data().restaurantId;
        sendNotification(restaurantId, "New Order!", "You have a new paid order at the kitchen.");
    }
    res.sendStatus(200);
});

async function sendNotification(topic, title, body) {
    const message = { notification: { title, body }, topic: topic };
    try { await admin.messaging().send(message); } catch (e) { console.log("FCM Error", e); }
}

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Bayra-Core running on port ${PORT}`));
