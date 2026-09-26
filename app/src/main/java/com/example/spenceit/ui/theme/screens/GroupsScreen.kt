package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spenceit.viewmodel.ExpenseGroup
import com.example.spenceit.viewmodel.Friend
import com.example.spenceit.viewmodel.FriendRequest
import com.example.spenceit.viewmodel.FriendsViewModel
import com.example.spenceit.viewmodel.GroupsViewModel
import com.example.spenceit.viewmodel.RequestsViewModel
import com.example.spenceit.viewmodel.DebtsViewModel

@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    onGroupClick: (String) -> Unit = {}, // 1. Add this parameter
    onFriendClick: (String) -> Unit = {},
    friendsViewModel: FriendsViewModel = viewModel(),
    requestsViewModel: RequestsViewModel = viewModel(),
    groupsViewModel: GroupsViewModel = viewModel(),
    debtsViewModel: DebtsViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Groups", "Friends", "Requests")

    // Friend Search States
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchEmail by remember { mutableStateOf("") }
    val searchResultText by friendsViewModel.searchResultText.collectAsState()
    val foundUser by friendsViewModel.foundUser.collectAsState()

    // Group Creation States
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var selectedGroupMembers by remember { mutableStateOf(setOf<String>()) }

    // Live Data Collections
    val myLiveFriends by friendsViewModel.friendsList.collectAsState()
    val liveRequests by requestsViewModel.pendingRequests.collectAsState()
    val liveGroups by groupsViewModel.myGroups.collectAsState()
    val liveDebts by debtsViewModel.activeDebts.collectAsState()
    val debugText by requestsViewModel.debugText.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (selectedTabIndex != 2) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (selectedTabIndex == 1) showSearchDialog = true
                        else if (selectedTabIndex == 0) showCreateGroupDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text(if (selectedTabIndex == 0) "New Group" else "Add Friend") }
                )
            }
        }
    ) { innerPadding ->

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // TAB CONTENT
            when (selectedTabIndex) {
                0 -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (liveGroups.isEmpty()) {
                            item { Text("No groups yet. Click 'New Group' to start sharing expenses!", modifier = Modifier.padding(16.dp)) }
                        }
                        items(liveGroups) { group ->
                            GroupCard(
                                group = group,
                                onClick = { onGroupClick(group.groupId) } // Trigger the navigation!
                            )
                        }
                    }
                }
                1 -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (myLiveFriends.isEmpty()) {
                            item { Text("You don't have any friends added yet. Click 'Add Friend' below!", modifier = Modifier.padding(16.dp)) }
                        }

                        items(myLiveFriends) { friend ->
                            // Run the Netting Algorithm for this specific friend
                            val sharedDebts = liveDebts.filter { it.otherPersonUid == friend.uid && it.status == "active" }
                            var netBalance = 0.0

                            sharedDebts.forEach { debt ->
                                if (debt.iOweThem) netBalance -= debt.amount
                                else netBalance += debt.amount
                            }

                            FriendCard(
                                friend = friend,
                                netBalance = netBalance, // <-- Pass the final number to the card
                                onClick = { onFriendClick(friend.uid) }
                            )
                        }
                    }
                }
                2 -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Text(text = "System Status: $debugText", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                        }
                        if (liveRequests.isEmpty()) {
                            item { Text("No pending friend requests.", modifier = Modifier.padding(16.dp)) }
                        }
                        items(liveRequests) { request ->
                            FriendRequestCard(
                                request = request,
                                onAccept = { requestsViewModel.respondToRequest(request, true) },
                                onReject = { requestsViewModel.respondToRequest(request, false) }
                            )
                        }
                    }
                }
            }
        }

        // --- RESTORED: THE FRIEND SEARCH DIALOG ---
        if (showSearchDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSearchDialog = false
                    friendsViewModel.resetSearch()
                },
                title = { Text("Find a Friend") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = searchEmail,
                            onValueChange = { searchEmail = it },
                            label = { Text("Friend's Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (searchResultText.isNotEmpty()) {
                            Text(text = searchResultText, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (foundUser != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = foundUser!!.name, fontWeight = FontWeight.Bold)
                                        Text(text = foundUser!!.email, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Button(onClick = { friendsViewModel.sendFriendRequest() }) {
                                        Text("Add")
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (foundUser == null) {
                        Button(onClick = { friendsViewModel.searchByEmail(searchEmail) }) {
                            Text("Search")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSearchDialog = false
                        friendsViewModel.resetSearch()
                    }) {
                        Text("Close")
                    }
                }
            )
        }

        // --- THE CREATE GROUP DIALOG ---
        if (showCreateGroupDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCreateGroupDialog = false
                    newGroupName = ""
                    selectedGroupMembers = emptySet()
                },
                title = { Text("Create New Group") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newGroupName,
                            onValueChange = { newGroupName = it },
                            label = { Text("Group Name") },
                            placeholder = { Text("e.g. Galle Trip") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select Friends:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (myLiveFriends.isEmpty()) {
                            Text("You need to add friends first!", style = MaterialTheme.typography.bodySmall)
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                items(myLiveFriends) { friend ->
                                    val isSelected = selectedGroupMembers.contains(friend.uid)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedGroupMembers = if (isSelected) {
                                                    selectedGroupMembers - friend.uid
                                                } else {
                                                    selectedGroupMembers + friend.uid
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(friend.name)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            groupsViewModel.createGroup(newGroupName, selectedGroupMembers)
                            showCreateGroupDialog = false
                            newGroupName = ""
                            selectedGroupMembers = emptySet()
                        },
                        enabled = newGroupName.isNotBlank() && selectedGroupMembers.isNotEmpty()
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCreateGroupDialog = false
                        newGroupName = ""
                        selectedGroupMembers = emptySet()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun GroupCard(group: ExpenseGroup, onClick: () -> Unit) { // Add onClick here
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() } // Make it clickable
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = group.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = "${group.memberUids.size} members", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// --- RESTORED: FRIEND CARD ---
@Composable
fun FriendCard(
    friend: Friend,
    netBalance: Double, // <-- Accept the calculation
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = friend.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = friend.email, style = MaterialTheme.typography.bodyMedium)
            }

            // Pushes the text to the far right of the card
            Spacer(modifier = Modifier.weight(1f))

            // Display the financial status
            Column(horizontalAlignment = Alignment.End) {
                if (netBalance > 0.01) {
                    Text(text = "Owes you", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Rs. ${abs(netBalance)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                } else if (netBalance < -0.01) {
                    Text(text = "You owe", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    Text(text = "Rs. ${abs(netBalance)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(text = "Settled", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
fun FriendRequestCard(request: FriendRequest, onAccept: () -> Unit, onReject: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${request.senderName} (${request.senderEmail}) sent you a friend request.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onReject) {
                    Icon(Icons.Default.Close, contentDescription = "Reject")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onAccept) {
                    Icon(Icons.Default.Check, contentDescription = "Accept")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            }
        }
    }
}