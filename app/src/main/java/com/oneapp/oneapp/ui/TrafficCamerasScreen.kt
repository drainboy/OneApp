package com.oneapp.oneapp.ui

//import com.oneapp.oneapp.OneAppScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import com.oneapp.oneapp.OneAppScreen
import com.oneapp.oneapp.data.*
import com.oneapp.oneapp.data.camera.TrafficCameraRestApiService

@Composable
fun TrafficCamerasScreen(
    navController: NavController = rememberNavController(),
    userState: UserState
){
    //Text("You have reached the detailed Traffic Camera Page!!")
    Column() {
        // Text(text="You have reached Traffic Cameras!!")
    }
    CameraGridList(navController, userState)
}

@Preview
@Composable
fun TrafficCamerasScreenPreview(){
    TrafficCamerasScreen(userState = UserState())

}



@Composable
fun CameraGridList(navController: NavController, userState: UserState){
    val state = rememberLazyGridState()

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

    LazyVerticalGrid (
        state = state,
        columns = GridCells.Adaptive(150.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {

        // Retrieve list of highways from camerasByHighway map
        val highways = camerasByHighway.keys.toList()
        val highwayImage = highwayImages

        // Generate a Box element for each highway
        items(highways.size) { highway ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .aspectRatio(37f / 30f)
                    .clip(RoundedCornerShape(10.dp))
                    //.background(Color.Red)
            ) {
                // Text(text = "Item $i")
                val cameraId = highwayMapList[highway].camera_id
                val apiCameraData = ApiInfoState.find {it.camera_id == cameraId}
                val image_URL = apiCameraData?.image

                if (image_URL != null) {
                    SquareCard(cat = highways[highway], navController, image_URL)
                }

                //SquareCard(cat = highways[highway], navController, highwayImage.get(highways[highway]))
            }
        }


    }
}

@Composable
fun SquareCard(cat: String, navController: NavController, image: String?) {
    Column {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 185.dp, height = 104.dp)
                .background(Color.LightGray)
                //.background(Color(155, 211, 221))
                .clickable { navController.navigate("${OneAppScreen.TrafficCameraDetails.name}/$cat") }
        ) {
            Text(text = "Loading...")
            AsyncImage(
                model = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 185.dp, height = 47.dp)
                .background(MaterialTheme.colors.primary)
        ) {
            Text(
                text = cat,
                modifier = Modifier
                    .padding(4.dp),
                color = Color.White
            )
        }
    }
}
