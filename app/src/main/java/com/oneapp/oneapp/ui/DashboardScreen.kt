package com.oneapp.oneapp.ui

//import com.oneapp.oneapp.OneAppScreen
import android.annotation.SuppressLint
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.*
import com.google.android.libraries.places.ktx.api.net.fetchPlaceRequest
import com.google.android.libraries.places.ktx.api.net.findAutocompletePredictionsRequest
import com.oneapp.oneapp.OneAppScreen
import com.oneapp.oneapp.data.ApiCameraData
import com.oneapp.oneapp.data.UserState
import com.oneapp.oneapp.data.camera.TrafficCameraRestApiService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@SuppressLint("MissingPermission")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    navController : NavController = rememberNavController(),
    fusedLocationClient: FusedLocationProviderClient,
    placesClient: PlacesClient = Places.createClient(LocalContext.current),
    userState: UserState
){

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


    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val textModifier = Modifier.padding(8.dp)
    var destination by remember {
        mutableStateOf("")
    }
    var predictions by remember {
        mutableStateOf(listOf<AutocompletePrediction>())
    }
    var enabled by remember {
        mutableStateOf(false)
    }
    val favouritePlace = remember {
        mutableStateListOf<Place>()
    }

    var queryPlaceId by remember { mutableStateOf("") }

    val favouriteLocation = userState.user.favouriteLocations
    val favouriteCameras = userState.user.favouriteCameras

    if(favouritePlace.size < favouriteLocation.size) {
        if (userState.user.places.size == favouriteLocation.size) {
            userState.user.places.forEach {
                favouritePlace.add(it)
            }
        } else {
            favouriteLocation.take(3).forEach {
                runBlocking {
                    findPlacesByID(it, placesClient) { place ->
                        if(place !in favouritePlace)
                            favouritePlace.add(place)
                        if (place !in userState.user.places)
                            userState.appendPlaces(place)
                    }
                }
            }
        }
    }

    LazyColumn(){
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ){
                Column(modifier = Modifier.weight(3f)){

                    val trailingIconView = @Composable{
                        IconButton(onClick = { destination = ""; enabled = false }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }

                    TextField(
                        value = destination,
                        onValueChange = {
                            destination = it
                            coroutineScope.launch {
                                val request = findAutocompletePredictionsRequest { setQuery(destination).setCountry("SG") }
                                findPredictions(request, placesClient) { response -> predictions = response }
                                enabled = true
                            }
                        },
                        placeholder = {Text("Where to?")},
                        singleLine = true,
                        keyboardActions = KeyboardActions(
                            onDone = {navController.navigate(OneAppScreen.Maps.name.plus("/${predictions[0].placeId}"))}
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        trailingIcon = if(destination.isNotBlank()) trailingIconView else null,
                        modifier = Modifier.onKeyEvent {
                            if(it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER){
                                navController.navigate(OneAppScreen.Maps.name.plus("/${predictions[0].placeId}"))
                            }
                            false
                        }
                    )

                    AnimatedVisibility(visible = enabled) {
                        Card {
                            LazyColumn(modifier = Modifier.height(150.dp)){
                                items(
                                    predictions.size
                                ){
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                destination = predictions[it]
                                                    .getPrimaryText(null)
                                                    .toString()
                                                queryPlaceId = predictions[it].placeId
                                                enabled = false
                                                navController.navigate(
                                                    OneAppScreen.Maps.name.plus("/$queryPlaceId")
                                                )
                                            }
                                    ){
                                        Text(text=predictions[it].getPrimaryText(null).toString())
                                    }
                                    Divider(modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }

                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 5.dp),
                    onClick =  {
                        if(predictions.isNotEmpty()) {
                            queryPlaceId = predictions[0].placeId
                            navController.navigate("${OneAppScreen.Maps.name}/$queryPlaceId")
                        } },
                ) {
                    Text("Let's go")
                }
            }

            // Favourite Locations
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text= "Favourite Locations",
                    modifier = textModifier
                )
//            IconButton(onClick = { userState.updateLocation(fusedLocationClient); originLatLng = userState.user.lastKnownLocation }) {
//                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh location" )
//            }
//            Text(text="${originLatLng.latitude},${originLatLng.longitude}")
            }
        }
        items(favouritePlace.size, {favouritePlace[it].id}) {
            val dismissState = rememberDismissState()
            val place = favouritePlace[it]

            if(dismissState.isDismissed(DismissDirection.EndToStart)){
                println("current favouritePlace $favouritePlace")
                println("current favouriteLocations ${userState.user.favouriteLocations}")
                println("current places ${userState.user.places}")

                favouritePlace.removeAt(it)
                place.id?.let { it1 -> userState.user.favouriteLocations.remove(it1) }
                userState.user.places.remove(place)

                println("after favouritePlace $favouritePlace")
                println("after favouriteLocations ${userState.user.favouriteLocations}")
                println("afer places ${userState.user.places}")
            }

            SwipeToDismiss(
                state = dismissState,
                modifier = Modifier.padding(vertical = 1.dp),
                directions = setOf(DismissDirection.EndToStart),
                dismissThresholds = { direction ->
                    FractionalThreshold(if (direction == DismissDirection.EndToStart) 0.1f else 0.05f)
                },
                background = {
                    val color by animateColorAsState(
                        when (dismissState.targetValue) {
                            DismissValue.Default -> Color.White
                            else -> Color.Red
                        }
                    )
                    val alignment = Alignment.CenterEnd
                    val icon = Icons.Default.Delete

                    val scale by animateFloatAsState(
                        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                    )

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = Dp(20f)),
                        contentAlignment = alignment
                    ) {
                        Icon(
                            icon,
                            contentDescription = "Delete Icon",
                            modifier = Modifier.scale(scale)
                        )
                    }
                },
                dismissContent = {
                    Card(
                        elevation = animateDpAsState(
                            if (dismissState.dismissDirection != null) 8.dp else 4.dp
                        ).value,
                        modifier = Modifier
                            .clickable {
                                navController.navigate(
                                    OneAppScreen.Maps.name.plus("/${place.id}")
                                )
                            }
                            .padding(start = 4.dp, end = 4.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Place, "Place")
                            Column {
                                place.name?.let { it1 -> Text(it1) }
                                place.address?.let { it1 -> Text(it1) }
                            }
                        }
                    }
                }
            )
        }
        item{
            if(favouritePlace.size < 3)
                Card(
                    elevation = 4.dp,
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (userState.state == "Guest")
                                    navController.navigate(OneAppScreen.Login.name)
                                else
                                    navController.navigate(OneAppScreen.Favourite.name)
                            }
                    ) {
                        val text = "Add Favourite Location"
                        val icon = Icons.Default.Add
                        val contentDescription = "Add Location"

                        //                if(favouritePlace.size == 3) {
                        //                    text = "Edit Favourite Locations"
                        //                    icon = Icons.Default.Edit
                        //                    contentDescription = "Edit Location"
                        //                }

                        Icon(imageVector = icon, contentDescription = contentDescription)
                        Text(text)
                    }
                }

            Divider()

            // Favourite Cameras
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text= "Favourite Cameras",
                    modifier = textModifier
                )
                TextButton(onClick = {
                    navController.navigate(OneAppScreen.TrafficCameras.name)
                }) {
                    Text(text = "View All Cameras")
                }
            }

            // Traffic Camera View

            if(favouriteCameras.isNotEmpty()) {
                HorizontalPager(
                    pageCount = favouriteCameras.size,
                    state = pagerState
                ) {

                    val coroutineScopey = rememberCoroutineScope()
                    val camera = favouriteCameras[it]
                    val cameraId = camera.camera_id

                    val apiCameraData = ApiInfoState.find {it.camera_id == cameraId}
                    val image_URL = apiCameraData?.image


                    //val image_URL = "https://images.data.gov.sg/api/traffic-images/2023/04/0c1e53af-1dbd-4657-b94c-39ae573141be.jpg"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "Loading...")
                            AsyncImage(
                                // model = camera.image,
                                model = image_URL,
                                contentDescription = camera.name + "Camera"
                            )
                            if (pagerState.canScrollBackward)
                                IconButton(onClick = {
                                    coroutineScopey.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                }, modifier = Modifier.align(alignment = Alignment.CenterStart)) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "left",
                                        tint = MaterialTheme.colors.onPrimary
                                    )
                                }
                            if (pagerState.canScrollForward)
                                IconButton(onClick = {
                                    coroutineScopey.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }, modifier = Modifier.align(alignment = Alignment.CenterEnd)) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "right",
                                        tint = MaterialTheme.colors.onPrimary
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(camera.name)
                                }
                            }

                    }
                }
                Spacer(Modifier.size(15.dp))
                Row(
                    Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(favouriteCameras.size) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(10.dp)
                        )
                    }
                }
            } else {
                Card(
                    elevation = 4.dp,
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (userState.state == "Guest")
                                    navController.navigate(OneAppScreen.Login.name)
                                else
                                    navController.navigate(OneAppScreen.TrafficCameras.name)
                            }
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Camera")
                        Text("Add Favourite Camera")
                    }
                }
            }
        }
    }
}

