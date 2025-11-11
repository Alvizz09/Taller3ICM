package com.alviz.talle3icm.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alviz.talle3icm.components.CustomTextField
import com.alviz.talle3icm.components.PasswordField
import com.alviz.talle3icm.firebaseAuth
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Screens
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

@Composable
fun PantallaRegistro(navController: NavController, model: UserAuthViewModel = viewModel()) {

    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column (
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text("Registrarse", fontSize = 25.sp, fontFamily = FontFamily.Default, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)
            Text("Ingresa tu datos para registrarte", fontSize = 15.sp, fontFamily = FontFamily.Default, fontStyle = FontStyle.Italic)
            CustomTextField(
                value = name,
                onValueChange = { name = it },
                textfield = "Nombre"
            )
            CustomTextField(
                value = lastName,
                onValueChange = { lastName = it },
                textfield = "Apellido"
            )
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                textfield = "email@domain.com",)
            PasswordField(
                value = password,
                onValueChange = { password = it },
                textfield = "Contraseña"
            )


            Button(
                onClick = {
                    registerUser(name, lastName, email, password,navController, context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                )
            ) {
                Text("Registrarse")
            }

        }
    }

}



fun registerUser(
    name: String,
    lastName: String,
    email: String,
    password: String,
    navController: NavController,
    context: Context
) {

    if (validateForm(email, password)) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName("$name $lastName")
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Usuario registrado correctamente",
                                    Toast.LENGTH_LONG
                                ).show()

                                val db = FirebaseDatabase.getInstance()
                                val userRef = db.getReference("users").child(uid)

                                val userData = mapOf(
                                    "name" to name,
                                    "lastName" to lastName,
                                    "email" to email,
                                    "createdAt" to System.currentTimeMillis()
                                )

                                userRef.setValue(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Usuario registrado y guardado correctamente ✅",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                navController.navigate(Screens.Login.name) {
                                    popUpTo(Screens.Register.name) { inclusive = true }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error al actualizar perfil: ${profileTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            context,
                            "Este correo ya está registrado. Intenta iniciar sesión.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Error al registrar usuario: ${exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    } else {
        Toast.makeText(context, "Datos inválidos. Verifica el correo y la contraseña.", Toast.LENGTH_LONG).show()
    }
}