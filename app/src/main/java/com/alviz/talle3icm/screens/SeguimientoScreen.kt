package com.alviz.talle3icm.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun SeguimientoScreen(name: String, lat: Double, lon: Double, navController: NavController) {
    val target = LatLng(lat, lon)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(target, 16f) // zoom al abrir la notificacion
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState // Usa el estado de la cámara
    ) {
        Marker(
            state = rememberUpdatedMarkerState(position = target), // Usa el estado del marcador
            title = name,
            snippet = "Disponible"
        )
    }
}
