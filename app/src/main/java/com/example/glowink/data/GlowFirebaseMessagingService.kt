package com.example.glowink.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio de Firebase Cloud Messaging (FCM) para recepcionar Notificaciones PUSH
 * de Retos, Desafíos e Invitaciones a Juegos en Glowink.
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
        val title = message.notification?.title ?: message.data["title"] ?: "⚡ ¡Nuevo Desafío Glowink!"
        val body = message.notification?.body ?: message.data["body"] ?: "Te han retado a una partida neón."
        val gameId = message.data["gameId"] ?: "duel_neon"
        val chatId = message.data["chatId"] ?: ""

        NotificationHelper.showChallengeNotification(
            context = applicationContext,
            title = title,
            body = body,
            gameId = gameId,
            chatId = chatId
        )
    }
}
