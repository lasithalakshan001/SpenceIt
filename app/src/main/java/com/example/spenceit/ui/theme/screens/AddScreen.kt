package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spenceit.viewmodel.DebtsViewModel
import com.example.spenceit.viewmodel.Friend
import com.example.spenceit.viewmodel.FriendsViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    modifier: Modifier = Modifier,
    friendsViewModel: FriendsViewModel = viewModel(),
    debtsViewModel: DebtsViewModel = viewModel()
) {

    // ----------------------------------------------------
    // EXISTING LIVE DATA
    // ----------------------------------------------------

    val friendsList by
    friendsViewModel.friendsList.collectAsState()

    val saveStatus by
    debtsViewModel.saveStatus.collectAsState()


    // ----------------------------------------------------
    // EXISTING FORM STATE
    // ----------------------------------------------------

    var amount by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var selectedFriend by remember {
        mutableStateOf<Friend?>(null)
    }

    var isExpanded by remember {
        mutableStateOf(false)
    }

    var iAmLender by remember {
        mutableStateOf(true)
    }


    // ----------------------------------------------------
    // EXISTING SUCCESS RESET LOGIC
    // ----------------------------------------------------

    LaunchedEffect(saveStatus) {

        if (saveStatus == "Success! Debt saved.") {

            delay(2000)

            amount = ""

            description = ""

            selectedFriend = null

            iAmLender = true

            debtsViewModel.resetStatus()
        }
    }


    // ----------------------------------------------------
    // SCREEN
    // ----------------------------------------------------

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
            text = "Create a New Debt",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Add a shared expense and keep balances updated.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // ------------------------------------------------
        // FRIEND SELECTOR
        // ------------------------------------------------

        Text(
            text = "Who is this with?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )


        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = {
                isExpanded = !isExpanded
            }
        ) {

            OutlinedTextField(
                value =
                    selectedFriend?.name
                        ?: "Select a Friend",

                onValueChange = {},

                readOnly = true,

                singleLine = true,

                leadingIcon = {

                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primaryContainer
                    ) {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Person,
                                contentDescription = null,
                                modifier =
                                    Modifier.size(18.dp),
                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )
                        }
                    }
                },

                trailingIcon = {

                    ExposedDropdownMenuDefaults
                        .TrailingIcon(
                            expanded = isExpanded
                        )
                },

                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),

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
                                .surface,
                        focusedBorderColor =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        unfocusedBorderColor =
                            MaterialTheme
                                .colorScheme
                                .outline
                    )
            )


            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = {
                    isExpanded = false
                }
            ) {

                if (friendsList.isEmpty()) {

                    DropdownMenuItem(
                        text = {
                            Text(
                                "No friends found. Add some in Groups!"
                            )
                        },
                        onClick = {
                            isExpanded = false
                        }
                    )

                } else {

                    friendsList.forEach { friend ->

                        DropdownMenuItem(

                            text = {

                                Column {

                                    Text(
                                        text = friend.name,
                                        fontWeight =
                                            FontWeight.Medium
                                    )

                                    Text(
                                        text = friend.email,
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
                            },

                            onClick = {

                                selectedFriend = friend

                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ------------------------------------------------
        // AMOUNT
        // ------------------------------------------------

        Text(
            text = "Amount",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )


        OutlinedTextField(
            value = amount,

            onValueChange = {
                amount = it
            },

            label = {
                Text("Amount (Rs.)")
            },

            placeholder = {
                Text("0.00")
            },

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                ),

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
            modifier = Modifier.height(20.dp)
        )


        // ------------------------------------------------
        // DESCRIPTION
        // ------------------------------------------------

        Text(
            text = "What was it for?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )


        OutlinedTextField(
            value = description,

            onValueChange = {
                description = it
            },

            label = {
                Text("Description")
            },

            placeholder = {
                Text("e.g. Dinner at Sam Sam's")
            },

            modifier = Modifier.fillMaxWidth(),

            minLines = 2,

            maxLines = 3,

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
            modifier = Modifier.height(24.dp)
        )


        // ------------------------------------------------
        // TRANSACTION TYPE
        // ------------------------------------------------

        Text(
            text = "Transaction Type",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {


            FilterChip(
                selected = iAmLender,

                onClick = {
                    iAmLender = true
                },

                label = {
                    Text("They owe me")
                },

                modifier = Modifier.weight(1f)
            )


            FilterChip(
                selected = !iAmLender,

                onClick = {
                    iAmLender = false
                },

                label = {
                    Text("I owe them")
                },

                modifier = Modifier.weight(1f)
            )
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // ------------------------------------------------
        // STATUS MESSAGE
        // ------------------------------------------------

        if (saveStatus.isNotEmpty()) {

            val isError =
                saveStatus.contains("Error") ||
                        saveStatus.contains("Failed")


            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color =
                    if (isError) {
                        MaterialTheme
                            .colorScheme
                            .errorContainer
                    } else {
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                    }
            ) {

                Text(
                    text = saveStatus,
                    modifier =
                        Modifier.padding(14.dp),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color =
                        if (isError) {
                            MaterialTheme
                                .colorScheme
                                .error
                        } else {
                            MaterialTheme
                                .colorScheme
                                .primary
                        }
                )
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }


        Spacer(
            modifier = Modifier.weight(1f)
        )


        // ------------------------------------------------
        // SAVE BUTTON
        // ------------------------------------------------

        Button(

            onClick = {

                if (selectedFriend != null) {

                    debtsViewModel.createDebt(
                        selectedFriend!!.uid,
                        amount,
                        description,
                        iAmLender
                    )
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),

            enabled =
                selectedFriend != null &&
                        amount.isNotEmpty() &&
                        description.isNotEmpty() &&
                        saveStatus != "Saving...",

            shape = RoundedCornerShape(16.dp)

        ) {

            if (saveStatus == "Saving...") {

                CircularProgressIndicator(
                    modifier =
                        Modifier.size(22.dp),
                    color =
                        MaterialTheme
                            .colorScheme
                            .onPrimary,
                    strokeWidth = 2.dp
                )

            } else {

                Icon(
                    imageVector =
                        Icons.Default.Add,
                    contentDescription = null,
                    modifier =
                        Modifier.size(19.dp)
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Save Debt",
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge
                )
            }
        }
    }
}