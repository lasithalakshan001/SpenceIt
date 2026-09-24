package com.example.spenceit.ui.theme.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // 1. STATE: The memory of what the user is typing
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 2. COLUMN: The container that stacks things from top to bottom
    Column(
        modifier = modifier
            .fillMaxSize() // Take up the whole screen
            .padding(16.dp), // Keep a 16-pixel gap from the phone edges
        verticalArrangement = Arrangement.Center, // Push everything to the middle vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center everything horizontally
    ) {

        Text(
            text = "Welcome to SpenceIt",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp)) // An invisible box to create space

        // 3. EMAIL FIELD
        OutlinedTextField(
            value = email, // What the box currently displays
            onValueChange = { newText -> email = newText }, // When the user types, update the State
            label = { Text("Email") }, // The little hint text
            modifier = Modifier.fillMaxWidth() // Stretch across the screen
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. PASSWORD FIELD
        OutlinedTextField(
            value = password,
            onValueChange = { newText -> password = newText },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(), // Turns text into dots
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 5. LOGIN BUTTON
        Button(
            onClick = {
                // We will tell the ViewModel to authenticate the user here later
                println("User tried to login with: $email")
                onLoginSuccess()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register here")
        }
    }
}