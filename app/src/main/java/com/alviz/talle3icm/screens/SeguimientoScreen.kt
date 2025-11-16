package com.alviz.talle3icm.screens

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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun SeguimientoScreen(
    name: String,
    lat: Double,
    lon: Double,
    navController: NavController,
    locVm: LocationViewModel = viewModel() // tu ubicación actual
) {
    // Ubicación del usuario disponible (notificación)
    val target = LatLng(lat, lon)

    // Ubicación actual tuya (se actualiza en tiempo real)
    val myState by locVm.state.collectAsState()
    val myLocation = LatLng(myState.latitude, myState.longitude)

    // Estado de cámara que se moverá automáticamente al target
    val cameraState = rememberCameraPositionState()

    // Cuando se abre la pantalla, mueve la cámara al punto objetivo
    LaunchedEffect(target) {
        cameraState.animate(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(target, 17f)
            ),
            1000
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState
    ) {

        // La ubicación actual
        Marker(
            state = rememberUpdatedMarkerState(myLocation),
            title = "Yo",
            snippet = "Mi ubicación"
        )

        // Usuario disponible (de la notificación)
        Marker(
            state = rememberUpdatedMarkerState(target),
            title = name,
            snippet = "Disponible"
        )

        // Línea entre usuario actual y el usuario objetivo
        Polyline(
            points = listOf(myLocation, target),
            width = 10f
        )
    }
}
