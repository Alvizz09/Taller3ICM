package com.alviz.talle3icm.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alviz.talle3icm.model.LocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@SuppressLint("UnrememberedMutableState")
@Composable
fun SeguimientoScreen(
    name: String,
    lat: Double,
    lon: Double,
    navController: NavController,
    locationVm: LocationViewModel
) {

    // Ubicación del usuario de la notificación
    val target = LatLng(lat, lon)

    // Tu ubicación actual desde el ViewModel
    val myLocation = locationVm.state.collectAsState().value
    val myLatLng = myLocation?.let { LatLng(it.latitude, it.longitude) }

    // Cámara: apuntar al usuario disponible
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(target, 16f)
    }

    // Para mover la cámara cuando esté lista la pantalla
    LaunchedEffect(target) {
        cameraState.animate(
            update = CameraUpdateFactory.newLatLngZoom(target, 16f),
            durationMs = 1200
        )
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState
        ) {

            // Marcador del usuario disponible (el de la notificación)
            Marker(
                state = MarkerState(position = target),
                title = name,
                snippet = "Usuario disponible",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
            )

            // Ubi ACTUAL
            if (myLatLng != null) {
                Marker(
                    state = MarkerState(position = myLatLng),
                    title = "Yo",
                    snippet = "Mi ubicación actual"
                )

                // DIBUJAR LA RUTA(entre mi ubicación y el usuario disponible)
                Polyline(
                    points = listOf(myLatLng, target),
                    width = 10f
                )
            }
        }
    }
}
