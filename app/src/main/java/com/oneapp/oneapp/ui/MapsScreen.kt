package com.oneapp.oneapp.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import com.oneapp.oneapp.R
import com.oneapp.oneapp.data.*
import com.oneapp.oneapp.data.camera.TrafficCameraRestApiService
import com.oneapp.oneapp.data.routing.*
import com.oneapp.oneapp.data.routing.classes.Destination
import com.oneapp.oneapp.data.routing.classes.Location
import com.oneapp.oneapp.data.routing.classes.Origin
import com.oneapp.oneapp.data.routing.classes.RouteModifiers
import com.oneapp.oneapp.ui.theme.LoadingAnimation
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime


@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    navController: NavController = rememberNavController(),
    fusedLocationClient: FusedLocationProviderClient,
    placesClient: PlacesClient,
    userState: UserState,
    destinationPlaceId: String,
) {
    val singapore = LatLng(1.35, 103.87)
    val routesModifier = RouteModifiers(false, false, false)
    val context = LocalContext.current

    // Remember Variables
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(singapore, 10f) }
    val origin by remember { mutableStateOf(Origin(Location(userState.user.lastKnownLocation))) }
    var destination by remember { mutableStateOf(Destination(Location(LatLng(0.0, 0.0)))) }
    var encodedpolyline by remember { mutableStateOf("") }
    val camerasOnRoute = remember { mutableStateListOf<TrafficCamera>() }
    val speedCamerasOnRoute = remember { mutableStateListOf<SpeedCamera>() }
    val redLightCamerasOnRoute = remember { mutableStateListOf<RedLightCamera>() }
    var cameraUpdate: CameraUpdate? by remember { mutableStateOf(null) }
    val allPoints = PolyUtil.decode(encodedpolyline)

    LaunchedEffect(cameraUpdate){
        if(cameraUpdate != null)
            cameraPositionState.animate(
                update = cameraUpdate!!
            )
    }

    if(userState.user.lastKnownLocation == LatLng(0.0,0.0)){
        userState.updateLocation(fusedLocationClient)

        navController.navigateUp()
        Toast.makeText(context, "Unable to detect your location!!",
            Toast.LENGTH_LONG).show()
    }

