package com.alviz.talle3icm

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alviz.talle3icm.model.LocationViewModel
import com.alviz.talle3icm.model.MyUsersViewModel
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Navigation
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database


val firebaseAuth = FirebaseAuth.getInstance()


val database = Firebase.database

val PATH_USERS = "users/"



class MainActivity : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            Navigation(UserAuthViewModel(), LocationViewModel(), MyUsersViewModel())

        }
    }
}

