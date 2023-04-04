package com.oneapp.oneapp.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.oneapp.oneapp.OneAppScreen
import com.oneapp.oneapp.data.UserState
import com.oneapp.oneapp.data.fakeDatabase

//import com.oneapp.oneapp.OneAppScreen

@Composable
fun LoginScreen(
    navController: NavController = rememberNavController(),
    userState: UserState
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { error = !login(email, password,navController,userState, context)}
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ){
            Button(
                onClick = {
                    error = !login(email, password, navController, userState, context)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Log in", color = MaterialTheme.colors.background)
            }

            Button(
                onClick = { navController.navigate(OneAppScreen.Register.name) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Register")
            }
        }
    
        if(error){
            Text(text = "Something went wrong... Please check your credentials", color = MaterialTheme.colors.error)
        }
    }
}

fun login(email:String, password:String, navController:NavController, userState: UserState, context:Context): Boolean {
    var validated = false

    fakeDatabase.forEach{
        if(it.email == email && it.password == password){
            navController.navigate(OneAppScreen.Dashboard.name, navOptions = navOptions { navController.backQueue.clear() })
            userState.login(it, context)
            Toast.makeText(context, "Welcome $email", Toast.LENGTH_SHORT).show()
            validated = true
        }
    }

    return validated
}

@Preview
@Composable
fun LoginScreenPreview(){
    LoginScreen (userState = UserState())
}