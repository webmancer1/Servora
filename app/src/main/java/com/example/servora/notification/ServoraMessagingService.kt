package com.example.servora.notification

import com.example.servora.data.model.AlertSeverity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives push alerts sent from the backend (e.g. a Cloud Function watching
 * server health). Message data keys: title, body, serverId, severity.
 * Also persists the device token in Firestore so the backend knows where to push.
 */
class ServoraMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: getString(com.example.servora.R.string.app_name)
        val body = data["body"] ?: message.notification?.body ?: return
        val serverId = data["serverId"] ?: ""
        val severity = runCatching { AlertSeverity.valueOf(data["severity"] ?: "WARNING") }
            .getOrDefault(AlertSeverity.WARNING)

        NotificationHelper.createChannels(this)
        NotificationHelper(this).showAlert(
            notificationId = (serverId + body).hashCode(),
            serverId = serverId,
            serverName = title,
            severity = severity,
            message = body,
            soundEnabled = true
        )
    }

    override fun onNewToken(token: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(user.uid)
            .set(mapOf("fcmToken" to token, "fcmTokenUpdatedAt" to System.currentTimeMillis()))
            .addOnFailureListener {
                android.util.Log.w("ServoraMessaging", "Failed to save FCM token", it)
            }
    }
}
