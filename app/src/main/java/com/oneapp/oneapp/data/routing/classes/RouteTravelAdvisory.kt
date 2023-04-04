package com.oneapp.oneapp.data.routing.classes

data class RouteTravelAdvisory(
    val tollInfo: TollInfo?,
    val speedReadingIntervals: List<SpeedReadingInterval>,
    val fuelConsumptionMicroliters: String?
)
