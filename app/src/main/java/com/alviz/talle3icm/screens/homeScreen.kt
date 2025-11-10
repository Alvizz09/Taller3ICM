package com.alviz.talle3icm.screens

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alviz.talle3icm.firebaseAuth
import com.alviz.talle3icm.model.LocationViewModel
import com.alviz.talle3icm.model.MyMarker
import com.alviz.talle3icm.model.UserAuthViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun LocationScreen(locVm: LocationViewModel = viewModel(), userVm: UserAuthViewModel = viewModel()) {
    val context = LocalContext.current
    val LocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
    val LocationPermissionState = rememberPermissionState(LocationPermission)
    var showRationale by remember { mutableStateOf(false) }
    var showScreen by remember { mutableStateOf(false)}

    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setWaitForAccurateLocation(true)
            .build()
    }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                Log.i("LocationApp", "lat=${loc.latitude}, lon=${loc.longitude}")
                locVm.update(loc.latitude, loc.longitude)
            }
        }
    }

    LaunchedEffect(LocationPermissionState.status) {
        if(LocationPermissionState.status.isGranted){
            showRationale = false
            showScreen = true
        } else if(LocationPermissionState.status.shouldShowRationale){
            showRationale = true
            showScreen = false
        } else{
            LocationPermissionState.launchPermissionRequest()
            showScreen = false
        }
    }

    DisposableEffect(LocationPermissionState.status) {
        if (LocationPermissionState.status.isGranted) {
            startLocationUpdatesIfGranted(
                locationClient, locationRequest, locationCallback, context
            )
        }
        onDispose { locationClient.removeLocationUpdates(locationCallback) }
    }

    if (showScreen){
        PantallaMapa(locVm, userVm)
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permiso de Ubicación") },
            text = { Text("Necesitamos los permisos de ubicación para mostrar tu ubicación.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    LocationPermissionState.launchPermissionRequest()
                }) { Text("Conceder") }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun PantallaMapa(viewModel: LocationViewModel, userVm: UserAuthViewModel) {

    val state by viewModel.state.collectAsState()
    var routeColor by remember { mutableStateOf(Color(0xFF9E9E9E)) }
    var markers = remember { mutableStateListOf<MyMarker>() }
    val routePoints = remember { mutableStateListOf<LatLng>() }
    val LocActual = LatLng(state.latitude, state.longitude)
    userVm.updateLocActual(LocActual)
    updateOnlyLocation(LocActual)
    val actualMarkerState = rememberUpdatedMarkerState(position = LocActual)



    Scaffold (
        floatingActionButton = {
            FloatingActionButton(
                onClick = { routeColor = Color(0xFF03A9F4) },
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFF03A9F4),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Borrar marcadores")
            }
        }, floatingActionButtonPosition = FabPosition.Start
    ) { paddingValues ->

        val cameraPositionState = key(LocActual) {
            rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LocActual, 18f)
            }
        }
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
            )
            {
                Marker(
                    state = actualMarkerState,
                    title = "Actual",
                    snippet = "Posición Actual"
                )
                markers.forEach {
                    Marker(
                        state = rememberUpdatedMarkerState(it.position),
                        title = it.title,
                        snippet = it.snippet
                    )

                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints.toList(), width = 18f,
                            color = routeColor,
                        )
                    }
                }
            }
        }
    }
}

private fun startLocationUpdatesIfGranted(
    client: FusedLocationProviderClient,
    request: LocationRequest,
    callback: LocationCallback,
    context: android.content.Context
) {
    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

    }
}

fun updateOnlyLocation(newLatLng: LatLng) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val updates = mapOf(
        "locActual/lat" to newLatLng.latitude,
        "locActual/lng" to newLatLng.longitude,
    )
    FirebaseDatabase.getInstance()
        .getReference("users")
        .child(uid)
        .updateChildren(updates)
}