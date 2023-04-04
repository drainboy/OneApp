package com.oneapp.oneapp.data.routing

import com.oneapp.oneapp.BuildConfig.GOOGLE_MAPS_API_KEY
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RoutesApi {

    @Headers("Content-Type: application/json",
        "X-Goog-Api-Key: $GOOGLE_MAPS_API_KEY",
        "X-Goog-FieldMask: *"
    )
    @POST("directions/v2:computeRoutes")
    fun getRoute(@Body routesRequest: RoutesRequestBody): Call<RoutesResponseBody>
}