package com.oneapp.oneapp.data.routing

import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.ktx.api.net.findAutocompletePredictionsRequest
import com.oneapp.oneapp.data.routing.classes.Destination
import com.oneapp.oneapp.data.routing.classes.Origin
import com.oneapp.oneapp.data.routing.classes.RouteModifiers

data class RoutesRequestBody(
    val computeAlternativeRoutes: Boolean,
    val departureTime: String,
    val destination: Destination,
    val languageCode: String,
    val origin: Origin,
    val routeModifiers: RouteModifiers,
    val routingPreference: String,
    val travelMode: String = "DRIVE",
    val units: String = "IMPERIAL"
)

fun getRequestInSG(query:String): FindAutocompletePredictionsRequest {
    return findAutocompletePredictionsRequest { setQuery(query).setCountry("SG") }
}