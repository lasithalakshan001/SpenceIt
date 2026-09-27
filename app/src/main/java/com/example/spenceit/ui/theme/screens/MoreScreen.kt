package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth


@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    onLogoutSuccess: () -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 24.dp
            )
    ) {


        // ------------------------------------------------
        // HEADER
        // ------------------------------------------------

        Text(
            text = "More",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Manage your account and app options.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ------------------------------------------------
        // PROFILE / ACCOUNT CARD
        // ------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
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
                            modifier = Modifier.size(26.dp),
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )
                    }
                }


                Spacer(
                    modifier = Modifier.width(14.dp)
                )


                Column {

                    Text(
                        text = "Your Account",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "SpenceIt user",
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
        }


        Spacer(
            modifier = Modifier.height(22.dp)
        )


        // ------------------------------------------------
        // SETTINGS SECTION
        // ------------------------------------------------

        Text(
            text = "Settings & More",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )


        Spacer(
            modifier = Modifier.height(10.dp)
        )


        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
        ) {

            Column {


                MoreItem(
                    icon = Icons.Default.Menu,
                    title = "App",
                    subtitle = "SpenceIt expense sharing"
                )


                HorizontalDivider(
                    modifier = Modifier.padding(
                        start = 64.dp
                    ),
                    color =
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                )


                MoreItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Simple shared expense tracking"
                )
            }
        }


        Spacer(
            modifier = Modifier.weight(1f)
        )


        // ------------------------------------------------
        // LOGOUT
        // ------------------------------------------------

        OutlinedButton(

            onClick = {

                FirebaseAuth
                    .getInstance()
                    .signOut()

                onLogoutSuccess()
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),

            shape = RoundedCornerShape(16.dp),

            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor =
                        MaterialTheme
                            .colorScheme
                            .error
                )

        ) {

            Text(
                text = "Log Out",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text = "You can sign in again anytime.",
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            ),
            style = MaterialTheme.typography.bodySmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}


@Composable
private fun MoreItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 15.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(12.dp),
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
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }


        Spacer(
            modifier = Modifier.width(12.dp)
        )


        Column {

            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .titleSmall
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = subtitle,
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
}