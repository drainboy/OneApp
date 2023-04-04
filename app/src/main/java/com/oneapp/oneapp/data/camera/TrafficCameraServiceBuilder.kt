package com.oneapp.oneapp.data.camera

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object TrafficCameraServiceBuilder {
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val client = OkHttpClient.Builder().build()
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.data.gov.sg/v1/transport/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    fun<TrafficCameraResponseBody> buildService(service: Class<TrafficCameraResponseBody>): TrafficCameraResponseBody {
        return retrofit.create(service)
    }
}