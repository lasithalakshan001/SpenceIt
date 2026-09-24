package com.example.spenceit.ui.theme.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(modifier: Modifier = Modifier) {
    // 1. STATE: We need four memory variables now
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // 2. COLUMN: Stacking items vertically again
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Create an Account",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. NAME FIELD
        OutlinedTextField(
            value = name,
            onValueChange = { name = it }, // "it" is a Kotlin shortcut for the incoming text
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. EMAIL FIELD
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. PASSWORD FIELD
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6. CONFIRM PASSWORD FIELD
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 7. REGISTER BUTTON
        Button(
            onClick = {
                println("User wants to register: $name, $email")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}