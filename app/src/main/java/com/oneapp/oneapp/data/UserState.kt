package com.oneapp.oneapp.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

val changiAirport = "ChIJ483Qk9YX2jERA0VOQV7d1tY"
val vivocity = "ChIJK7xLl1gZ2jERP_GdUY9XNLo"

var fakeDatabase = listOf(
    User("nicolesaidno@gmail.com", "12345",
        mutableListOf(changiAirport, vivocity),
        mutableListOf(camerasByHighway["PIE"]!![0], camerasByHighway["PIE"]!![1], camerasByHighway["PIE"]!![2]),
        mutableListOf()
    )
)

class User(
    val email:String,
    val password:String,
    val favouriteLocations:MutableList<String> = mutableListOf(),
    val favouriteCameras:MutableList<TrafficCamera> = mutableListOf(),
    val places: MutableList<Place> = mutableListOf(),
    var lastKnownLocation : LatLng = LatLng(0.0,0.0)
)

class UserState {
    var state = "Guest"
    var user = User("","")
//    var state = "Login"
//    var user = fakeDatabase[0]

    fun appendPlaces(place: Place){
        user.places.add(place)
    }

    fun register(email:String, password:String, context: Context){
        login(User(email, password, places = mutableListOf()), context)
    }

    fun login(user: User, context: Context){
        this.user = user
        state = "Login"
        updateLocation(fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context))
    }

    fun logout(){
        this.user = User("","")
        state = "Guest"
    }

    @SuppressLint("MissingPermission")
    fun updateLocation(fusedLocationProviderClient: FusedLocationProviderClient){
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if(it == null) {
                println("Didn't manage to get any last location")
                fusedLocationProviderClient.getCurrentLocation(100, null)
                    .addOnSuccessListener { second ->
                        this.user.lastKnownLocation = LatLng(second.latitude, second.longitude)
                        println("Got current location ${this.user.lastKnownLocation}")
                    }
            }
            else {
                this.user.lastKnownLocation = LatLng(it.latitude, it.longitude)
                println(it)
            }
        }
    }
}