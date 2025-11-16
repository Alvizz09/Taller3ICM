package com.alviz.talle3icm.screens

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alviz.talle3icm.firebaseAuth
import com.alviz.talle3icm.model.LocationViewModel
import com.alviz.talle3icm.model.MyMarker
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Screens
import com.alviz.talle3icm.notificaciones.UserAvailabilityService
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import org.json.JSONObject


data class Location(val name: String, val latLng: LatLng)

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun LocationScreen(locVm: LocationViewModel = viewModel(), userVm: UserAuthViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val serviceIntent = Intent(context, UserAvailabilityService::class.java)
        context.startService(serviceIntent)
    }
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
        PantallaMapa(locVm, userVm, navController)
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
fun PantallaMapa(viewModel: LocationViewModel, userVm: UserAuthViewModel, navController: NavController) {

    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val markers by viewModel.markers.collectAsState()
    val LocActual = LatLng(state.latitude, state.longitude)
    userVm.updateLocActual(LocActual)
    val actualMarkerState = rememberUpdatedMarkerState(position = LocActual)
    val locations: MutableList<Location> = loadLocations(context)
    //val otroMarkerState = rememberUpdatedMarkerState(position = locOtro)


    Scaffold(
        floatingActionButton = {menuBotones(navController, userVm)},
        floatingActionButtonPosition = FabPosition.Start
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
                    it.position?.let { position ->
                        Marker(
                            state = rememberUpdatedMarkerState(position),
                            title = it.title,
                            snippet = it.snippet,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        )
                    }
                }
                locations.forEach {
                    Marker(
                        state = rememberUpdatedMarkerState(it.latLng),
                        title = it.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
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


fun loadLocations(context: Context): MutableList<Location> {
    val locations = mutableListOf<Location>()

    val jsonString = context.assets
        .open("locations.json")
        .bufferedReader()
        .use { it.readText() }

    val json = JSONObject(jsonString)
    val obj = json.getJSONObject("locations")
    val keys = obj.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val item = obj.getJSONObject(key)
        val name = item.getString("name")
        val lat = item.getDouble("latitude")
        val lng = item.getDouble("longitude")
        locations.add(Location(name, LatLng(lat, lng)))
    }
    return locations
}


@Composable
fun menuBotones(navController: NavController, userVm: UserAuthViewModel){
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(16.dp)
    ) {
        if (expanded) {
            SmallFloatingActionButton(
                onClick = { navController.navigate(Screens.listaUsers.name) },
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFF03A9F4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Acción 1")
            }
            Spacer(Modifier.height(12.dp))

            SmallFloatingActionButton(
                onClick = { if(userVm.user.value.status == "Disponible"){
                    userVm.updateStatus("No Disponible")

                    Toast.makeText(
                        context,
                        "Ya no estás disponible",
                        Toast.LENGTH_LONG
                    ).show()
                } else {userVm.updateStatus("Disponible")
                    Toast.makeText(
                        context,
                        "Ahora estás disponible",
                        Toast.LENGTH_LONG
                    ).show()}
                          },
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFF03A9F4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Place, contentDescription = "Acción 2")
            }

            Spacer(Modifier.height(12.dp))

            SmallFloatingActionButton(
                onClick = { firebaseAuth.signOut()
                    navController.navigate(Screens.Login.name){popUpTo(Screens.Login.name){inclusive=true}} },
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFF03A9F4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Acción 3")
            }
        }


        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.padding(16.dp),
            containerColor = Color(0xFF03A9F4),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp)) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Más acciones"
            )
        }
    }
}