//    println("Origin -> ${origin.location.latLng} Destination -> ${destination.location.latLng}")

    // Get Polyline from Origin to Destination
    LaunchedEffect(destination) {
        println("Entered into launched effect")

        //Get Destination
        if(destination.location.latLng.latitude == 0.0)
            findPlacesByID(destinationPlaceId,placesClient) {
                destination = Destination(Location(LatLng(it.latLng!!.latitude,it.latLng!!.longitude)))
            }

        if(encodedpolyline == "") {
            val time = LocalDateTime.now().plusMinutes(1).toString() + "Z"
            val request = RoutesRequestBody(
                false,
                time, destination, "en-US", origin, routesModifier,
                "TRAFFIC_AWARE"
            )
            RoutesRestApiService().getRoute(request) {
                println("Successful")
                if (it is String) {
                    encodedpolyline = it
                    println(encodedpolyline)
                }
//                else{
//                    navController.navigateUp()
//                    Toast.makeText(context, "Unable to detect route to ${destination.location.latLng}",
//                        Toast.LENGTH_LONG).show()
//                }
            }
        }
    }

    if(encodedpolyline != "") {
        // Get all TrafficCameras that are on route
        val cameras = getAllCameras()
        camerasOnRoute.clear()
        cameras.forEach {
            if (PolyUtil.isLocationOnPath(it.location, allPoints, false, 90.0)) {
                if (it !in camerasOnRoute)
                    camerasOnRoute.add(it)
            }
        }

        val camerasOnRouteSorted = mutableListOf<TrafficCamera>()
        for(i in 0..allPoints.size-2) {
            val fakePolyline = listOf<LatLng>(allPoints[i], allPoints[i+1])
            camerasOnRoute.forEach {
                if(PolyUtil.isLocationOnPath(it.location, fakePolyline, false, 90.0) &&
                        it !in camerasOnRouteSorted)
                    camerasOnRouteSorted.add(it)
            }
        }

        // Get all SpeedCameras that are on route
        speedCamerasOnRoute.clear()
        AllSpeedCameras.forEach {
            if (PolyUtil.isLocationOnPath(it.location, allPoints, false, 90.0)) {
                if (it !in speedCamerasOnRoute)
                    speedCamerasOnRoute.add(it)
            }
        }

        // Get all RedLightCameras that are on route
        redLightCamerasOnRoute.clear()
        AllRedLightCameras.forEach {
            if (PolyUtil.isLocationOnPath(it.location, allPoints, false, 90.0)) {
                if (it !in redLightCamerasOnRoute)
                    redLightCamerasOnRoute.add(it)
            }
        }

        MapsView(
            cameraPositionState = cameraPositionState,
            destination = destination,
            userState = userState,
            camerasOnRoute = camerasOnRouteSorted,
            speedCamerasOnRoute = speedCamerasOnRoute,
            redLightCamerasOnRoute = redLightCamerasOnRoute,
            encodedpolyline = encodedpolyline,
            callback = { cameraUpdate = it }
        )
    }
    else
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement =  Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingAnimation()
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MapsView(cameraPositionState: CameraPositionState,
             destination: Destination,
             userState: UserState,
             camerasOnRoute: List<TrafficCamera>,
             speedCamerasOnRoute: List<SpeedCamera>,
             redLightCamerasOnRoute: List<RedLightCamera>,
             encodedpolyline: String,
             callback: (CameraUpdate) -> Unit
){
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0)


    Column(
        verticalArrangement = Arrangement.SpaceEvenly
    ){
        // Google Maps
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd
        ){
        GoogleMap(
            modifier = Modifier,
            onMapLoaded= {
                callback(CameraUpdateFactory.newLatLngBounds(getBounds(PolyUtil.decode(encodedpolyline)), 20))
            },
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true, isTrafficEnabled = false)
        ) {
            camerasOnRoute.forEach {camera ->
                Marker(
                    state = MarkerState(position = camera.location),
                    title = camera.name,
                    snippet = camera.camera_id.toString(),
//                    onClick = updatePager(pagerState = pagerState, camera = camera, camerasOnRoute = camerasOnRoute),
                    onInfoWindowClick = {
                        _ -> coroutineScope.launch {
                            pagerState.animateScrollToPage(camerasOnRoute.indexOf(camera))
                        }
                    },
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            speedCamerasOnRoute.forEach { speedCamera ->
                Marker(
                    state = MarkerState(position = speedCamera.location),
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.speed_camera)
                )
            }

            redLightCamerasOnRoute.forEach { redLightCamera ->
                Marker(
                    state = MarkerState(position = redLightCamera.location),
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.traffic_light),
                )
            }

            Marker(
                state = MarkerState(position = destination.location.latLng),
                title = "Destination"
            )

            Polyline(points = PolyUtil.decode(encodedpolyline) , color = Color.Cyan, width = 10f)
        }
            Button(onClick = {
                val allPoints = PolyUtil.decode(encodedpolyline)
                val update = CameraUpdateFactory.newLatLngBounds(
                    getBounds(allPoints),
                    20
                )
                callback(update)
            }, modifier = Modifier
                .padding(start = 10.dp)
                .align(Alignment.TopStart)) {
                Text("View Route")
            }
            Text(
                text = "Powered by Google, ©${LocalDate.now().year} Google",
                modifier = Modifier.padding(10.dp)
            )
        }

        // Traffic Camera View
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        {

            val coroutineScopey = rememberCoroutineScope()
            val apiInfo = remember { mutableStateListOf<ApiCameraData>() }
            TrafficCameraRestApiService().getImages {response ->

                for (i in response.items[0].cameras.indices) {
                    val newApiData = ApiCameraData(
                        (response.items[0].cameras[i].camera_id).toInt(),
                        response.items[0].cameras[i].image
                    )
                    apiInfo.add(newApiData)
                }
            }

            if(camerasOnRoute.isEmpty()){
                val color = remember {
                    Animatable(Color.Cyan)
                }

                LaunchedEffect(key1 = Unit) {
                    color.animateTo(Color.Magenta, animationSpec = tween(1000))
                    color.animateTo(Color.Cyan, animationSpec = tween(1000))
                }


                Row(horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color.value)) {
                    Text("Oh WOW your journey is too smooth!!")
                }
            }

            HorizontalPager(
                pageCount = camerasOnRoute.size,
                state = pagerState
            ) {index ->
                val camera = camerasOnRoute[index]
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(4.dp)
                    ) {
                        val apiCameraData = apiInfo.find { it.camera_id == camera.camera_id }
                        val imageURL = apiCameraData?.image
                        Text("Loading...")
                        AsyncImage(
                            model = imageURL,
                            contentDescription = camera.name + "Camera",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    callback(
                                        CameraUpdateFactory.newLatLngZoom(
                                            camerasOnRoute[pagerState.currentPage].location,
                                            15f
                                        )
                                    )
                                }
                        )
                        if(pagerState.canScrollBackward)
                            IconButton(onClick = {
                                coroutineScopey.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage-1)
                                }
                            }, modifier = Modifier.align(alignment = Alignment.CenterStart)) {
                                Icon(imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "left", tint = MaterialTheme.colors.onPrimary)
                            }
                        if(pagerState.canScrollForward)
                            IconButton(onClick = {
                                coroutineScopey.launch{
                                    pagerState.animateScrollToPage(pagerState.currentPage+1)
                                }
                            }, modifier = Modifier.align(alignment = Alignment.CenterEnd)) {
                                Icon(imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "right", tint = MaterialTheme.colors.onPrimary)
                            }
                    }
                    Row(horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()){
                        Text(camera.name)
                    }
                }
            }


            Spacer(Modifier.size(10.dp))
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(camerasOnRoute.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp)
                    )
                }
            }
        }
    }
}

fun getBounds(list: List<LatLng>): LatLngBounds {
    return list.fold ( LatLngBounds.builder()) { builder, it -> builder.include(it) }.build()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun updatePager(pagerState: PagerState, camera: TrafficCamera, camerasOnRoute: List<TrafficCamera>): (Marker) -> Boolean {
    LaunchedEffect(true) {
        pagerState.scrollToPage(camerasOnRoute.indexOf(camera))
    }

    return {true}
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun MapsViewPreview(){
    MapsView(
        cameraPositionState = CameraPositionState(),
        destination = Destination(Location(LatLng(0.0,0.0))),
        encodedpolyline = "",
        userState = UserState(),
        camerasOnRoute = camerasByHighway["PIE"]!!,
        speedCamerasOnRoute = AllSpeedCameras.subList(0,4),
        redLightCamerasOnRoute = AllRedLightCameras.subList(10,12),
        callback = {}
    )
}