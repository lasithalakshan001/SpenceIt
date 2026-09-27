package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spenceit.viewmodel.GroupsViewModel
import com.example.spenceit.viewmodel.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    groupsViewModel: GroupsViewModel,
    friendsViewModel: FriendsViewModel // <-- Added FriendsViewModel
) {
    val liveGroups by groupsViewModel.myGroups.collectAsState()
    val group = liveGroups.find { it.groupId == groupId }

    val myLiveFriends by friendsViewModel.friendsList.collectAsState()

    var description by remember { mutableStateOf("") }
    var showSuccessMsg by remember { mutableStateOf(false) }

    val contributions = remember { mutableStateMapOf<String, String>() }
    val memberNames = remember { mutableStateMapOf<String, String>() }
    val myUid = FirebaseAuth.getInstance().currentUser?.uid

    // --- Management States ---
    var expandedMenu by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var selectedNewMembers by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(group) {
        if (group != null) {
            val db = FirebaseFirestore.getInstance()
            group.memberUids.forEach { uid ->
                if (uid == myUid) {
                    memberNames[uid] = "Me (You)"
                } else if (!memberNames.containsKey(uid)) {
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
                },
                actions = {
                    // The 3-dot options menu
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Members") },
                            onClick = {
                                expandedMenu = false
                                showAddMemberDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Group", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expandedMenu = false
                                groupsViewModel.deleteGroup(groupId)
                                onNavigateBack() // Kick the user back to the list since the group is gone!
                            }
                        )
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
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("What was this for?") },
                placeholder = { Text("e.g. Lunch at Sam Sam's") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Group Members & Contributions", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(group.memberUids) { uid ->
                    val name = memberNames[uid] ?: "Loading..."
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Member Name
                        Text(text = name, modifier = Modifier.weight(1f))

                        // Remove Button (Hide it for yourself so you don't accidentally leave your own group)
                        if (uid != myUid) {
                            IconButton(onClick = { groupsViewModel.removeMemberFromGroup(groupId, uid) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        // Contribution Input
                        OutlinedTextField(
                            value = contributions[uid] ?: "",
                            onValueChange = { contributions[uid] = it },
                            label = { Text("Rs.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp),
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

        // --- Dialog for Adding New Members ---
        if (showAddMemberDialog) {
            // Find friends who are NOT already in this group
            val availableFriends = myLiveFriends.filter { !group.memberUids.contains(it.uid) }

            AlertDialog(
                onDismissRequest = {
                    showAddMemberDialog = false
                    selectedNewMembers = emptySet()
                },
                title = { Text("Add Friends to Group") },
                text = {
                    if (availableFriends.isEmpty()) {
                        Text("All of your friends are already in this group!")
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(availableFriends) { friend ->
                                val isSelected = selectedNewMembers.contains(friend.uid)
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedNewMembers = if (isSelected) selectedNewMembers - friend.uid
                                        else selectedNewMembers + friend.uid
                                    }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = isSelected, onCheckedChange = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(friend.name)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            groupsViewModel.addMembersToGroup(groupId, selectedNewMembers.toList())
                            showAddMemberDialog = false
                            selectedNewMembers = emptySet()
                        },
                        enabled = selectedNewMembers.isNotEmpty()
                    ) {
                        Text("Add to Group")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddMemberDialog = false
                        selectedNewMembers = emptySet()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}