package com.alviz.talle3icm.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

data class AuthState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val lastName: String = "",
    val contactImage: Uri? = null,
    val id: Int? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    )

class UserAuthViewModel: ViewModel(){

    val _user= MutableStateFlow<AuthState>(AuthState())
    val user= _user

    fun updateEmail(newEmail: String){
        _user.value= _user.value.copy(email=newEmail)
    }
    fun updatePassword(newPassword: String){
        _user.value= _user.value.copy(password=newPassword)
    }
    fun updateName(newName: String){
        _user.value= _user.value.copy(name=newName)
    }
    fun updateLastName(newLastName: String){
        _user.value= _user.value.copy(lastName=newLastName)
    }
    fun updateContactImage(newContactImage: Uri?){
        _user.value= _user.value.copy(contactImage=newContactImage)
    }
    fun updateId(newId: Int?) {
        _user.value = _user.value.copy(id = newId)
    }
    fun updateLat(newLat: Double?) {
        _user.value = _user.value.copy(lat = newLat)
    }
    fun updateLon(newLon: Double?) {
        _user.value = _user.value.copy(lon = newLon)
    }

}