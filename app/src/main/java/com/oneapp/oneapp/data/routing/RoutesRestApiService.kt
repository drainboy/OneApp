package com.oneapp.oneapp.data.routing

import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoutesRestApiService {
    fun getRoute(requestBody: RoutesRequestBody, onResult: (Any?) -> Unit){
        val retrofit = RoutesServiceBuilder.buildService(RoutesApi::class.java)
        retrofit.getRoute(requestBody).enqueue(
            object : Callback<RoutesResponseBody>{
                override fun onResponse(call: Call<RoutesResponseBody>, response: Response<RoutesResponseBody>) {
                    val routesResponse = response.body()
                    println(routesResponse)
                    if(response.isSuccessful) {
                        println("Number of routes: " + routesResponse?.routes?.size)
                        routesResponse?.routes?.forEach {
                            onResult(it.polyline.encodedPolyline)
                        }
                    } else {
                        val error = JSONObject(response.errorBody()!!.string())
                        onResult("There's an error {$error}")
                    }
                }

                override fun onFailure(call: Call<RoutesResponseBody>, t: Throwable) {
                    onResult(call.request())
                }
            }
        )
    }
}