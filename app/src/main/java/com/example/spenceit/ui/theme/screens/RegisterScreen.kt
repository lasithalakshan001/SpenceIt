package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
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
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {

    // ----------------------------------------------------
    // FORM STATE
    // ----------------------------------------------------

    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var localError by remember {
        mutableStateOf("")
    }


    // ----------------------------------------------------
    // AUTH STATE
    // ----------------------------------------------------

    val authState by
    viewModel.authState.collectAsState()


    // ----------------------------------------------------
    // EXISTING SUCCESS NAVIGATION
    // ----------------------------------------------------

    LaunchedEffect(authState) {

        if (authState is AuthState.Success) {

            onRegisterSuccess()
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
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(22.dp),
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // ------------------------------------------------
            // HEADER
            // ------------------------------------------------

            Text(
                text = "Create an Account",
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
                text = "Start sharing expenses with your friends.",
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
                modifier = Modifier.height(26.dp)
            )


            // ------------------------------------------------
            // FULL NAME
            // ------------------------------------------------

            OutlinedTextField(
                value = name,

                onValueChange = {
                    name = it
                },

                label = {
                    Text("Full Name")
                },

                placeholder = {
                    Text("Your name")
                },

                singleLine = true,

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

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

                singleLine = true,

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

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

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

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
            // CONFIRM PASSWORD
            // ------------------------------------------------

            OutlinedTextField(
                value = confirmPassword,

                onValueChange = {
                    confirmPassword = it
                },

                label = {
                    Text("Confirm Password")
                },

                visualTransformation =
                    PasswordVisualTransformation(),

                singleLine = true,

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

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
            // LOCAL ERROR
            // ------------------------------------------------

            if (localError.isNotEmpty()) {

                RegisterErrorCard(
                    message = localError
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }


            // ------------------------------------------------
            // FIREBASE ERROR
            // ------------------------------------------------

            if (authState is AuthState.Error) {

                RegisterErrorCard(
                    message =
                        (authState as AuthState.Error)
                            .message
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }


            Spacer(
                modifier = Modifier.height(4.dp)
            )


            // ------------------------------------------------
            // REGISTER BUTTON
            // ------------------------------------------------

            Button(

                onClick = {

                    localError = ""


                    if (
                        password !=
                        confirmPassword
                    ) {

                        localError =
                            "Passwords do not match!"

                    } else if (
                        email.isNotEmpty() &&
                        password.isNotEmpty()
                    ) {

                        viewModel.register(
                            name,
                            email,
                            password
                        )
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
                        text = "Register",
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }


        // ------------------------------------------------
        // BOTTOM TEXT
        // ------------------------------------------------

        Text(
            text = "SpenceIt • Shared expenses made simple",

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
private fun RegisterErrorCard(
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
            modifier = Modifier.padding(13.dp),
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