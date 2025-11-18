package com.alviz.talle3icm.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alviz.talle3icm.model.LocationViewModel
import com.alviz.talle3icm.model.UserAuthViewModel.MyUsersViewModel
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.screens.LocationScreen
import com.alviz.talle3icm.screens.LoginScreen
import com.alviz.talle3icm.screens.PantallaRegistro
import com.alviz.talle3icm.screens.ProfileScreen
import com.alviz.talle3icm.screens.SeguimientoScreen
import com.alviz.talle3icm.screens.enabledList

enum  class Screens {
    Login,
    Home,
    Register,
    listaUsers,
    Profile
}

@Composable
fun Navigation(
    authVm: UserAuthViewModel,
    locVm: LocationViewModel,
    userVm: UserAuthViewModel,
    myUsersVm: MyUsersViewModel,
    fromNotification: Boolean,
    notifName: String?,
    notifUid: String?
) {
    val navController = rememberNavController()
    NavHost(navController =navController, startDestination = Screens.Login.name){

        composable(route = Screens.Login.name){
            LoginScreen(navController)

            // se Navegar despues de que LoginScreen esté dibujada
            LaunchedEffect(fromNotification) {
                if (fromNotification && notifName != null && notifUid != null) {
                    navController.navigate(
                        "seguimiento/${Uri.encode(notifName)}/$notifUid"
                        )
                }
            }
        }

        composable(Screens.Register.name) {
            PantallaRegistro(navController, userVm)
        }

        composable(Screens.Home.name) {
            LocationScreen(locVm, userVm, myUsersVm,navController)
        }

        composable(Screens.listaUsers.name) {
            enabledList(navController, myUsersVm, locVm)
        }

        composable("seguimiento/{name}/{uid}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            SeguimientoScreen(name, uid, navController, locVm, authVm, myUsersVm)
        }
        composable(route = Screens.Profile.name){
            ProfileScreen()
        }

    }
}
