package com.oneapp.oneapp.data.routing.classes

data class GeocodingResult(
    val origin: GeocodedWaypoint,
    val destination: GeocodedWaypoint,
    val intermediates: List<GeocodedWaypoint>
)
