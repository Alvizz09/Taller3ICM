package com.alviz.talle3icm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alviz.talle3icm.model.LocationViewModel
import com.alviz.talle3icm.model.MyUsersViewModel
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.screens.LocationScreen
import com.alviz.talle3icm.screens.LoginScreen
import com.alviz.talle3icm.screens.PantallaRegistro
import com.alviz.talle3icm.screens.enabledList

enum  class Screens {
    Login,
    Home,
    Register,
    listaUsers
}

@Composable
fun Navigation(userVm: UserAuthViewModel, locVm: LocationViewModel, MyUsersVm: MyUsersViewModel){
    val navController = rememberNavController()
    NavHost(navController =navController, startDestination = Screens.Login.name){

        composable(route = Screens.Login.name){
            LoginScreen(navController)
        }
        composable(route = Screens.Register.name){
            PantallaRegistro(navController, userVm)
        }
        composable(route = Screens.Home.name){
            LocationScreen(locVm, userVm, navController)
        }
        composable(route = Screens.listaUsers.name){
            enabledList(navController, MyUsersVm, locVm)
        }

    }
}
