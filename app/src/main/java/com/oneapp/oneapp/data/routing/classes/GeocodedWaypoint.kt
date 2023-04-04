package com.oneapp.oneapp.data.routing.classes

data class GeocodedWaypoint(
    val geocoderStatus: Status,
    val type: List<String>,
    val partialMatch: Boolean,
    val placeId: String,
    val intermediateWayPointRequestIndex: Int
)
