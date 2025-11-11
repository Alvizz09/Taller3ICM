package com.alviz.talle3icm.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.alviz.talle3icm.database
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val lastName: String = "",
    //val contactImage: String? = null,
    val id: Int? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val status: String = "No Disponible"
    )

class UserAuthViewModel: ViewModel() {

    val _user = MutableStateFlow<AuthState>(AuthState())
    val user = _user

    fun updateEmail(newEmail: String) {
        _user.value = _user.value.copy(email = newEmail)
    }

    fun updatePassword(newPassword: String) {
        _user.value = _user.value.copy(password = newPassword)
    }

    fun updateName(newName: String) {
        _user.value = _user.value.copy(name = newName)
    }

    fun updateLastName(newLastName: String) {
        _user.value = _user.value.copy(lastName = newLastName)
    }

  /*  fun updateContactImage(newContactImage: Uri?) {
        _user.value = _user.value.copy(contactImage = newContactImage)
    }

   */
    fun updateId(newId: Int?) {
        _user.value = _user.value.copy(id = newId)
    }

    fun updateLocActual(newLoc: LatLng) {
        _user.value = _user.value.copy(
            lat = newLoc.latitude,
            lon = newLoc.longitude
        )

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val updates = mapOf(
            "locActual/lat" to newLoc.latitude,
            "locActual/lng" to newLoc.longitude,
        )
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .updateChildren(updates)

    }

    fun updateStatus(newStatus: String) {
        _user.value = _user.value.copy(status = newStatus)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val updates = mapOf(
            "status" to newStatus
        )
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .updateChildren(updates)

    }
}


    class MyUsersViewModel : ViewModel() {
        val dbReference = database.getReference("users")
        val _users = MutableStateFlow(listOf<AuthState>())
        val users: StateFlow<List<AuthState>> = _users.asStateFlow()
        var vel: ValueEventListener =
            dbReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    android.util.Log.d("USERS", "childrenCount=${snapshot.childrenCount}")
                    val updatedList = mutableListOf<AuthState>()
                    for (child in snapshot.children) {
                        val user = child.getValue<AuthState>()
                        user?.let {
                            updatedList.add(user)
                        }
                    }
                    _users.value = updatedList
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