suspend fun findPredictions(request: FindAutocompletePredictionsRequest, placesClient: PlacesClient, callback: (MutableList<AutocompletePrediction>) -> Unit) {
    val task: Task<FindAutocompletePredictionsResponse> =
        placesClient.findAutocompletePredictions(request)

    task.addOnSuccessListener { response ->
        val predictions = response.autocompletePredictions

        callback(predictions)
        println("Sample, Got ${predictions.size} predictions.")

    }.addOnFailureListener{
        exception -> println("Sample, Failed to get Place $exception")
    }
}

suspend fun findPlaces(request:FetchPlaceRequest,
                       placesClient: PlacesClient,
                       callback: (Place) -> Unit){
    val task: Task<FetchPlaceResponse> =
        placesClient.fetchPlace(request)

    task.addOnSuccessListener { responseDetails : FetchPlaceResponse ->
        val place = responseDetails.place
        callback(place)
        println("we got the place: $place")
    }.addOnFailureListener { println("Something went wrong with Places Details API") }
}

suspend fun findPlacesByID(id: String, placesClient: PlacesClient, callback: (Place) -> Unit){
    val placeField = listOf(
        Place.Field.NAME,
        Place.Field.LAT_LNG,
        Place.Field.ADDRESS,
        Place.Field.ID
    )
    val request = fetchPlaceRequest(id, placeField)
    findPlaces(request, placesClient, callback)
}

@Preview(showSystemUi = true)
@Composable
fun DashboardPreview(){
    DashboardScreen(userState = UserState(),
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current))
}