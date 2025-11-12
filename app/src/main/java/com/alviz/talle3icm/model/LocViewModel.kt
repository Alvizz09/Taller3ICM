package com.alviz.talle3icm.model

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus


data class MyMarker(val position: LatLng?, val title: String = "Marker", val snippet: String ="Desc")


data class LocationState(
    val latitude : Double =0.0,
    val longitude : Double =0.0
)

class LocationViewModel : ViewModel(){

    private val _markers = MutableStateFlow<List<MyMarker>>(emptyList())

    val markers: StateFlow<List<MyMarker>> = _markers
    private val _uiState = MutableStateFlow(LocationState())
    val state : StateFlow<LocationState> = _uiState
    fun update(lat : Double, long : Double){
        _uiState.update { it.copy(lat, long) }
    }
    fun addMarker(m : MyMarker){
        _markers.value = _markers.value + m
    }

    fun replaceWith(marker: MyMarker) {
        _markers.value = listOf(marker)
    }

    fun clearMarkers() {
        _markers.value = emptyList()
    }
}