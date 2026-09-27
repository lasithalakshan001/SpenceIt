package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spenceit.viewmodel.ActiveDebt
import com.example.spenceit.viewmodel.DebtsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebtsScreen(
    modifier: Modifier = Modifier,
    debtsViewModel: DebtsViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active", "Pending", "History")

    val liveDebts by debtsViewModel.activeDebts.collectAsState()

    var debtToPay by remember { mutableStateOf<ActiveDebt?>(null) }
    var paymentNote by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        // Modern Segmented Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Bulletproof switch statement (Fixes the Tab crossing bug!)
        when (selectedTabIndex) {
            0 -> {
                // --- ACTIVE TAB ---
                val activeOnly = liveDebts.filter { it.status == "active" }
                if (activeOnly.isEmpty()) {
                    EmptyStateMessage("All caught up!", "You have no active debts.", Icons.Default.ThumbUp)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(activeOnly) { debt ->
                            ModernDebtCard(debt = debt, onMarkPaidClick = { debtToPay = debt }, onAccept = {}, onReject = {})
                        }
                    }
                }
            }
            1 -> {
                // --- PENDING TAB ---
                val pendingOnly = liveDebts.filter { it.status == "pending_approval" }
                if (pendingOnly.isEmpty()) {
                    EmptyStateMessage("No pending requests.", "You're all clear here.", Icons.Default.CheckCircle)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(pendingOnly) { debt ->
                            ModernDebtCard(debt = debt, onMarkPaidClick = {}, onAccept = { debtsViewModel.acceptDebt(debt) }, onReject = { debtsViewModel.rejectDebt(debt) })
                        }
                    }
                }
            }
            2 -> {
                // --- HISTORY TAB ---
                val historyOnly = liveDebts.filter { it.status == "settled" }.sortedByDescending { it.timestamp }
                if (historyOnly.isEmpty()) {
                    EmptyStateMessage("No recent history.", "Settled debts will appear here.", Icons.Default.Check)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(historyOnly) { debt -> HistoryDebtCard(debt = debt) }
                    }
                }
            }
        }
    }

    // Payment Dialog
    if (debtToPay != null) {
        AlertDialog(
            onDismissRequest = { debtToPay = null; paymentNote = "" },
            title = { Text("Mark as Paid") },
            text = {
                Column {
                    Text("Send a payment confirmation to ${debtToPay?.otherPersonName} for Rs. ${debtToPay?.amount}.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = { paymentNote = it },
                        label = { Text("Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    debtsViewModel.updateDebtStatus(debtToPay!!.debtId, "pending_approval", paymentNote)
                    debtToPay = null
                    paymentNote = ""
                }) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { debtToPay = null; paymentNote = "" }) { Text("Cancel") } }
        )
    }
}

// --- NEW UI COMPONENTS ---

@Composable
fun EmptyStateMessage(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun ModernDebtCard(debt: ActiveDebt, onMarkPaidClick: () -> Unit, onAccept: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = debt.otherPersonName, fontWeight = FontWeight.Bold)
                            Text(text = debt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    val amountColor = if (debt.iOweThem) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    Text(text = "Rs. ${debt.amount}", color = amountColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                if (debt.status == "pending_approval" && !debt.settlementNote.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Note: ${debt.settlementNote}", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Full-width buttons at the bottom of the card
            if (debt.iOweThem && debt.status == "active") {
                Button(onClick = onMarkPaidClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)) {
                    Text("Mark as Paid")
                }
            } else if (!debt.iOweThem && debt.status == "pending_approval") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(bottomStart = 16.dp)) { Text("Reject") }
                    Button(onClick = onAccept, modifier = Modifier.weight(1f), shape = RoundedCornerShape(bottomEnd = 16.dp)) { Text("Accept") }
                }
            }
        }
    }
}

@Composable
fun HistoryDebtCard(debt: ActiveDebt) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(debt.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = "Settled", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Settled with ${debt.otherPersonName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                Text(text = dateString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Text(text = "Rs. ${debt.amount}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
        }
    }
}