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

    companion object { //PARA CONSTANTES ESTATICAS
        private const val TAG = "UserAvailService" // TAG para los logs para ver coomo se comporta la notificacion
    }

    private var childEventListener: ChildEventListener? = null // Listener para escuchar cambios en la base de datos
    private val usersRef = database.getReference("users") // Referencia a la base de datos
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // usuario actual
    private val userStatusMap = mutableMapOf<String, String>() // Estado en el mapa
    //ciclo de vida del servicio
    //se lllama la primera vez que es creado
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio Creado")
        Log.d(TAG, "Usuario actual: $currentUserId") //para ver el usuario actual
        startListeningForAvailabilityChanges() // Inicia el servicio para escuchar cambios en la base de datos
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Servicio Restablecido")
        return START_STICKY // Para que el servicio se reinicie si se destruye, lo recreara para el servicio backgrround
    }

    override fun onBind(intent: Intent?): IBinder? = null //// Servicio sin comunicación directa con otras apps

    private fun startListeningForAvailabilityChanges() { // Construye el childEventListener para escuchar cambios en la base de datos y se añade a usersRef
        Log.d(TAG, "Iniciando el servicio de escucha de cambios en: users")
        // se crea el listener para escuchar cambios en la base de datos
        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) { // le mandamos si hay un nuevo usuario en users
                val userId = snapshot.key ?: return
                val status = snapshot.child("status").getValue(String::class.java) ?: "No Disponible"
                userStatusMap[userId] = status // Guardamos el estado del usuario en el mapa
                Log.d(TAG, "Usuario Añadido: $userId, status: '$status'") //para verificar si se añadio correctamente el usuario a la base de datos
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { // le mandamos si hay un cambio en el estado de un usuario en users
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

                // Verificar si cambió de "No Disponible" a "Disponible", el .trim() es para evitar espacios vacios, o basura mejor
                if (oldStatus.trim() != "Disponible" && newStatus.trim() == "Disponible") {
                    Log.d(TAG, "Condicion se cumple entonces ... Enviando notificación")
                    //si se cumple obtenemos los datos del usuario para enviarle la notificacion
                    val userName = snapshot.child("name").getValue(String::class.java) ?: "Usuario"
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val lat = snapshot.child("locActual/lat").getValue(Double::class.java) ?: 0.0
                    val lon = snapshot.child("locActual/lng").getValue(Double::class.java) ?: 0.0
                    sendLocalNotification( //mostrara la notificacion local al usuario
                        userId = userId,
                        userName = "$userName $lastName".trim(), //le mandamos datos del usuario que esta disponible sin datos vacios
                        lat = lat,
                        lon = lon
                    )
                } else {
                    Log.d(TAG, "Condición NO cumplida") // para debug mio para ver que cambio
                }

                // Actualizar el estado en el mapa, con los nuevos datos
                userStatusMap[userId] = newStatus
            }

            override fun onChildRemoved(snapshot: DataSnapshot) { // le mandamos si hay un usuario removido de users
                val userId = snapshot.key ?: return
                userStatusMap.remove(userId) // Quitamos el usuario del mapa
                Log.d(TAG, "Usuario Removido: $userId") // para debug mio para ver que cambio
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // No necesitamos implementar esto pero se necesita para el servicio xde
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error en base de datos: ${error.message}") // para debug mio para ver que cambio
            }
        }

        usersRef.addChildEventListener(childEventListener!!) // Agregamos el listener a la base de datos pero no lo ejecutamos, se usuara callback para que escuche las actualizaciones
        Log.d(TAG, "Listener agregado correctamente") // para debug mio para ver que cambio
    }
    // Metodo para enviar la notificacion local al usuario y mostrarla
    private fun sendLocalNotification(userId: String, userName: String, lat: Double, lon: Double) {
        Log.d(TAG, "Enviando notificación para: $userName") // para debug mio para ver que cambio

        try { // instaceamos la notificacion con la clase NotificationHelper, para crear la not
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
            usersRef.removeEventListener(it) //limpiamos los recursos para evitar datos basura
        }
        Log.d(TAG, "Servicio Removido")
        }
}