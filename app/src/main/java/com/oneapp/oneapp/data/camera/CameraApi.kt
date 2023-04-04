package com.oneapp.oneapp.data.camera

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET

interface CameraApi {
    @GET("traffic-images/")
    fun getTrafficCamera() : Call<GetTrafficCameraResponse>
}