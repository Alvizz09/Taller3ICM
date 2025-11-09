package com.alviz.talle3icm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.screens.LoginScreen
import com.alviz.talle3icm.screens.PantallaHome
import com.alviz.talle3icm.screens.PantallaRegistro

enum  class Screens {
    Login,
    Home,
    Register
}

@Composable
fun Navigation(userVm: UserAuthViewModel){
    val navController = rememberNavController()
    NavHost(navController =navController, startDestination = Screens.Login.name){

        composable(route = Screens.Login.name){
            LoginScreen(navController)
        }
        composable(route = Screens.Register.name){
            PantallaRegistro(navController, userVm)
        }
        composable(route = Screens.Home.name){
            PantallaHome()
        }

    }
}
