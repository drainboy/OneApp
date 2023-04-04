package com.oneapp.oneapp.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.oneapp.oneapp.OneAppScreen
import com.oneapp.oneapp.data.User
import com.oneapp.oneapp.data.UserState
import com.oneapp.oneapp.data.fakeDatabase

@Composable
fun RegisterScreen(
    navController : NavController = rememberNavController(),
    userState: UserState
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var validatePassword by remember { mutableStateOf("") }
    var error by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Register",
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
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = validatePassword,
            onValueChange = { validatePassword = it },
            label = { Text("Reenter Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ){
            val context = LocalContext.current

            Button(
                onClick = {
                    if(password == validatePassword) {
                        userState.register(email, password, context)
                        navController.navigate(OneAppScreen.Dashboard.name){
                            popUpTo(OneAppScreen.Dashboard.name)
                        }
                    } else error = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Register")
            }
        }

        if(error){
            Text(text = "Password is not the same!!", color = Color.Red)
        }
    }
}

@Preview
@Composable
fun RegisterScreenPreview(){
    RegisterScreen(userState = UserState())
}