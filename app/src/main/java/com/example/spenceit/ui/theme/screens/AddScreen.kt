package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
    debtsViewModel: DebtsViewModel = viewModel() // 1. Bring in the new Expediter
) {
    val friendsList by friendsViewModel.friendsList.collectAsState()
    val saveStatus by debtsViewModel.saveStatus.collectAsState() // 2. Listen to the save status

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    var iAmLender by remember { mutableStateOf(true) }

    // 3. Automatically clear the form when successful
    LaunchedEffect(saveStatus) {
        if (saveStatus == "Success! Debt saved.") {
            delay(2000) // Show the success message for 2 seconds
            amount = ""
            description = ""
            selectedFriend = null
            iAmLender = true
            debtsViewModel.resetStatus()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create a New Debt", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }
        ) {
            OutlinedTextField(
                value = selectedFriend?.name ?: "Select a Friend",
                onValueChange = {},
                readOnly = true,
                label = { Text("Who is this with?") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                if (friendsList.isEmpty()) {
                    DropdownMenuItem(text = { Text("No friends found. Add some in Groups!") }, onClick = { isExpanded = false })
                } else {
                    friendsList.forEach { friend ->
                        DropdownMenuItem(
                            text = { Text(friend.name) },
                            onClick = {
                                selectedFriend = friend
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (Rs.)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("What was it for?") },
            placeholder = { Text("e.g. Dinner at Sam Sam's") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text("Transaction Type", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FilterChip(selected = iAmLender, onClick = { iAmLender = true }, label = { Text("They owe me") })
            FilterChip(selected = !iAmLender, onClick = { iAmLender = false }, label = { Text("I owe them") })
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 4. Show the loading or success message
        if (saveStatus.isNotEmpty()) {
            val textColor = if (saveStatus.contains("Error") || saveStatus.contains("Failed"))
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary

            Text(text = saveStatus, color = textColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                // 5. Send the data to the ViewModel!
                if (selectedFriend != null) {
                    debtsViewModel.createDebt(selectedFriend!!.uid, amount, description, iAmLender)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedFriend != null && amount.isNotEmpty() && description.isNotEmpty() && saveStatus != "Saving..."
        ) {
            if (saveStatus == "Saving...") {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Save Debt")
            }
        }
    }
}