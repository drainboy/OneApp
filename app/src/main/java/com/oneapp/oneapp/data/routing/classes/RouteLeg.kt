package com.oneapp.oneapp.data.routing.classes

data class RouteLeg(
    val distanceMeters: Int,
    val duration: String,
    val staticDuration: String,
    val polyline: Polyline,
    val startLocation: Location,
    val endLocation: Location,
    val steps: List<RouteLegStep>,
    val travelAdvisory: RouteLegTravelAdvisory
)
