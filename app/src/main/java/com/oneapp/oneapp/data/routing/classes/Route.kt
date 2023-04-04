package com.oneapp.oneapp.data.routing.classes


data class Route(
    val routeLabels: List<RouteLabel>,
    val legs: List<RouteLeg>,
    val distanceMeters: Int,
    val duration: String,
    val staticDuration: String,
    val polyline: Polyline,
    val description: String,
    val warnings: List<String>?,
    val viewport: Viewport,
    val travelAdvisory: RouteTravelAdvisory,
    val routeToken: String
)
