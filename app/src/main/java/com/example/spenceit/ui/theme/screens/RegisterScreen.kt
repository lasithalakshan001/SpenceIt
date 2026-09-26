package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // You might need to press Alt+Enter to import this
import com.example.spenceit.viewmodel.AuthState
import com.example.spenceit.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit, // The remote control to go to the Home screen
    viewModel: AuthViewModel = viewModel() // 1. Bring in the Kitchen Expediter
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") } // Used for checking if passwords match

    // 2. Watch the ViewModel's state. If it changes, Jetpack Compose instantly redraws!
    val authState by viewModel.authState.collectAsState()

    // 3. LaunchedEffect runs code automatically when a variable changes.
    // If authState turns into "Success", it clicks the remote to go to Home.
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create an Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        // 4. Show Errors if there are any
        if (localError.isNotEmpty()) {
            Text(text = localError, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (authState is AuthState.Error) {
            // Read the error message sent from Firebase
            Text(text = (authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 5. Button Logic
        Button(
            onClick = {
                localError = "" // Clear old errors
                if (password != confirmPassword) {
                    localError = "Passwords do not match!"
                } else if (email.isNotEmpty() && password.isNotEmpty()) {
                    // Tell the ViewModel to talk to Firebase!
                    viewModel.register(name, email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            // Disable the button if Firebase is currently loading
            enabled = authState !is AuthState.Loading
        ) {
            // If loading, show a small spinning wheel inside the button. If not, show "Register"
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Register")
            }
        }
    }
}