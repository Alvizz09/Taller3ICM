package com.alviz.talle3icm.notificaciones

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.alviz.talle3icm.database
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class UserAvailabilityService : Service() {

    companion object {
        private const val TAG = "UserAvailService" // TAG para los logs para ver coomo se comporta la notificacion
    }

    private var childEventListener: ChildEventListener? = null // Listener para escuchar cambios en la base de datos
    private val usersRef = database.getReference("users") // Referencia a la base de datos
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid //
    private val userStatusMap = mutableMapOf<String, String>() // Estado en el mapa

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio Creado")
        Log.d(TAG, "Usuario actual: $currentUserId") //para ver el usuario actual
        startListeningForAvailabilityChanges() // Inicia el servicio para escuchar cambios en la base de datos
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Servicio Restablecido")
        return START_STICKY // Para que el servicio se reinicie si se destruye
    }

    override fun onBind(intent: Intent?): IBinder? = null //// Servicio sin comunicación directa con otras apps

    private fun startListeningForAvailabilityChanges() {
        Log.d(TAG, "Iniciando el servicio de escucha de cambios en: users")

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) { // le mandamos
                val userId = snapshot.key ?: return
                val status = snapshot.child("status").getValue(String::class.java) ?: "No Disponible"
                userStatusMap[userId] = status // Agregamos el usuario a la base de datos
                Log.d(TAG, "Usuario Añadido: $userId, status: '$status'") //para verificar si se añadio correctamente el usuario a la base de datos
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val userId = snapshot.key ?: return // obtenemos el id del usuario

                Log.d(TAG, "Cambio detectado en usuario: $userId") // para debug mio para ver que cambio

                // Ignorar cambios del usuario actual
                if (userId == currentUserId) {
                    Log.d(TAG, "Ignorando cambio propio") // para debug mio para ver que cambio
                    return
                }

                val newStatus = snapshot.child("status").getValue(String::class.java) ?: "No Disponible"
                val oldStatus = userStatusMap[userId] ?: "No Disponible"

                Log.d(TAG, "Estado anterior: '$oldStatus' → Estado nuevo: '$newStatus'") //para ver si el estado se cambio o no

                // Verificar si cambió de "No Disponible" a "Disponible"
                if (oldStatus.trim() != "Disponible" && newStatus.trim() == "Disponible") {
                    Log.d(TAG, "Condicion se cumple entonces ... Enviando notificación")

                    val userName = snapshot.child("name").getValue(String::class.java) ?: "Usuario"
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val lat = snapshot.child("locActual/lat").getValue(Double::class.java) ?: 0.0
                    val lon = snapshot.child("locActual/lng").getValue(Double::class.java) ?: 0.0

                    // 👇 CAMBIO: ahora también le pasamos el userId al método de notificación
                    sendLocalNotification(
                        userId = userId,
                        userName = "$userName $lastName".trim(), //le mandamos datos del usuario que esta disponible
                        lat = lat,
                        lon = lon
                    )
                } else {
                    Log.d(TAG, "Condición NO cumplida") // para debug mio para ver que cambio
                }

                // Actualizar el estado en el mapa
                userStatusMap[userId] = newStatus
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val userId = snapshot.key ?: return
                userStatusMap.remove(userId)
                Log.d(TAG, "Usuario Removido: $userId") // para debug mio para ver que cambio
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // No necesitamos implementar esto xde
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error en base de datos: ${error.message}") // para debug mio para ver que cambio
            }
        }

        usersRef.addChildEventListener(childEventListener!!) // Agregamos el listener a la base de datos pero no lo ejecutamos
        Log.d(TAG, "Listener agregado correctamente") // para debug mio para ver que cambio
    }

    private fun sendLocalNotification(userId: String, userName: String, lat: Double, lon: Double) {
        Log.d(TAG, "Enviando notificación para: $userName") // para debug mio para ver que cambio

        try {
            val notificationHelper = NotificationHelper(applicationContext) // Instanciamos la notificacion
            notificationHelper.showUserAvailableNotification( // mandamos la notificacion
                userId = userId,
                userName = userName,
                lat = lat,
                lon = lon
            )
            Log.d(TAG, "Notificación enviada exitosamente") // para debug mio para ver que cambio
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar notificación: ${e.message}")
            e.printStackTrace() // para debug mio para ver que cambio
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        childEventListener?.let {
            usersRef.removeEventListener(it)
        }
        Log.d(TAG, "Servicio Removido")
        }
}