package com.alviz.talle3icm.screens

import android.net.Uri
import android.util.Log
import android.widget.Space
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.alviz.talle3icm.model.UserAuthViewModel

@Composable
fun ProfileScreen(viewModel: UserAuthViewModel = viewModel() ) {

    val imageUri by viewModel.contactImageUri.collectAsState()

    Scaffold() { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues)
                        .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {



            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri : Uri? ->
                if(uri != null){
                    viewModel.setContactImage(uri)
                    Log.d("ProfileScreen", "Imagen seleccionada: $uri")
                }
            }

            if(imageUri != null){
                AsyncImage(
                    model = imageUri,
                    contentDescription = "imagen seleccionada",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(onClick = { launcher.launch("image/*") }) {
                Text(text = "Seleccionar imagen de perfil")
            }
            if(imageUri != null){
                Button(onClick = {
                    viewModel.uploadContactImage { url ->
                        if(url != null){
                            viewModel.updateContactImageUrl(url)
                            Log.d("ProfileScreen", "Imagen subida con exito: $url")
                        } else {
                            Log.e("ProfileScreen", "Error al subir la imagen")
                        }
                    }
                }) {
                    Text(text = "Subir imagen a Firebase")
                }
            }



        }

    }

}


