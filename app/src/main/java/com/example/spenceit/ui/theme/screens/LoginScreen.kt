package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spenceit.viewmodel.AuthState
import com.example.spenceit.viewmodel.AuthViewModel


@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {

    // ----------------------------------------------------
    // FORM STATE
    // ----------------------------------------------------

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var localError by remember {
        mutableStateOf("")
    }


    // ----------------------------------------------------
    // EXISTING AUTH STATE
    // ----------------------------------------------------

    val authState by
    viewModel.authState.collectAsState()


    // ----------------------------------------------------
    // EXISTING SUCCESS NAVIGATION
    // ----------------------------------------------------

    LaunchedEffect(authState) {

        if (authState is AuthState.Success) {

            onLoginSuccess()
        }
    }


    // ----------------------------------------------------
    // SCREEN
    // ----------------------------------------------------

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // ------------------------------------------------
            // APP ICON
            // ------------------------------------------------

            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(24.dp),
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "S",
                        style =
                            MaterialTheme
                                .typography
                                .headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(22.dp)
            )


            // ------------------------------------------------
            // TITLE
            // ------------------------------------------------

            Text(
                text = "Welcome Back",
                style =
                    MaterialTheme
                        .typography
                        .headlineLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )


            Spacer(
                modifier = Modifier.height(6.dp)
            )


            Text(
                text = "Sign in to continue to SpenceIt.",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            Spacer(
                modifier = Modifier.height(30.dp)
            )


            // ------------------------------------------------
            // EMAIL
            // ------------------------------------------------

            OutlinedTextField(
                value = email,

                onValueChange = {
                    email = it
                },

                label = {
                    Text("Email")
                },

                placeholder = {
                    Text("you@example.com")
                },

                leadingIcon = {

                    Icon(
                        imageVector =
                            Icons.Default.Person,
                        contentDescription = null
                    )
                },

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(16.dp),

                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surface,

                        unfocusedContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    )
            )


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // ------------------------------------------------
            // PASSWORD
            // ------------------------------------------------

            OutlinedTextField(
                value = password,

                onValueChange = {
                    password = it
                },

                label = {
                    Text("Password")
                },

                visualTransformation =
                    PasswordVisualTransformation(),

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(16.dp),

                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surface,

                        unfocusedContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    )
            )


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            // ------------------------------------------------
            // ERROR MESSAGE
            // ------------------------------------------------

            if (localError.isNotEmpty()) {

                LoginErrorCard(
                    message = localError
                )


                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }


            if (authState is AuthState.Error) {

                LoginErrorCard(
                    message =
                        (authState as AuthState.Error)
                            .message
                )


                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }


            Spacer(
                modifier = Modifier.height(6.dp)
            )


            // ------------------------------------------------
            // LOGIN BUTTON
            // ------------------------------------------------

            Button(

                onClick = {

                    localError = ""


                    if (
                        email.isNotEmpty() &&
                        password.isNotEmpty()
                    ) {

                        viewModel.login(
                            email,
                            password
                        )

                    } else {

                        localError =
                            "Please enter both email and password"
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                enabled =
                    authState !is AuthState.Loading,

                shape =
                    RoundedCornerShape(16.dp)

            ) {

                if (
                    authState is
                            AuthState.Loading
                ) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(23.dp),
                        color =
                            MaterialTheme
                                .colorScheme
                                .onPrimary,
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text = "Login",
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            // ------------------------------------------------
            // REGISTER
            // ------------------------------------------------

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "Don't have an account?",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                TextButton(
                    onClick =
                        onNavigateToRegister
                ) {

                    Text(
                        text = "Register",
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }


        // ------------------------------------------------
        // BOTTOM BRAND TEXT
        // ------------------------------------------------

        Text(
            text =
                "Share expenses. Keep it simple.",

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}


// ================================================================
// ERROR CARD
// ================================================================

@Composable
private fun LoginErrorCard(
    message: String
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color =
            MaterialTheme
                .colorScheme
                .errorContainer
    ) {

        Text(
            text = message,
            modifier =
                Modifier.padding(13.dp),
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
            color =
                MaterialTheme
                    .colorScheme
                    .error
        )
    }
}