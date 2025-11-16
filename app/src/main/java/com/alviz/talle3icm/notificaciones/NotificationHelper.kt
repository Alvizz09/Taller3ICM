package com.alviz.talle3icm.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alviz.talle3icm.MainActivity
import com.alviz.talle3icm.R
import com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.Companion.EXTRA_USER_LAT
import com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.Companion.EXTRA_USER_LON
import com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.Companion.EXTRA_USER_NAME
import com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.Companion.NOTIFICATION_CHANNEL_ID

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Usuarios disponibles",
                NotificationManager.IMPORTANCE_HIGH // IMPORTANTE: HIGH para heads-up
            ).apply {
                description = "Notificaciones cuando un usuario se pone disponible"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                // Esto hace que aparezca como heads-up
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showUserAvailableNotification(userName: String, lat: Double, lon: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_USER_NAME, userName)
            putExtra(EXTRA_USER_LAT, lat.toString())
            putExtra(EXTRA_USER_LON, lon.toString())
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Sonido predeterminado de notificación
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Usuario disponible 📍")
            .setContentText("$userName ahora está disponible")
            .setSmallIcon(R.drawable.notificacion)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Categoría mensaje
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible en lockscreen
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sonido, vibración, luces
            .setSound(defaultSoundUri) // Sonido
            // Estilo expandido para mostrar más texto
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$userName ahora está disponible. Toca para ver su ubicación en el mapa.")
            )
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}