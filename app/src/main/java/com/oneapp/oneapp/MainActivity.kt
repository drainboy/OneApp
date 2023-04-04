package com.oneapp.oneapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.oneapp.oneapp.BuildConfig.GOOGLE_MAPS_API_KEY
import com.oneapp.oneapp.data.UserState
import com.oneapp.oneapp.data.camerasByHighway
import com.oneapp.oneapp.ui.*
import com.oneapp.oneapp.ui.theme.OneAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            } else -> {
                this.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Places.initialize(applicationContext, GOOGLE_MAPS_API_KEY)
        val placesClient = Places.createClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val userState = UserState()

        super.onCreate(savedInstanceState)
        userState.updateLocation(fusedLocationClient)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }

        setContent {
            OneAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    OneAppApp(fusedLocationClient, placesClient, userState)
                }
            }
        }
    }
}

/**
 *  enum values that represent the screens in the app
 */
enum class OneAppScreen(){
    Login,
    Register,
    Dashboard,
    TrafficCameras,
    TrafficCameraDetails,
    Maps,
    Favourite
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@Composable
fun OneAppAppBar(
    currentScreen: OneAppScreen,
    canNavigateBack: Boolean,
    navigateUp: ()-> Unit,
    navigateRefresh: () -> Unit,
    navigateToLogin: () -> Unit,
    userState: UserState,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.CenterEnd){
        TopAppBar(
            title = { Text(currentScreen.name) },
            modifier = modifier,
            navigationIcon = {
                if (canNavigateBack) {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            },
        )
        if(userState.state == "Login" && currentScreen == OneAppScreen.Dashboard)
            IconButton(onClick = navigateRefresh) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        else if(currentScreen == OneAppScreen.Dashboard){
            IconButton(onClick = navigateToLogin) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Login",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    }

}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "CoroutineCreationDuringComposition",
    "MissingPermission"
)
@Composable
fun OneAppApp(
    fusedLocationClient: FusedLocationProviderClient,
    placesClient: PlacesClient,
    userState: UserState,
    navController: NavHostController = rememberNavController()
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = OneAppScreen.valueOf(
        backStackEntry?.destination?.route?.split("/")?.get(0) ?: OneAppScreen.Dashboard.name
    )

    Scaffold(
        topBar = {
            OneAppAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                navigateRefresh = {
                    userState.logout()
                    navController.popBackStack(navController.currentDestination?.id!!, true)
                    navController.navigate(OneAppScreen.Dashboard.name)
                },
                navigateToLogin = { navController.navigate(OneAppScreen.Login.name)},
                userState = userState
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = OneAppScreen.Dashboard.name,
        ) {
            composable(route = OneAppScreen.Login.name) {
                LoginScreen(
                    navController = navController,
                    userState = userState
                )
            }
            composable(route = OneAppScreen.Register.name){
                RegisterScreen(
                    navController = navController,
                    userState = userState
                )
            }
            composable(route = OneAppScreen.Dashboard.name) {
                DashboardScreen(
                    navController = navController,
                    fusedLocationClient = fusedLocationClient,
                    placesClient = placesClient,
                    userState = userState
                )
            }
            composable(route = OneAppScreen.TrafficCameras.name) {
                TrafficCamerasScreen(
                    navController = navController,
                    userState = userState
                )
            }
            composable(
                route = "${OneAppScreen.TrafficCameraDetails.name}/{cat}",
                arguments = listOf(navArgument("cat"){type= NavType.StringType})
            ){

                backStackEntry ->
                TrafficCameraDetailsScreen(
                    navController = navController,
                    cameras = camerasByHighway[backStackEntry.arguments?.getString("cat")],
                    userState = userState
                )
            }
            composable(
                "${OneAppScreen.Maps.name}/{placeId}",
                listOf(navArgument("placeId"){type = NavType.StringType})
            ){ backStackEntry ->
                val placeId = backStackEntry.arguments?.getString("placeId")

                MapScreen(
                    navController = navController,
                    fusedLocationClient = fusedLocationClient,
                    placesClient = placesClient,
                    userState = userState,
                    destinationPlaceId = placeId!!
                )
            }
            composable(
                OneAppScreen.Favourite.name
            ){
                FavouriteScreen(navController,userState)
            }
        }
    }
}