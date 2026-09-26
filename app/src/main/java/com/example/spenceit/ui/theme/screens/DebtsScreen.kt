package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spenceit.viewmodel.ActiveDebt
import com.example.spenceit.viewmodel.DebtsViewModel

@Composable
fun DebtsScreen(
    modifier: Modifier = Modifier,
    debtsViewModel: DebtsViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active Debts", "Pending Approval")

    val liveDebts by debtsViewModel.activeDebts.collectAsState()

    // 1. State for the Payment Popup Dialog
    var debtToPay by remember { mutableStateOf<ActiveDebt?>(null) }
    var paymentNote by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        if (selectedTabIndex == 0) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val activeOnly = liveDebts.filter { it.status == "active" }
                if (activeOnly.isEmpty()) {
                    item { Text("No active debts right now.", modifier = Modifier.padding(16.dp)) }
                }
                items(activeOnly) { debt ->
                    ActiveDebtCard(
                        debt = debt,
                        onMarkPaidClick = { debtToPay = debt }, // Opens the dialog
                        onAccept = {},
                        onReject = {}
                    )
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val pendingOnly = liveDebts.filter { it.status == "pending_approval" }
                if (pendingOnly.isEmpty()) {
                    item { Text("No pending payments.", modifier = Modifier.padding(16.dp)) }
                }
                items(pendingOnly) { debt ->
                    ActiveDebtCard(
                        debt = debt,
                        onMarkPaidClick = {},
                        onAccept = { debtsViewModel.acceptDebt(debt) },
                        onReject = { debtsViewModel.rejectDebt(debt) }
                    )
                }
            }
        }
    }

    // 4. The Payment Dialog
    if (debtToPay != null) {
        AlertDialog(
            onDismissRequest = {
                debtToPay = null
                paymentNote = ""
            },
            title = { Text("Mark as Paid") },
            text = {
                Column {
                    Text("Send a payment confirmation to ${debtToPay?.otherPersonName} for Rs. ${debtToPay?.amount}.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = { paymentNote = it },
                        label = { Text("Note (Optional)") },
                        placeholder = { Text("e.g. Paid in cash at university") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Send status update with the note
                    debtsViewModel.updateDebtStatus(debtToPay!!.debtId, "pending_approval", paymentNote)
                    debtToPay = null
                    paymentNote = ""
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    debtToPay = null
                    paymentNote = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActiveDebtCard(
    debt: ActiveDebt,
    onMarkPaidClick: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = debt.otherPersonName, fontWeight = FontWeight.Bold)
                }
                val amountColor = if (debt.iOweThem) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                Text(text = "Rs. ${debt.amount}", color = amountColor, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = debt.description, style = MaterialTheme.typography.bodyMedium)

                val statusText = if (debt.status == "pending_approval") "Pending Approval" else "Active"
                val statusColor = if (debt.status == "pending_approval") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline

                Surface(color = statusColor.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                    Text(text = statusText, style = MaterialTheme.typography.labelMedium, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            // 5. Display the note if it exists and the debt is pending
            if (debt.status == "pending_approval" && !debt.settlementNote.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: ${debt.settlementNote}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 6. Borrower Button
            if (debt.iOweThem && debt.status == "active") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onMarkPaidClick, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Paid")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Paid")
                }
            }

            // 7. Lender Buttons (Only visible if you are owed money and it is pending)
            if (!debt.iOweThem && debt.status == "pending_approval") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onReject) { Text("Reject") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept) { Text("Accept Payment") }
                }
            }
        }
    }
}