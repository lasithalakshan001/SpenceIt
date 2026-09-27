package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spenceit.viewmodel.FriendsViewModel
import com.example.spenceit.viewmodel.GroupsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    groupsViewModel: GroupsViewModel,
    friendsViewModel: FriendsViewModel
) {

    val liveGroups by groupsViewModel.myGroups.collectAsState()
    val group = liveGroups.find { it.groupId == groupId }

    val myLiveFriends by friendsViewModel.friendsList.collectAsState()

    var description by remember { mutableStateOf("") }
    var showSuccessMsg by remember { mutableStateOf(false) }

    val contributions = remember {
        mutableStateMapOf<String, String>()
    }

    val memberNames = remember {
        mutableStateMapOf<String, String>()
    }

    val myUid = FirebaseAuth.getInstance().currentUser?.uid

    var expandedMenu by remember {
        mutableStateOf(false)
    }

    var showAddMemberDialog by remember {
        mutableStateOf(false)
    }

    var selectedNewMembers by remember {
        mutableStateOf(setOf<String>())
    }


    // ----------------------------------------------------
    // EXISTING MEMBER NAME LOADING
    // ----------------------------------------------------

    LaunchedEffect(group) {

        if (group != null) {

            val db = FirebaseFirestore.getInstance()

            group.memberUids.forEach { uid ->

                if (uid == myUid) {

                    memberNames[uid] = "Me (You)"

                } else if (!memberNames.containsKey(uid)) {

                    db.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { doc ->

                            memberNames[uid] =
                                doc.getString("name")
                                    ?: "Unknown User"
                        }
                }
            }
        }
    }


    // ----------------------------------------------------
    // SCREEN
    // ----------------------------------------------------

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = group?.name ?: "Loading...",
                        style = MaterialTheme.typography.titleLarge
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onNavigateBack
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            expandedMenu = true
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }


                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = {
                            expandedMenu = false
                        }
                    ) {

                        DropdownMenuItem(

                            text = {
                                Text("Add Members")
                            },

                            onClick = {

                                expandedMenu = false

                                showAddMemberDialog = true
                            }
                        )


                        DropdownMenuItem(

                            text = {

                                Text(
                                    text = "Delete Group",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },

                            onClick = {

                                expandedMenu = false

                                groupsViewModel.deleteGroup(groupId)

                                onNavigateBack()
                            }
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.background
                    )
            )
        }

    ) { innerPadding ->


        // ------------------------------------------------
        // LOADING STATE
        // ------------------------------------------------

        if (group == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator()
            }

            return@Scaffold
        }


        // ------------------------------------------------
        // EXISTING TOTAL CALCULATION
        // ------------------------------------------------

        val currentTotal =
            contributions.values.sumOf {
                it.toDoubleOrNull() ?: 0.0
            }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
        ) {


            // ------------------------------------------------
            // TOTAL BILL CARD
            // ------------------------------------------------

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Total Bill",
                        style = MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )


                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )


                    Text(
                        text = formatGroupDetailMoney(currentTotal),
                        style =
                            MaterialTheme.typography.headlineMedium,
                        color =
                            MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )


                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )


                    Text(
                        text =
                            "${group.memberUids.size} members in this group",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // ------------------------------------------------
            // DESCRIPTION
            // ------------------------------------------------

            Text(
                text = "What was this for?",
                style = MaterialTheme.typography.titleSmall
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
                    Text("e.g. Lunch at Sam Sam's")
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

                maxLines = 3
            )


            Spacer(
                modifier = Modifier.height(22.dp)
            )


            // ------------------------------------------------
            // MEMBERS HEADER
            // ------------------------------------------------

            Text(
                text = "Members & Contributions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )


            Spacer(
                modifier = Modifier.height(4.dp)
            )


            Text(
                text = "Enter how much each person paid.",
                style = MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // ------------------------------------------------
            // MEMBER LIST
            // ------------------------------------------------

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(
                    items = group.memberUids,
                    key = { it }
                ) { uid ->

                    val name =
                        memberNames[uid]
                            ?: "Loading..."


                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
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
                                .padding(14.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {


                            Surface(
                                modifier = Modifier.size(42.dp),
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
                                            Modifier.size(21.dp),
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
                                    text = name,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleSmall
                                )


                                if (uid == myUid) {

                                    Spacer(
                                        modifier =
                                            Modifier.height(2.dp)
                                    )

                                    Text(
                                        text = "Your contribution",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .labelSmall,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                    )
                                }
                            }


                            // ----------------------------------------
                            // EXISTING MEMBER REMOVE ACTION
                            // ----------------------------------------

                            if (uid != myUid) {

                                IconButton(
                                    onClick = {

                                        groupsViewModel
                                            .removeMemberFromGroup(
                                                groupId,
                                                uid
                                            )
                                    }
                                ) {

                                    Icon(
                                        imageVector =
                                            Icons.Default.Clear,
                                        contentDescription =
                                            "Remove",
                                        tint =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
                                }
                            }


                            Spacer(
                                modifier = Modifier.width(4.dp)
                            )


                            OutlinedTextField(
                                value =
                                    contributions[uid]
                                        ?: "",

                                onValueChange = {
                                    contributions[uid] = it
                                },

                                label = {
                                    Text("Rs.")
                                },

                                placeholder = {
                                    Text("0")
                                },

                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType =
                                            KeyboardType.Number
                                    ),

                                modifier =
                                    Modifier.width(105.dp),

                                singleLine = true,

                                shape =
                                    RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }


            // ------------------------------------------------
            // SUCCESS MESSAGE
            // ------------------------------------------------

            if (showSuccessMsg) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )


                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color =
                        MaterialTheme
                            .colorScheme
                            .secondaryContainer
                ) {

                    Text(
                        text =
                            "Calculated and added to Debts!",
                        modifier =
                            Modifier.padding(14.dp),
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        fontWeight =
                            FontWeight.Medium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .secondary
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            // ------------------------------------------------
            // SPLIT BUTTON
            // ------------------------------------------------

            Button(

                onClick = {

                    val parsedContributions =
                        contributions.mapValues {
                            it.value.toDoubleOrNull()
                                ?: 0.0
                        }


                    groupsViewModel
                        .addComplexSharedExpense(
                            group,
                            parsedContributions,
                            description
                        )


                    contributions.clear()

                    description = ""

                    showSuccessMsg = true
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                enabled =
                    currentTotal > 0 &&
                            description.isNotEmpty(),

                shape =
                    RoundedCornerShape(16.dp)

            ) {

                Text(
                    text = "Split & Settle Balances",
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge
                )
            }
        }


        // ============================================================
        // ADD MEMBERS DIALOG
        // ============================================================

        if (showAddMemberDialog) {

            val availableFriends =
                myLiveFriends.filter {
                    !group.memberUids
                        .contains(it.uid)
                }


            AlertDialog(

                onDismissRequest = {

                    showAddMemberDialog = false

                    selectedNewMembers =
                        emptySet()
                },

                shape = RoundedCornerShape(24.dp),

                containerColor =
                    MaterialTheme.colorScheme.surface,


                title = {

                    Column {

                        Text(
                            text = "Add Friends to Group",
                            style =
                                MaterialTheme
                                    .typography
                                    .headlineSmall
                        )


                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )


                        Text(
                            text =
                                "Choose friends to add to ${group.name}.",
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

                    if (availableFriends.isEmpty()) {

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(14.dp),
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                        ) {

                            Text(
                                text =
                                    "All of your friends are already in this group!",
                                modifier =
                                    Modifier.padding(16.dp),
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium
                            )
                        }

                    } else {

                        LazyColumn(
                            modifier =
                                Modifier.heightIn(
                                    max = 260.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(5.dp)
                        ) {

                            items(
                                items =
                                    availableFriends,
                                key = {
                                    it.uid
                                }
                            ) { friend ->


                                val isSelected =
                                    selectedNewMembers
                                        .contains(friend.uid)


                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {

                                            selectedNewMembers =
                                                if (isSelected) {

                                                    selectedNewMembers -
                                                            friend.uid

                                                } else {

                                                    selectedNewMembers +
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
                                                horizontal =
                                                    8.dp,
                                                vertical =
                                                    7.dp
                                            ),

                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Checkbox(
                                            checked =
                                                isSelected,
                                            onCheckedChange =
                                                null
                                        )


                                        Spacer(
                                            modifier =
                                                Modifier.width(
                                                    6.dp
                                                )
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
                },


                confirmButton = {

                    Button(

                        onClick = {

                            groupsViewModel
                                .addMembersToGroup(
                                    groupId,
                                    selectedNewMembers
                                        .toList()
                                )


                            showAddMemberDialog =
                                false


                            selectedNewMembers =
                                emptySet()
                        },

                        enabled =
                            selectedNewMembers
                                .isNotEmpty(),

                        shape =
                            RoundedCornerShape(12.dp)

                    ) {

                        Text("Add to Group")
                    }
                },


                dismissButton = {

                    TextButton(

                        onClick = {

                            showAddMemberDialog =
                                false


                            selectedNewMembers =
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
// MONEY FORMAT
// ================================================================

private fun formatGroupDetailMoney(
    amount: Double
): String {

    return if (amount % 1.0 == 0.0) {

        "Rs. ${amount.toLong()}"

    } else {

        "Rs. %.2f".format(amount)
    }
}