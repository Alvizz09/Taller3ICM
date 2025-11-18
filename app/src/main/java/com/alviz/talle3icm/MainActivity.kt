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
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Navigation
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

val firebaseAuth = FirebaseAuth.getInstance()
val database = Firebase.database
val PATH_USERS = "users/"


class MainActivity : ComponentActivity() {

    @SuppressLint("ViewModelConstructorInComposable")
    private var notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> }

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        }

        // Revisar si venimos desde una notificación
        val fromNotification = intent.getBooleanExtra("from_notification", false)

        // Nombre del usuario desde la notificación
        val notifName = intent.getStringExtra(
            com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.EXTRA_USER_NAME
        )

        // UID del usuario disponible desde la notificación
        val notifUid = intent.getStringExtra(
            com.alviz.talle3icm.notificaciones.MiFirebaseMessagingService.EXTRA_USER_UID
        )

        setContent {
            Navigation(
                UserAuthViewModel(),
                LocationViewModel(),
                UserAuthViewModel(),
                MyUsersViewModel(),
                fromNotification = fromNotification,
                notifName = notifName,
                notifUid = notifUid
            )
            }
        }
}

