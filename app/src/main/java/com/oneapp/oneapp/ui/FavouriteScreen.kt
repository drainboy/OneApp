package com.oneapp.oneapp.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.oneapp.oneapp.data.UserState
import com.oneapp.oneapp.data.routing.getRequestInSG

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FavouriteScreen(
    navController: NavController,
    userState: UserState
){
    val context = LocalContext.current
    val placesClient = Places.createClient(context)
    var query by remember{ mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    var predictions by remember {
        mutableStateOf(listOf<AutocompletePrediction>())
    }

    LaunchedEffect(query){
        val request = getRequestInSG(query)
        findPredictions(request, placesClient) { predictions = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
            },
            label = { Text("Search Location") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
            },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn{

            items(predictions.size, {predictions[it].placeId}){
                val prediction = predictions[it]

                Box(contentAlignment = Alignment.CenterEnd) {
                    Card(modifier = Modifier.padding(8.dp)){
                        Row(modifier = Modifier.fillMaxWidth()){
                            Column {
                                Text(prediction.getPrimaryText(null).toString())
                                Text(prediction.getSecondaryText(null).toString())
                            }
                        }
                    }
                    FavouriteButton(
                        function = {
                            addToUserFavourite(userState, prediction.placeId,
                                context)
                        },
                        prediction.placeId in userState.user.favouriteLocations
                    )
                }
            }
        }
    }
}

fun addToUserFavourite(userState: UserState, placeId: String, context: Context): Boolean {
    val user = userState.user
    var output = true

    if (placeId in user.favouriteLocations) {
        user.favouriteLocations.remove(placeId)
        output = false
    }
    else if(user.favouriteLocations.size < 3) {
        user.favouriteLocations.add(placeId)
    }
    else {
        Toast.makeText(context, "Cannot favourite more than 3 locations!!", Toast.LENGTH_SHORT).show()
    }

    return output
}

@Composable
fun FavouriteButton(
    function: () -> Boolean,
    isItemFavourite: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary
) {
    var isFavourite by remember { mutableStateOf(isItemFavourite) }

    IconToggleButton(
        checked = isFavourite,
        onCheckedChange = {
            isFavourite = function()
        }
    ) {
        Icon(
            tint = color,

            imageVector = if (isFavourite) {
                Icons.Filled.Favorite
            } else {
                Icons.Default.FavoriteBorder
            },
            contentDescription = null
        )
    }
}

@Preview
@Composable
fun FavouritesPreview(){
    FavouriteScreen(navController = rememberNavController(), userState = UserState())
}