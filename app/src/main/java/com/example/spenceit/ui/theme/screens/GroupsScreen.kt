package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spenceit.viewmodel.DebtsViewModel
import com.example.spenceit.viewmodel.ExpenseGroup
import com.example.spenceit.viewmodel.Friend
import com.example.spenceit.viewmodel.FriendRequest
import com.example.spenceit.viewmodel.FriendsViewModel
import com.example.spenceit.viewmodel.GroupsViewModel
import com.example.spenceit.viewmodel.RequestsViewModel
import kotlin.math.abs


@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    onGroupClick: (String) -> Unit = {},
    onFriendClick: (String) -> Unit = {},
    friendsViewModel: FriendsViewModel = viewModel(),
    requestsViewModel: RequestsViewModel = viewModel(),
    groupsViewModel: GroupsViewModel = viewModel(),
    debtsViewModel: DebtsViewModel = viewModel()
) {

    // ----------------------------------------------------
    // TAB STATE
    // ----------------------------------------------------

    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }

    val tabs = listOf(
        "Groups",
        "Friends",
        "Requests"
    )


    // ----------------------------------------------------
    // FRIEND SEARCH STATES
    // ----------------------------------------------------

    var showSearchDialog by remember {
        mutableStateOf(false)
    }

    var searchEmail by remember {
        mutableStateOf("")
    }

    val searchResultText by
    friendsViewModel.searchResultText.collectAsState()

    val foundUser by
    friendsViewModel.foundUser.collectAsState()


    // ----------------------------------------------------
    // GROUP CREATION STATES
    // ----------------------------------------------------

    var showCreateGroupDialog by remember {
        mutableStateOf(false)
    }

    var newGroupName by remember {
        mutableStateOf("")
    }

    var selectedGroupMembers by remember {
        mutableStateOf(setOf<String>())
    }


    // ----------------------------------------------------
    // LIVE DATA
    // ----------------------------------------------------

    val myLiveFriends by
    friendsViewModel.friendsList.collectAsState()

    val liveRequests by
    requestsViewModel.pendingRequests.collectAsState()

    val liveGroups by
    groupsViewModel.myGroups.collectAsState()

    val liveDebts by
    debtsViewModel.activeDebts.collectAsState()


    // ----------------------------------------------------
    // SCREEN
    // ----------------------------------------------------

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,

        floatingActionButton = {

            if (selectedTabIndex != 2) {

                ExtendedFloatingActionButton(

                    onClick = {

                        when (selectedTabIndex) {

                            0 -> {
                                showCreateGroupDialog = true
                            }

                            1 -> {
                                showSearchDialog = true
                            }
                        }
                    },

                    icon = {

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    },

                    text = {

                        Text(
                            text =
                                if (selectedTabIndex == 0) {
                                    "New Group"
                                } else {
                                    "Add Friend"
                                },
                            fontWeight = FontWeight.SemiBold
                        )
                    },

                    shape = RoundedCornerShape(18.dp),

                    containerColor =
                        MaterialTheme.colorScheme.primary,

                    contentColor =
                        MaterialTheme.colorScheme.onPrimary
                )
            }
        }

    ) { innerPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {


            // ------------------------------------------------
            // PAGE HEADER
            // ------------------------------------------------

            Column(
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 22.dp,
                    bottom = 18.dp
                )
            ) {

                Text(
                    text = "Groups",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = "Manage groups, friends and requests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            // ------------------------------------------------
            // CLEAN TAB SELECTOR
            // ------------------------------------------------

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {

                Row(
                    modifier = Modifier.padding(4.dp)
                ) {

                    tabs.forEachIndexed { index, title ->

                        val selected =
                            selectedTabIndex == index

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedTabIndex = index
                                },
                            shape = RoundedCornerShape(12.dp),
                            color =
                                if (selected) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                            tonalElevation =
                                if (selected) 1.dp else 0.dp
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 6.dp,
                                        vertical = 11.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color =
                                        if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            // ------------------------------------------------
            // TAB CONTENT
            // ------------------------------------------------

            when (selectedTabIndex) {


                // ====================================================
                // GROUPS TAB
                // ====================================================

                0 -> {

                    if (liveGroups.isEmpty()) {

                        GroupsEmptyState(
                            title = "No groups yet",
                            subtitle = "Create a group and start sharing expenses.",
                            buttonText = "Create Group",
                            onClick = {
                                showCreateGroupDialog = true
                            }
                        )

                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 12.dp,
                                bottom = 100.dp
                            ),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            items(
                                items = liveGroups,
                                key = { it.groupId }
                            ) { group ->

                                GroupCard(
                                    group = group,
                                    onClick = {
                                        onGroupClick(group.groupId)
                                    }
                                )
                            }
                        }
                    }
                }


                // ====================================================
                // FRIENDS TAB
                // ====================================================

                1 -> {

                    if (myLiveFriends.isEmpty()) {

                        GroupsEmptyState(
                            title = "No friends yet",
                            subtitle = "Add a friend to start sharing expenses.",
                            buttonText = "Add Friend",
                            onClick = {
                                showSearchDialog = true
                            }
                        )

                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 12.dp,
                                bottom = 100.dp
                            ),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            items(
                                items = myLiveFriends,
                                key = { it.uid }
                            ) { friend ->


                                // ----------------------------------------
                                // EXISTING NET BALANCE LOGIC
                                // ----------------------------------------

                                val sharedDebts =
                                    liveDebts.filter {

                                        it.otherPersonUid == friend.uid &&
                                                it.status == "active"
                                    }


                                var netBalance = 0.0


                                sharedDebts.forEach { debt ->

                                    if (debt.iOweThem) {

                                        netBalance -= debt.amount

                                    } else {

                                        netBalance += debt.amount
                                    }
                                }


                                FriendCard(
                                    friend = friend,
                                    netBalance = netBalance,
                                    onClick = {
                                        onFriendClick(friend.uid)
                                    }
                                )
                            }
                        }
                    }
                }


                // ====================================================
                // REQUESTS TAB
                // ====================================================

                2 -> {

                    if (liveRequests.isEmpty()) {

                        GroupsEmptyState(
                            title = "No pending requests",
                            subtitle = "New friend requests will appear here."
                        )

                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 12.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            items(
                                items = liveRequests,
                                key = { it.requestId }
                            ) { request ->

                                FriendRequestCard(

                                    request = request,

                                    onAccept = {

                                        requestsViewModel
                                            .respondToRequest(
                                                request,
                                                true
                                            )
                                    },

                                    onReject = {

                                        requestsViewModel
                                            .respondToRequest(
                                                request,
                                                false
                                            )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }


        // ============================================================
        // FIND FRIEND DIALOG
        // ============================================================

        if (showSearchDialog) {

            AlertDialog(

                onDismissRequest = {

                    showSearchDialog = false

                    searchEmail = ""

                    friendsViewModel.resetSearch()
                },

                shape = RoundedCornerShape(24.dp),

                containerColor =
                    MaterialTheme.colorScheme.surface,


                title = {

                    Column {

                        Text(
                            text = "Find a Friend",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text = "Search using your friend's email address.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },


                text = {

                    Column {

                        OutlinedTextField(
                            value = searchEmail,

                            onValueChange = {
                                searchEmail = it
                            },

                            label = {
                                Text("Friend's Email")
                            },

                            placeholder = {
                                Text("example@email.com")
                            },

                            singleLine = true,

                            modifier = Modifier.fillMaxWidth(),

                            shape = RoundedCornerShape(14.dp)
                        )


                        if (searchResultText.isNotEmpty()) {

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .primaryContainer
                            ) {

                                Text(
                                    text = searchResultText,
                                    modifier =
                                        Modifier.padding(10.dp),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )
                            }
                        }


                        if (foundUser != null) {

                            Spacer(
                                modifier = Modifier.height(14.dp)
                            )


                            Card(
                                modifier = Modifier.fillMaxWidth(),

                                shape = RoundedCornerShape(16.dp),

                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            MaterialTheme
                                                .colorScheme
                                                .surfaceVariant
                                    )
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {


                                    Surface(
                                        modifier =
                                            Modifier.size(42.dp),
                                        shape = CircleShape,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .primaryContainer
                                    ) {

                                        Box(
                                            modifier =
                                                Modifier.fillMaxSize(),
                                            contentAlignment =
                                                Alignment.Center
                                        ) {

                                            Icon(
                                                imageVector =
                                                    Icons.Default.Person,
                                                contentDescription = null,
                                                tint =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .primary
                                            )
                                        }
                                    }


                                    Spacer(
                                        modifier =
                                            Modifier.width(12.dp)
                                    )


                                    Column(
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = foundUser!!.name,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .titleMedium
                                        )

                                        Text(
                                            text = foundUser!!.email,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant,
                                            maxLines = 1,
                                            overflow =
                                                TextOverflow.Ellipsis
                                        )
                                    }


                                    Button(
                                        onClick = {

                                            friendsViewModel
                                                .sendFriendRequest()
                                        },

                                        shape =
                                            RoundedCornerShape(12.dp),

                                        contentPadding =
                                            PaddingValues(
                                                horizontal = 16.dp
                                            )
                                    ) {

                                        Text("Add")
                                    }
                                }
                            }
                        }
                    }
                },


                confirmButton = {

                    if (foundUser == null) {

                        Button(
                            onClick = {

                                friendsViewModel
                                    .searchByEmail(searchEmail)
                            },

                            enabled =
                                searchEmail.isNotBlank(),

                            shape =
                                RoundedCornerShape(12.dp)
                        ) {

                            Text("Search")
                        }
                    }
                },


                dismissButton = {

                    TextButton(
                        onClick = {

                            showSearchDialog = false

                            searchEmail = ""

                            friendsViewModel.resetSearch()
                        }
                    ) {

                        Text("Close")
                    }
                }
            )
        }


        // ============================================================
        // CREATE GROUP DIALOG
        // ============================================================

        if (showCreateGroupDialog) {

            AlertDialog(

                onDismissRequest = {

                    showCreateGroupDialog = false

                    newGroupName = ""

                    selectedGroupMembers =
                        emptySet()
                },

                shape = RoundedCornerShape(24.dp),

                containerColor =
                    MaterialTheme.colorScheme.surface,


                title = {

                    Column {

                        Text(
                            text = "Create New Group",
                            style =
                                MaterialTheme
                                    .typography
                                    .headlineSmall
                        )

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text = "Create a group and choose who should join.",
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


                text = {

                    Column {

                        OutlinedTextField(
                            value = newGroupName,

                            onValueChange = {
                                newGroupName = it
                            },

                            label = {
                                Text("Group Name")
                            },

                            placeholder = {
                                Text("e.g. Galle Trip")
                            },

                            singleLine = true,

                            modifier = Modifier.fillMaxWidth(),

                            shape = RoundedCornerShape(14.dp)
                        )


                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )


                        Text(
                            text = "Select Friends",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleSmall
                        )


                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )


                        if (myLiveFriends.isEmpty()) {

                            Surface(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceVariant,
                                shape =
                                    RoundedCornerShape(14.dp)
                            ) {

                                Text(
                                    text =
                                        "You need to add friends first.",
                                    modifier =
                                        Modifier.padding(16.dp),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }

                        } else {

                            LazyColumn(
                                modifier =
                                    Modifier.heightIn(
                                        max = 220.dp
                                    ),
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {

                                items(
                                    items = myLiveFriends,
                                    key = { it.uid }
                                ) { friend ->


                                    val isSelected =
                                        selectedGroupMembers
                                            .contains(friend.uid)


                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {

                                                selectedGroupMembers =
                                                    if (isSelected) {

                                                        selectedGroupMembers -
                                                                friend.uid

                                                    } else {

                                                        selectedGroupMembers +
                                                                friend.uid
                                                    }
                                            },

                                        shape =
                                            RoundedCornerShape(12.dp),

                                        color =
                                            if (isSelected) {
                                                MaterialTheme
                                                    .colorScheme
                                                    .primaryContainer
                                            } else {
                                                MaterialTheme
                                                    .colorScheme
                                                    .surface
                                            }
                                    ) {

                                        Row(
                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 7.dp
                                                ),
                                            verticalAlignment =
                                                Alignment.CenterVertically
                                        ) {

                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null
                                            )


                                            Spacer(
                                                modifier =
                                                    Modifier.width(6.dp)
                                            )


                                            Column {

                                                Text(
                                                    text =
                                                        friend.name,
                                                    style =
                                                        MaterialTheme
                                                            .typography
                                                            .bodyMedium,
                                                    fontWeight =
                                                        FontWeight
                                                            .Medium
                                                )

                                                Text(
                                                    text =
                                                        friend.email,
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
                                }
                            }
                        }
                    }
                },


                confirmButton = {

                    Button(

                        onClick = {

                            groupsViewModel.createGroup(
                                newGroupName,
                                selectedGroupMembers
                            )

                            showCreateGroupDialog = false

                            newGroupName = ""

                            selectedGroupMembers =
                                emptySet()
                        },

                        enabled =
                            newGroupName.isNotBlank() &&
                                    selectedGroupMembers.isNotEmpty(),

                        shape =
                            RoundedCornerShape(12.dp)

                    ) {

                        Text("Create")
                    }
                },


                dismissButton = {

                    TextButton(
                        onClick = {

                            showCreateGroupDialog = false

                            newGroupName = ""

                            selectedGroupMembers =
                                emptySet()
                        }
                    ) {

                        Text("Cancel")
                    }
                }
            )
        }
    }
}


// ================================================================
// GROUP CARD
// ================================================================

@Composable
fun GroupCard(
    group: ExpenseGroup,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(18.dp),

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
                .padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {


            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color =
                    MaterialTheme.colorScheme.primaryContainer
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }


            Spacer(
                modifier = Modifier.width(14.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = group.name,
                    style =
                        MaterialTheme.typography.titleMedium,
                    color =
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )


                Spacer(
                    modifier = Modifier.height(3.dp)
                )


                Text(
                    text =
                        "${group.memberUids.size} members",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }


            Text(
                text = "›",
                style =
                    MaterialTheme.typography.titleLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


// ================================================================
// FRIEND CARD
// ================================================================

@Composable
fun FriendCard(
    friend: Friend,
    netBalance: Double,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(18.dp),

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
                .padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {


            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color =
                    MaterialTheme.colorScheme.primaryContainer
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp),
                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }


            Spacer(
                modifier = Modifier.width(13.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = friend.name,
                    style =
                        MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )


                Spacer(
                    modifier = Modifier.height(2.dp)
                )


                Text(
                    text = friend.email,
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }


            Spacer(
                modifier = Modifier.width(10.dp)
            )


            FriendBalanceStatus(
                netBalance = netBalance
            )
        }
    }
}


// ================================================================
// FRIEND BALANCE STATUS
// ================================================================

@Composable
private fun FriendBalanceStatus(
    netBalance: Double
) {

    when {

        netBalance > 0.01 -> {

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    text = "Owes you",
                    style =
                        MaterialTheme.typography.labelSmall,
                    color =
                        MaterialTheme.colorScheme.primary
                )


                Spacer(
                    modifier = Modifier.height(2.dp)
                )


                Text(
                    text =
                        formatGroupMoney(
                            abs(netBalance)
                        ),
                    style =
                        MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.primary
                )
            }
        }


        netBalance < -0.01 -> {

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    text = "You owe",
                    style =
                        MaterialTheme.typography.labelSmall,
                    color =
                        MaterialTheme.colorScheme.error
                )


                Spacer(
                    modifier = Modifier.height(2.dp)
                )


                Text(
                    text =
                        formatGroupMoney(
                            abs(netBalance)
                        ),
                    style =
                        MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.error
                )
            }
        }


        else -> {

            Surface(
                shape = RoundedCornerShape(50),
                color =
                    MaterialTheme
                        .colorScheme
                        .secondaryContainer
            ) {

                Text(
                    text = "Settled",
                    modifier =
                        Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                    style =
                        MaterialTheme.typography.labelSmall,
                    color =
                        MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


// ================================================================
// FRIEND REQUEST CARD
// ================================================================

@Composable
fun FriendRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {

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

        Column(
            modifier = Modifier.padding(16.dp)
        ) {


            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {


                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                ) {

                    Box(
                        modifier =
                            Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Person,
                            contentDescription = null,
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


                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = request.senderName,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(2.dp)
                    )

                    Text(
                        text = request.senderEmail,
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


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            Text(
                text = "Sent you a friend request",
                style =
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {


                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape =
                        RoundedCornerShape(12.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text("Reject")
                }


                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape =
                        RoundedCornerShape(12.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text("Accept")
                }
            }
        }
    }
}


// ================================================================
// EMPTY STATE
// ================================================================

@Composable
private fun GroupsEmptyState(
    title: String,
    subtitle: String,
    buttonText: String? = null,
    onClick: (() -> Unit)? = null
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {


            Surface(
                modifier = Modifier.size(70.dp),
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
                        imageVector =
                            Icons.Default.Person,
                        contentDescription = null,
                        modifier =
                            Modifier.size(32.dp),
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )


            Spacer(
                modifier = Modifier.height(6.dp)
            )


            Text(
                text = subtitle,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            if (
                buttonText != null &&
                onClick != null
            ) {

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )


                OutlinedButton(
                    onClick = onClick,
                    shape =
                        RoundedCornerShape(14.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Add,
                        contentDescription = null,
                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(7.dp)
                    )

                    Text(buttonText)
                }
            }
        }
    }
}


// ================================================================
// MONEY FORMAT
// ================================================================

private fun formatGroupMoney(
    amount: Double
): String {

    return if (amount % 1.0 == 0.0) {

        "Rs. ${amount.toLong()}"

    } else {

        "Rs. %.2f".format(amount)
    }
}