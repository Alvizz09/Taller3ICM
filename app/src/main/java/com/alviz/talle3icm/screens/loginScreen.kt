package com.alviz.talle3icm.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alviz.talle3icm.components.CustomTextField
import com.alviz.talle3icm.components.PasswordField
import com.alviz.talle3icm.components.SignUpText
import com.alviz.talle3icm.firebaseAuth
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Screens

@Composable
fun LoginScreen(navController: NavController, model: UserAuthViewModel = viewModel()) {
    val user by model.user.collectAsState()
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        firebaseAuth.currentUser?.let {
            navController.navigate(Screens.Home.name) {
                popUpTo(Screens.Login.name) {
                    inclusive = true
                }
            }
        }
    }

    Column (
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {

        Text("Iniciar Sesión", fontSize = 25.sp, fontFamily = FontFamily.Default, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Bold)
        CustomTextField(
            value = user.email,
            onValueChange = { model.updateEmail(it)},
            textfield = "email@domain.com",
        )

        PasswordField(
            value = user.password,
            onValueChange = { model.updatePassword(it) },
            textfield = "Contraseña"
        )
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        SignUpText(
            onSignUpClick = {
                navController.navigate(Screens.Register.name)
            }
        )


        Button(
            onClick = {
                login(user.email, user.password, navController, context) { msg ->
                    errorMessage = msg
                }
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
            Text("Login", fontSize = 15.sp)
        }

    }
}


fun validateForm(email:String, password:String):Boolean{
    if (!email.isEmpty() &&
        validEmailAddress(email) &&
        !password.isEmpty() &&
        password.length >= 6)
    {
        return true
    }
    return false
}
private fun validEmailAddress(email:String):Boolean{
    val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    return email.matches(regex.toRegex())
}


fun login(email:String, password:String, controller: NavController, context: Context, onError: (String) -> Unit){
    if(validateForm(email, password)){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful){
                controller.navigate(Screens.Home.name)
            }else{
                onError("Correo o contraseña incorrecta")
            }
        }
    }else{
        onError("Por favor ingresa un correo y contraseña válidos")
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = NavController(LocalContext.current))
}
