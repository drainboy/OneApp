package com.oneapp.oneapp.data.camera

import android.util.Log
import com.google.gson.Gson
import com.oneapp.oneapp.data.camera.TrafficCameraServiceBuilder.retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrafficCameraRestApiService {
    fun getImages( onResult: (GetTrafficCameraResponse) -> Unit) {
        val trafficData: CameraApi = retrofit.create(CameraApi::class.java)
        trafficData.getTrafficCamera().enqueue(
            object: Callback<GetTrafficCameraResponse> {
                override fun onResponse(
                    call: Call<GetTrafficCameraResponse>,
                    response: Response<GetTrafficCameraResponse>
                ) {
                    if (!response.isSuccessful) {
                        //Toast to inform user that the api is not working (Need to change the context for it to work)
                        //Toast.makeText(getActivity().getApplicationContext(), "Unable to retrieve api", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val body = response.body()!!
                    onResult(body)
                }

                override fun onFailure(call: Call<GetTrafficCameraResponse>, t: Throwable) {
                    Log.i("MainActivity", t.message ?: "Null message")
                }
            }
        )
    }
}