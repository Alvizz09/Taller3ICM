package com.alviz.talle3icm

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.alviz.talle3icm.model.LocationViewModel
import com.alviz.talle3icm.model.MyUsersViewModel
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Navigation
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database


val firebaseAuth = FirebaseAuth.getInstance()


val database = Firebase.database

val PATH_USERS = "users/"



class MainActivity : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    private var notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        } //permiso de la notificacion antes de iniciar sesion xd
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    POST_NOTIFICATIONS //permiso de notificacion para mostrar en pantalla
                )
            }
        }
        //Revisar si venimos desde una notificación
        val fromNotification = intent.getBooleanExtra("from_notification", false)
        val notifName = intent.getStringExtra("extra_user_name")
        val notifLat = intent.getStringExtra("extra_user_lat")?.toDoubleOrNull()
        val notifLon = intent.getStringExtra("extra_user_lon")?.toDoubleOrNull()

        setContent {
            Navigation(
                UserAuthViewModel(),
                LocationViewModel(),
                UserAuthViewModel(),
                MyUsersViewModel(),
                fromNotification = fromNotification,
                notifName = notifName,
                notifLat = notifLat,
                notifLon = notifLon
            )

        }
    }
}

