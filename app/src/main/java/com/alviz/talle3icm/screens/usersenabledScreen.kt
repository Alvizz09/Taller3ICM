package com.alviz.talle3icm.screens

import android.R.attr.padding
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
import com.alviz.talle3icm.model.MyUsersViewModel
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Screens
import com.google.firebase.auth.FirebaseAuth

@Composable
fun enabledList(navcontroller: NavController,
         viewModel: MyUsersViewModel = viewModel()) {
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
                                onClick = { })
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

