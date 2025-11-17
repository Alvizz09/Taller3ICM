package com.alviz.talle3icm.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alviz.talle3icm.MainActivity
import com.alviz.talle3icm.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MiFirebaseMessagingService : FirebaseMessagingService() {
    //Datos para el canal dio mio de notificacion
    companion object {
        private const val TAG = "FCMService"
        const val NOTIFICATION_CHANNEL_ID = "Canal_Disponibilidad"
        const val EXTRA_USER_NAME = "extra_user_name"
        const val EXTRA_USER_LAT = "extra_user_lat"
        const val EXTRA_USER_LON = "extra_user_lon"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel() // Crea el canal de notificación
    }

    override fun onNewToken(token: String) { // es importante obtener el token poke
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token") //verificar token en consola
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        val data = message.data
        val userName = data["userName"] ?: "Usuario"
        val userLat = data["userLat"] ?: "0.0"
        val userLon = data["userLon"] ?: "0.0"

        showNotification(
            title = "Usuario Disponible",
            message = "$userName ahora está disponible, checkealo!!",
            userName = userName,
            lat = userLat,
            lon = userLon
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Usuarios Disponibles",
                NotificationManager.IMPORTANCE_HIGH //para que apareza de una vez en pantalla
            ).apply {
                description = "Notificaciones cuando un usuario se pone disponible"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String, userName: String, lat: String, lon: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_USER_NAME, userName) // Agregar el nombre del usuario
            putExtra(EXTRA_USER_LAT, lat) // Agregar latitud y longitud
            putExtra(EXTRA_USER_LON, lon) // Agregar latitud y longitud
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Agregar FLAG_IMMUTABLE para Android 12 y versiones anteriores
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // sonido de notificacion

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

        notificationManager.notify(System.currentTimeMillis().toInt(), notification) // notificacion en el dispositivo
    }
}