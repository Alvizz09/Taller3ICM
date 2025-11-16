package com.alviz.talle3icm.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alviz.talle3icm.firebaseAuth
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Screens

@Composable
fun menuHamburguesa(navController: NavController, userVm: UserAuthViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {

        Text(text = "Menú", style = MaterialTheme.typography.titleLarge)

        // Ver usuarios
        RowItem("Ver Usuarios", Icons.Default.Edit) {
            navController.navigate(Screens.listaUsers.name)
        }


        RowItem("Cambiar estado", Icons.Default.Place) {
            if (userVm.user.value.status == "Disponible") {
                userVm.updateStatus("No Disponible")
                Toast.makeText(context, "Ya no estás disponible", Toast.LENGTH_LONG).show()
            } else {
                userVm.updateStatus("Disponible")
                Toast.makeText(context, "Ahora estás disponible", Toast.LENGTH_LONG).show()
            }
        }


        RowItem("Mi Perfil", Icons.Default.AccountCircle) {
            navController.navigate(Screens.Profile.name)
        }

        // Logout
        RowItem("Cerrar sesión", Icons.Default.ExitToApp) {
            firebaseAuth.signOut()
            navController.navigate(Screens.Login.name) {
                popUpTo(Screens.Login.name) { inclusive = true }
            }
        }
    }
}