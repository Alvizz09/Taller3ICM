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
import com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.Companion.EXTRA_USER_LAT
import com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.Companion.EXTRA_USER_LON
import com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.Companion.EXTRA_USER_NAME
import com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.Companion.EXTRA_USER_UID
import com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.Companion.NOTIFICATION_CHANNEL_ID

class NotificationHelper(private val context: Context) {
    // Obtiene el servicio de notificaciones
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init { // se ejecuta al momento de crear la clase
        createNotificationChannel() // para la notificacion es necesario crear el canal, aca se crea
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(// se crea el canal en donde se enviaran las notificaciones
                NOTIFICATION_CHANNEL_ID,
                "Usuarios disponibles",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando un usuario se pone disponible"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            //registra el canal en el sistema de notificaciones
            notificationManager.createNotificationChannel(channel)
        }
    }
    //muestra la notificacion local
    fun showUserAvailableNotification(userId: String, userName: String, lat: Double, lon: Double) {
        Log.d("NOTIF", "Notificación enviada con lat=$lat lon=$lon para $userName (uid=$userId)")
        // el intent es el que se encarga de abrir la actividad de seguimiento con los datos del usuario al momento de dar click en la notificacion
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // se envian los datos necesarios para abrir la pantalla de seguimiento
            putExtra(EXTRA_USER_NAME, userName)
            putExtra(EXTRA_USER_LAT, lat.toString())
            putExtra(EXTRA_USER_LON, lon.toString())
            putExtra(EXTRA_USER_UID, userId)
            putExtra("from_notification", true)
        }
        // para que se ejecute el intent en el momento de dar click en la notificacion
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Sonido predeterminado de notificación
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        // aca se crea la notificacion con los datos anteriores y con lo que va a contener esa notificacion
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Usuario disponible, checkealo!!")
            .setContentText("$userName ahora está disponible")
            .setSmallIcon(R.drawable.notificacion)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Categoría mensaje
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible en lockscreen
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sonido, vibración, luces
            .setSound(defaultSoundUri) // Sonido
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$userName ahora está disponible. Toca para ver su ubicación en el mapa.")
            )
            .build()
        // se manda la notificacion al sistema de notificaciones
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }
}