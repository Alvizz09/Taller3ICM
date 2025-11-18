package com.alviz.talle3icm.model

import android.net.Uri
import android.util.Log
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
    var contactImageUrl : String? = null,
    val id: String? = null,
    val lat: Double? = 0.0,
    val lon: Double? = 0.0,
    val status: String = "No Disponible"
    )

class UserAuthViewModel: ViewModel() {

    val _user = MutableStateFlow<AuthState>(AuthState())
    val user = _user

    var contactImageUri = MutableStateFlow<Uri?>(null)

    fun setContactImage(uri: Uri?) {
        contactImageUri.value = uri
    }

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

    fun uploadContactImage(onResult: (String?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val uri = contactImageUri.value ?: return

        val storageRef = com.google.firebase.storage.FirebaseStorage
            .getInstance()
            .reference
            .child("profile_images/$uid.jpg")

        val uploadTask = storageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onResult(downloadUri.toString())
                }.addOnFailureListener { onResult(null) }
            }
            .addOnFailureListener {
                onResult(null)
            }


}
    fun updateContactImageUrl(url: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val updates = mapOf("contactImageUrl" to url)
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .updateChildren(updates)
    }

    fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(AuthState::class.java)
                    if (userData != null) {
                        _user.value = userData

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserAuthViewModel", "Error al cargar datos: ${error.message}")
                }
            })
    }


class MyUsersViewModel : ViewModel() {
    val dbReference = database.getReference("users")
    val _users = MutableStateFlow(listOf<AuthState>())
    val users: StateFlow<List<AuthState>> = _users.asStateFlow()

    private fun DataSnapshot.doubleAt(path: String): Double? {
        val v = child(path).value ?: return null
        return (v as? Number)?.toDouble() ?: v.toString().toDoubleOrNull()
    }

    var vel: ValueEventListener =
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedList = snapshot.children.mapNotNull { c ->

                    val uid = c.key ?: return@mapNotNull null

                    val name = c.child("name").getValue(String::class.java) ?: ""
                    val lastName = c.child("lastName").getValue(String::class.java) ?: ""
                    val status = c.child("status").getValue(String::class.java) ?: ""

                    val lat = c.doubleAt("locActual/lat")
                    val lon = c.doubleAt("locActual/lng")

                    AuthState(
                        id = uid,
                        name = name,
                        lastName = lastName,
                        status = status,
                        lat = lat,
                        lon = lon
                    )
                }
                _users.value = updatedList
            }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}





