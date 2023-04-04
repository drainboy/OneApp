package com.oneapp.oneapp.ui

//import androidx.compose.foundation.layout.RowScopeInstance.align
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.oneapp.oneapp.data.ApiCameraData
import com.oneapp.oneapp.data.TrafficCamera
import com.oneapp.oneapp.data.UserState
import com.oneapp.oneapp.data.camera.TrafficCameraRestApiService
import com.oneapp.oneapp.data.camerasByHighway

// Search for a camera by ID
fun findCameraById(id: Int): TrafficCamera? {
    for (cameras in camerasByHighway.values) {
        for (camera in cameras) {
            if (camera.camera_id == id) {
                return camera
            }
        }
    }
    return null
}

@Composable
fun TrafficCameraDetailsScreen(
    navController: NavController = rememberNavController(),
    cameras: List<TrafficCamera>?,
    userState: UserState
){
    //Text("You have reached the detailed Traffic Camera Page!!")
    var ApiInfoState by remember { mutableStateOf(listOf<ApiCameraData>()) }
    TrafficCameraRestApiService().getImages {
        val ApiInfoList = mutableListOf<ApiCameraData>()
        for (i in it.items[0].cameras.indices) {
            val newApiData = ApiCameraData(
                (it.items[0].cameras[i].camera_id).toInt(),
                it.items[0].cameras[i].image
            )
            ApiInfoList.add(newApiData)
        }
        ApiInfoState = ApiInfoList
    }

    LazyColumn {
        if (cameras != null) {
            items(cameras.size) { eachCamera ->
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(width = 396.dp, height = 269.dp)
                        //.aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        //.background(Color.Red)
                ) {
                    // Text(text = "Box $index", color = Color.White)
                    val apiCameraData = ApiInfoState.find {it.camera_id == cameras[eachCamera].camera_id}
                    val image_URL = apiCameraData?.image

                    if (image_URL != null) {
                        SquareLongCard(
                            cam = cameras[eachCamera],
                            imageURL = image_URL,
                            userState = userState
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SquareLongCard(cam: TrafficCamera, imageURL: String, userState: UserState){
    Box(contentAlignment = Alignment.TopEnd){
        Column {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 396.dp, height = 222.dp)
                    .background(Color.LightGray)
                //.background(Color(155, 211, 221))
            ) {
                Text(text = "Loading...")
                AsyncImage(
                    //imageLoader = imageLoader,
                    model = imageURL,
                    contentDescription = cam.name + "Camera",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    //.aspectRatio(8f)
                    .size(width = 396.dp, height = 47.dp)
                    .background(MaterialTheme.colors.primary)
            ){
                Text(text = cam.name, color = Color.White)
            }
        }
        if (userState.state == "Login")
            FavouriteButton(
                function = { addCameraToUserFavourite(userState, cam) },
                isItemFavourite = cam in userState.user.favouriteCameras,
                color = Color.White
            )
    }
}

fun addCameraToUserFavourite(userState: UserState, camera: TrafficCamera): Boolean {
    val user = userState.user
    var output = true

    if (camera in user.favouriteCameras) {
        user.favouriteCameras.remove(camera)
        output = false
    }
    else {
        user.favouriteCameras.add(camera)
    }

    return output
}

@Preview
@Composable
fun TrafficCameraDetailsScreenPreview(){
    TrafficCameraDetailsScreen(cameras = camerasByHighway["CTE"], userState = UserState())
}