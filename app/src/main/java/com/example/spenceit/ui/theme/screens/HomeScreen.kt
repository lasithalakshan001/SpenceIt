package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spenceit.viewmodel.DebtsViewModel
import com.example.spenceit.viewmodel.GroupsViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToGroups: () -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onGroupClick: (String) -> Unit = {},
    debtsViewModel: DebtsViewModel = viewModel(),
    groupsViewModel: GroupsViewModel = viewModel()
) {
    val liveDebts by debtsViewModel.activeDebts.collectAsState()
    val liveGroups by groupsViewModel.myGroups.collectAsState()

    // 1. Calculate Global Balances
    val activeOnly = liveDebts.filter { it.status == "active" }
    val totalOwedToMe = activeOnly.filter { !it.iOweThem }.sumOf { it.amount }
    val totalIOwe = activeOnly.filter { it.iOweThem }.sumOf { it.amount }

    // 2. Action Queue (Find payments waiting for YOUR approval)
    val pendingCount = liveDebts.count { it.status == "pending_approval" && !it.iOweThem }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- HEADER ---
        item {
            Text("Dashboard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }

        // --- SECTION 1: GLOBAL BALANCES ---
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("You are owed", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rs. $totalOwedToMe", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("You owe", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rs. $totalIOwe", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // --- SECTION 2: ACTION QUEUE ALERT ---
        if (pendingCount > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToDebts() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alert", tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Action Required", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            Text("You have $pendingCount payment(s) waiting for your approval.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // --- SECTION 3: QUICK TRIGGERS ---
        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onNavigateToAdd, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Debt")
                }
                OutlinedButton(onClick = onNavigateToGroups, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Groups")
                }
            }
        }

        // --- SECTION 4: ACTIVE GROUPS TELEMETRY ---
        item {
            Divider()
            Spacer(modifier = Modifier.height(24.dp))
            Text("Recent Groups", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (liveGroups.isEmpty()) {
                Text("No active groups yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        }

        // Only display the first 3 groups to keep the home screen clean
        items(liveGroups.take(3)) { group ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onGroupClick(group.groupId) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = group.name, fontWeight = FontWeight.Bold)
                        Text(text = "${group.memberUids.size} members", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}