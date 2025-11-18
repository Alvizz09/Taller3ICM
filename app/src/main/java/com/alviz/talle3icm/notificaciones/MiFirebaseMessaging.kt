package com.alviz.talle3icm.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alviz.talle3icm.MainActivity
import com.alviz.talle3icm.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MiFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val NOTIFICATION_CHANNEL_ID = "Canal_Disponibilidad"

        const val EXTRA_USER_NAME = "extra_user_name"
        const val EXTRA_USER_LAT = "extra_user_lat"
        const val EXTRA_USER_LON = "extra_user_lon"
        const val EXTRA_USER_UID = "extra_user_uid"
        const val EXTRA_ROUTE = "route"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido de: ${message.from}")
        val data = message.data
        val userName = data["userName"] ?: "Usuario"
        val userLat = data["userLat"] ?: "0.0"
        val userLon = data["userLon"] ?: "0.0"
        val userUid = data["userUid"] ?: ""

        showNotification(
            title = "Usuario Disponible",
            message = "$userName ahora está disponible, checkealo!!",
            userName = userName,
            userUid = userUid,
            lat = userLat,
            lon = userLon
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Usuarios Disponibles",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando un usuario se pone disponible"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        title: String,
        message: String,
        userName: String,
        userUid: String,
        lat: String,
        lon: String
    ) {
        val route = "seguimiento/${Uri.encode(userName)}/$userUid"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_USER_NAME, userName)
            putExtra(EXTRA_USER_LAT, lat)
            putExtra(EXTRA_USER_LON, lon)
            putExtra(EXTRA_USER_UID, userUid)
            putExtra(EXTRA_ROUTE, route)

            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("$title 📍")
            .setContentText(message)
            .setSmallIcon(R.drawable.notificacion)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message. Toca para ver su ubicación en el mapa.")
            )
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}