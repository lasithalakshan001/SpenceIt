package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spenceit.viewmodel.DebtsViewModel
import com.example.spenceit.viewmodel.FriendsViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendUid: String,
    onNavigateBack: () -> Unit,
    friendsViewModel: FriendsViewModel,
    debtsViewModel: DebtsViewModel
) {
    val myFriends by friendsViewModel.friendsList.collectAsState()
    val friend = myFriends.find { it.uid == friendUid }

    val allDebts by debtsViewModel.activeDebts.collectAsState()
    val sharedDebts = allDebts.filter { it.otherPersonUid == friendUid && it.status == "active" }

    var totalIOweThem = 0.0
    var totalTheyOweMe = 0.0

    sharedDebts.forEach { debt ->
        if (debt.iOweThem) totalIOweThem += debt.amount
        else totalTheyOweMe += debt.amount
    }

    val netBalance = totalTheyOweMe - totalIOweThem

    // States for your new custom payment dialog
    var showSettleDialog by remember { mutableStateOf(false) }
    var customPaymentAmount by remember { mutableStateOf("") }
    var paymentNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(friend?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Net Balance", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (netBalance > 0.01) {
                Text(
                    text = "${friend?.name} owes you Rs. ${abs(netBalance)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else if (netBalance < -0.01) {
                Text(
                    text = "You owe ${friend?.name} Rs. ${abs(netBalance)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // The Big Settle Up Button!
                Button(
                    onClick = {
                        // Default the text box to the total amount owed
                        customPaymentAmount = abs(netBalance).toString()
                        showSettleDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Settle Up")
                }
            } else {
                Text(
                    text = "You are all settled up!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Active Transactions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            if (sharedDebts.isEmpty()) {
                Text("No active transactions.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sharedDebts) { debt ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(debt.description)
                                val color = if (debt.iOweThem) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                val prefix = if (debt.iOweThem) "You owe" else "They owe"
                                Text("$prefix Rs. ${debt.amount}", color = color, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // The Custom Payment Dialog
        if (showSettleDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSettleDialog = false
                    paymentNote = ""
                },
                title = { Text("Settle Up") },
                text = {
                    Column {
                        Text("How much are you paying right now?")
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = customPaymentAmount,
                            onValueChange = { customPaymentAmount = it },
                            label = { Text("Amount (Rs.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = paymentNote,
                            onValueChange = { paymentNote = it },
                            label = { Text("Note (Optional)") },
                            placeholder = { Text("e.g. Paid cash at class") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (friend != null) {
                            // USE THE NEW FUNCTION HERE
                            debtsViewModel.sendPaymentRequest(friend.uid, customPaymentAmount, paymentNote)
                        }
                        showSettleDialog = false
                        paymentNote = ""
                    }) {
                        Text("Send Payment")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSettleDialog = false
                        paymentNote = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}