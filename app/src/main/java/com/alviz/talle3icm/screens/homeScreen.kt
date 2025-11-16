package com.alviz.talle3icm.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alviz.talle3icm.components.RowItem
import com.alviz.talle3icm.components.menuHamburguesa
import com.alviz.talle3icm.firebaseAuth
import com.alviz.talle3icm.model.LocationViewModel
import com.alviz.talle3icm.model.MyMarker
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Screens
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import org.json.JSONObject


data class Location(val name: String, val latLng: LatLng)



@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun LocationScreen(
    locVm: LocationViewModel = viewModel(),
    userVm: UserAuthViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    val permissionState = rememberPermissionState(locationPermission)

    var showRationale by remember { mutableStateOf(false) }
    var showScreen by remember { mutableStateOf(false) }

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
                Log.i("LocationAPP", "lat=${loc.latitude}, lon=${loc.longitude}")
                locVm.update(loc.latitude, loc.longitude)
            }
        }
    }


    LaunchedEffect(permissionState.status) {
        when {
            permissionState.status.isGranted -> {
                showRationale = false
                showScreen = true
            }
            permissionState.status.shouldShowRationale -> {
                showRationale = true
                showScreen = false
            }
            else -> {
                permissionState.launchPermissionRequest()
            }
        }
    }


    DisposableEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            startLocationUpdates(locationClient, locationRequest, locationCallback, context)
        }
        onDispose { locationClient.removeLocationUpdates(locationCallback) }
    }


    if (showScreen) {
        PantallaMapa(locVm, userVm, navController)
    }


    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permiso requerido") },
            text = { Text("Debes conceder el permiso de ubicación para usar esta función.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionState.launchPermissionRequest()
                }) { Text("Permitir") }
            }
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(
    viewModel: LocationViewModel,
    userVm: UserAuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val locActual = LatLng(state.latitude, state.longitude)
    userVm.updateLocActual(locActual)

    val markers = remember { mutableStateListOf<MyMarker>() }
    val marcadores = loadLocations(context)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(locActual, 18f)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                menuHamburguesa(navController, userVm)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mapa") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->

            Box(Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // marcador actual
                    Marker(
                        state = rememberUpdatedMarkerState(locActual),
                        title = "Tú",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )

                    // marcadores json
                    marcadores.forEach {
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
}

fun loadLocations(context: Context): MutableList<Location> {
    val locations = mutableListOf<Location>()
    val jsonString = context.assets.open("locations.json").bufferedReader().use { it.readText() }

    val json = JSONObject(jsonString).getJSONObject("locations")
    val keys = json.keys()

    while (keys.hasNext()) {
        val key = keys.next()
        val item = json.getJSONObject(key)
        val name = item.getString("name")
        val lat = item.getDouble("latitude")
        val lng = item.getDouble("longitude")
        locations.add(Location(name, LatLng(lat, lng)))
    }

    return locations
}

fun startLocationUpdates(
    client: FusedLocationProviderClient,
    request: LocationRequest,
    callback: LocationCallback,
    context: Context
) {
    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }
}
