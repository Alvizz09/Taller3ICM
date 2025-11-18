package com.alviz.talle3icm.screens

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.core.app.ActivityCompat
import com.alviz.talle3icm.model.LocationViewModel
import com.alviz.talle3icm.model.MyUsersViewModel
import com.alviz.talle3icm.model.UserAuthViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@SuppressLint("MissingPermission")
@Composable
fun SeguimientoScreen(
    name: String,
    userId: String?,
    navController: NavController,
    locVm: LocationViewModel,
    authVm: UserAuthViewModel,
    myUsersVm: MyUsersViewModel
) {
    val context = LocalContext.current

    LaunchedEffect(true) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).setWaitForAccurateLocation(true).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                // Actualiza estado del ViewModel compartido
                locVm.update(loc.latitude, loc.longitude)

                authVm.updateLocActual(
                    LatLng(loc.latitude, loc.longitude))
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        }
    }
    // ---------------------------------------------------------------------------------------

    // Se obtiene la ubicación REAL desde el mismo ViewModel que usa PantallaMapa
    val state by locVm.state.collectAsState()
    val miUbi = LatLng(state.latitude, state.longitude)

    val users by myUsersVm.users.collectAsState()

    val selectedUser = users.firstOrNull { it.id == userId }

    val ubiUser = remember(selectedUser?.lat, selectedUser?.lon) {
        LatLng(
            selectedUser?.lat ?: 0.0,
            selectedUser?.lon ?: 0.0
        )
    }

    // Logs para verificar que ya no sea Ghana
    Log.d("SEGUI", "Mi ubicación REAL = $miUbi")
    Log.d("SEGUI", "Ubicación usuario = $ubiUser")

    // Cámara apuntando al usuario disponible
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubiUser, 16f)
    }

    val currentMiUbi by rememberUpdatedState(miUbi)
    val currentUbiUser by rememberUpdatedState(ubiUser)
    val currentSelectedUser by rememberUpdatedState(selectedUser)

    LaunchedEffect(userId) {

        while (true) {
            val results = FloatArray(1)

            android.location.Location.distanceBetween(
                currentMiUbi.latitude,
                currentMiUbi.longitude,
                currentUbiUser.latitude,
                currentUbiUser.longitude,
                results
            )

            val metros = results[0]

            if (metros > 5f && currentSelectedUser != null) {
                Toast.makeText(
                    context,
                    "Estás a $metros metros de ${currentSelectedUser!!.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState,
    ) {

        // UBICACIÓN DEL USUARIO ACTUAL
        Marker(
            state = rememberUpdatedMarkerState(miUbi),
            title = "Mi ubicación",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        )

        //UBICACIÓN DEL USUARIO DISPONIBLE
        Marker(
            state = rememberUpdatedMarkerState(ubiUser),
            title = name,
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
        )

        //MARCA LA RUTA ENTRE AMBOS
        Polyline(
            points = listOf(miUbi, ubiUser)
        )
    }
}
