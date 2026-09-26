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
import com.example.spenceit.viewmodel.GroupsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    groupsViewModel: GroupsViewModel
) {
    val liveGroups by groupsViewModel.myGroups.collectAsState()
    val group = liveGroups.find { it.groupId == groupId }

    var description by remember { mutableStateOf("") }
    var showSuccessMsg by remember { mutableStateOf(false) }

    // State to hold how much each person paid
    val contributions = remember { mutableStateMapOf<String, String>() }

    // State to hold the display names so it doesn't just show messy UIDs
    val memberNames = remember { mutableStateMapOf<String, String>() }

    // Fetch names from Firestore when the screen loads
    LaunchedEffect(group) {
        if (group != null) {
            val myUid = FirebaseAuth.getInstance().currentUser?.uid
            val db = FirebaseFirestore.getInstance()

            group.memberUids.forEach { uid ->
                if (uid == myUid) {
                    memberNames[uid] = "Me (You)"
                } else {
                    db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                        memberNames[uid] = doc.getString("name") ?: "Unknown User"
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (group == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Calculate the total bill dynamically based on inputs
        val currentTotal = contributions.values.sumOf { it.toDoubleOrNull() ?: 0.0 }

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Bill: Rs. $currentTotal",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("What was this for?") },
                placeholder = { Text("e.g. Lunch at Sam Sam's") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text("Who paid how much?", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            // The dynamic list of group members and their input fields
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(group.memberUids) { uid ->
                    val name = memberNames[uid] ?: "Loading..."
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = contributions[uid] ?: "",
                            onValueChange = { contributions[uid] = it },
                            label = { Text("Rs.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(130.dp),
                            singleLine = true
                        )
                    }
                }
            }

            if (showSuccessMsg) {
                Text("Calculated and added to Debts!", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    // Convert the text inputs into actual Decimals
                    val parsedContributions = contributions.mapValues { it.value.toDoubleOrNull() ?: 0.0 }

                    groupsViewModel.addComplexSharedExpense(group, parsedContributions, description)
                    contributions.clear()
                    description = ""
                    showSuccessMsg = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentTotal > 0 && description.isNotEmpty()
            ) {
                Text("Split & Settle Balances")
            }
        }
    }
}