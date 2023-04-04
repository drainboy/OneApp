package com.oneapp.oneapp.data.routing.classes

data class RouteLegStep (
    val distanceMeters: Int,
    val staticDuration: String,
    val polyline: Polyline,
    val startLocation: Location,
    val endLocation: Location,
    val navigationInstruction: NavigationInstruction?,
    val travelAdvisory: RouteLegTravelAdvisory?
)
