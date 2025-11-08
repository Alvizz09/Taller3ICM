package com.alviz.talle3icm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alviz.talle3icm.screens.LoginScreen

enum  class Screens {
    Login,
    Home,
    Register
}

@Composable
fun Navigation(){
    val navController = rememberNavController()
    NavHost(navController =navController, startDestination = Screens.Login.name){

        composable(route = Screens.Login.name){
            LoginScreen(navController)
        }

    }
}
