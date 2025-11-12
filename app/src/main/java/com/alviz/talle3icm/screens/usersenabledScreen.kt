package com.alviz.talle3icm.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alviz.talle3icm.components.DashboardCard
import com.alviz.talle3icm.firebaseAuth
import com.alviz.talle3icm.model.LocationViewModel
import com.alviz.talle3icm.model.MyMarker
import com.alviz.talle3icm.model.MyUsersViewModel
import com.alviz.talle3icm.navigation.Screens
import com.google.android.gms.maps.model.LatLng

@Composable
fun enabledList(navcontroller: NavController,
         viewModel: MyUsersViewModel = viewModel(), locationVm: LocationViewModel) {
    val users by viewModel.users.collectAsState()

    val enabled = remember(users) {
        users.filter { it.status.trim() == "Disponible" }
    }

    Scaffold(topBar = { AppTopBar(navcontroller) }) { paddingValues ->

        if (enabled.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay usuarios disponibles")
            }
        }
            else {
                LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {

                    items(enabled) { item ->
                        android.util.Log.d("USERS", "infoUSER=${item.name}")
                        if (item.status == "Disponible") {
                            DashboardCard(
                                title = "",
                                content = {
                                    Text(text = item.name)
                                    Text(text = item.lastName)
                                },
                                buttonText = "Ver ubicación",
                                onClick = {
                                    if(item.lat == 0.0 || item.lon == 0.0){
                                        android.util.Log.d("OTRO", "LAT Y LON SON 0")
                                    }
                                    val latLng = item.lat?.let { item.lon?.let { longitude -> LatLng(it, longitude) } }
                                    android.util.Log.d("OTRO", "LOCATION=${item.lat} ${item.lon}")
                                    val marker = MyMarker(latLng, item.name)
                                    locationVm.replaceWith(marker)
                                    navcontroller.navigate(Screens.Home.name) {
                                    }
                                })
                        }
                    }
                }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavController){
    TopAppBar(
        title={Text("")},
        actions = {
            IconButton(onClick = { firebaseAuth.signOut()
                navController.navigate(Screens.Login.name){popUpTo(Screens.Login.name){inclusive=true}} },) {
                Icon(Icons.Filled.ExitToApp, "Log Out")
            }
        }
    )
}

