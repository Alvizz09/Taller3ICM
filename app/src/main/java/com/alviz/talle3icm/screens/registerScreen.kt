package com.alviz.talle3icm.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.alviz.talle3icm.components.CustomTextField
import com.alviz.talle3icm.components.PasswordField
import com.alviz.talle3icm.firebaseAuth
import com.alviz.talle3icm.model.UserAuthViewModel
import com.alviz.talle3icm.navigation.Screens
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

@Composable
fun PantallaRegistro(navController: NavController, model: UserAuthViewModel = viewModel()) {

    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Observar el Uri de la imagen desde el ViewModel
    val imageUri = model.contactImageUri.collectAsState().value

    // Estado del permiso - Verificar directamente aquí
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher para solicitar el permiso
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "Permiso concedido ", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Se necesita el permiso para agregar una foto a su perfil", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para seleccionar imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            model.setContactImage(it)
            Toast.makeText(context, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                "Registrarse",
                fontSize = 25.sp,
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Ingresa tu datos para registrarte",
                fontSize = 15.sp,
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Italic
            )

            // Imagen de perfil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF2196F3)
                    )
                }
            }

            // Botón para seleccionar imagen
            OutlinedButton(
                onClick = {
                    if (permissionGranted) {
                        // Si ya tiene permiso, abrir galería
                        galleryLauncher.launch("image/*")
                    } else {
                        // Si no tiene permiso, solicitarlo
                        requestPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2196F3)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (imageUri != null) "Cambiar imagen"
                    else if (permissionGranted) "Seleccionar imagen"
                    else "Solicitar permiso",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

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
                textfield = "email@domain.com"
            )
            PasswordField(
                value = password,
                onValueChange = { password = it },
                textfield = "Contraseña"
            )

            Button(
                onClick = {
                    if (!isLoading) {
                        isLoading = true
                        registerUser(name, lastName, email, password, navController, context, model) {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Registrarse")
                }
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
    context: Context,
    model: UserAuthViewModel,
    onComplete: () -> Unit
) {
    if (validateForm(email, password)) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val uid = user?.uid ?: run {
                        onComplete()
                        return@addOnCompleteListener
                    }

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName("$name $lastName")
                        .build()

                    user.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {

                                val uri = model.contactImageUri.value

                                if (uri != null) {
                                    // Si hay imagen
                                    model.uploadContactImage { imageUrl ->
                                        val finalImageUrl = imageUrl

                                        guardarFirebase(
                                            uid,
                                            name,
                                            lastName,
                                            email,
                                            finalImageUrl,
                                            context,
                                            navController,
                                            onComplete
                                        )
                                    }

                                } else {
                                    // No hay imagen
                                    guardarFirebase(
                                        uid,
                                        name,
                                        lastName,
                                        email,
                                        null,
                                        context,
                                        navController,
                                        onComplete
                                    )
                                }

                            } else {
                                Toast.makeText(
                                    context,
                                    "Error al actualizar perfil: ${profileTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                onComplete()
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
                    onComplete()
                }
            }
    } else {
        Toast.makeText(context, "Datos inválidos. Verifica el correo y la contraseña.", Toast.LENGTH_LONG).show()
        onComplete()
    }
}

private fun guardarFirebase(
    uid: String,
    name: String,
    lastName: String,
    email: String,
    imageUrl: String?,
    context: Context,
    navController: NavController,
    onComplete: () -> Unit
) {
    val db = FirebaseDatabase.getInstance()
    val userRef = db.getReference("users").child(uid)

    val userData = mutableMapOf<String, Any>(
        "name" to name,
        "lastName" to lastName,
        "email" to email,
        "createdAt" to System.currentTimeMillis()
    )

    // Agregar URL de imagen si existe
    imageUrl?.let {
        userData["contactImageUrl"] = it
    }

    userRef.setValue(userData)
        .addOnSuccessListener {
            Toast.makeText(
                context,
                "Usuario registrado correctamente ",
                Toast.LENGTH_LONG
            ).show()

            navController.navigate(Screens.Login.name) {
                popUpTo(Screens.Register.name) { inclusive = true }
            }
            onComplete()
        }
        .addOnFailureListener { e ->
            Toast.makeText(
                context,
                "Error al guardar datos: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            onComplete()
        }
}