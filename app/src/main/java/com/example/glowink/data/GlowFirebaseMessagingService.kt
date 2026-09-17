package com.example.glowink.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio de Firebase Cloud Messaging (FCM) para recepcionar Notificaciones PUSH
 * de Mensajes Nuevos, Retos e Invitaciones a Juegos en Glowink.
 */
class GlowFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (!uid.isNullOrBlank()) {
            FirebaseFirestore.getInstance()
                .collection("users_glowink")
                .document(uid)
                .update("fcmToken", token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"] ?: "chat_message"
        val title = message.notification?.title ?: message.data["title"] ?: "⚡ Notificación Glowink"
        val body = message.notification?.body ?: message.data["body"] ?: "Tienes una nueva interacción neón."
        val chatId = message.data["chatId"] ?: ""
        val senderName = message.data["senderName"] ?: "Amigo Glowink"
        val gameId = message.data["gameId"] ?: "duel_neon"

        when (type) {
            "game_invite", "challenge" -> {
                NotificationHelper.showChallengeNotification(
                    context = applicationContext,
                    title = if (title != "⚡ Notificación Glowink") title else "🎮 Reto de $senderName",
                    body = body,
                    gameId = gameId,
                    chatId = chatId
                )
            }
            else -> {
                NotificationHelper.showNewMessageNotification(
                    context = applicationContext,
                    senderName = senderName,
                    messageText = body,
                    chatId = chatId
                )
            }
        }
    }
}
