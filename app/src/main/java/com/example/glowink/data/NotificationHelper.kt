package com.example.glowink.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.glowink.MainActivity
import com.example.glowink.R

/**
 * Gestor centralizado de notificaciones push y locales para Mensajes, Retos y Desafíos en Glowink.
 */
object NotificationHelper {

    const val CHANNEL_MESSAGES_ID = "glow_messages"
    const val CHANNEL_MESSAGES_NAME = "Mensajes de Chat Glowink"

    const val CHANNEL_CHALLENGES_ID = "glow_challenges"
    const val CHANNEL_CHALLENGES_NAME = "Retos y Desafíos Glowink"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal 1: Mensajes de Chat
            val chatChannel = NotificationChannel(
                CHANNEL_MESSAGES_ID,
                CHANNEL_MESSAGES_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones en tiempo real de nuevos mensajes de chat"
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
                enableVibration(true)
            }

            // Canal 2: Retos e Invitaciones de Juego
            val gameChannel = NotificationChannel(
                CHANNEL_CHALLENGES_ID,
                CHANNEL_CHALLENGES_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de retos, partidas e invitaciones de juego"
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                enableVibration(true)
            }

            manager.createNotificationChannel(chatChannel)
            manager.createNotificationChannel(gameChannel)
        }
    }

    fun showNewMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        chatId: String = ""
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "chat_screen")
            putExtra("chatId", chatId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES_ID)
            .setSmallIcon(R.drawable.ic_launcher_glowink)
            .setContentTitle("💬 $senderName")
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notifySafely(context, notification)
    }

    fun showChallengeNotification(
        context: Context,
        title: String,
        body: String,
        gameId: String = "duel_neon",
        chatId: String = ""
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "game_challenge")
            putExtra("gameId", gameId)
            putExtra("chatId", chatId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CHALLENGES_ID)
            .setSmallIcon(R.drawable.ic_launcher_glowink)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notifySafely(context, notification)
    }

    private fun notifySafely(context: Context, notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
