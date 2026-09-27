package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spenceit.ui.theme.OweCardAccent
import com.example.spenceit.ui.theme.OweCardBackground
import com.example.spenceit.ui.theme.OwedCardAccent
import com.example.spenceit.ui.theme.OwedCardBackground
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


    // ----------------------------------------------------
    // EXISTING BALANCE LOGIC
    // ----------------------------------------------------

    val activeOnly = liveDebts.filter {
        it.status == "active"
    }

    val totalOwedToMe = activeOnly
        .filter { !it.iOweThem }
        .sumOf { it.amount }

    val totalIOwe = activeOnly
        .filter { it.iOweThem }
        .sumOf { it.amount }


    // ----------------------------------------------------
    // EXISTING PENDING APPROVAL LOGIC
    // ----------------------------------------------------

    val pendingCount = liveDebts.count {
        it.status == "pending_approval" && !it.iOweThem
    }


    // ----------------------------------------------------
    // MAIN SCREEN
    // ----------------------------------------------------

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 24.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {


        // ------------------------------------------------
        // HEADER
        // ------------------------------------------------

        item {

            Column {

                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Keep track of your shared expenses.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


        // ------------------------------------------------
        // BALANCE CARDS
        // ------------------------------------------------

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                BalanceCard(
                    modifier = Modifier.weight(1f),
                    title = "You are owed",
                    amount = totalOwedToMe,
                    backgroundColor = OwedCardBackground,
                    accentColor = OwedCardAccent
                )


                BalanceCard(
                    modifier = Modifier.weight(1f),
                    title = "You owe",
                    amount = totalIOwe,
                    backgroundColor = OweCardBackground,
                    accentColor = OweCardAccent
                )
            }
        }


        // ------------------------------------------------
        // PENDING APPROVAL ALERT
        // ------------------------------------------------

        if (pendingCount > 0) {

            item {

                PendingApprovalCard(
                    pendingCount = pendingCount,
                    onClick = onNavigateToDebts
                )
            }
        }


        // ------------------------------------------------
        // QUICK ACTIONS
        // ------------------------------------------------

        item {

            Column {

                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {


                    Button(
                        onClick = onNavigateToAdd,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = "Add Debt",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }


                    OutlinedButton(
                        onClick = onNavigateToGroups,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder,
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = "Groups",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }


        // ------------------------------------------------
        // RECENT GROUPS HEADER
        // ------------------------------------------------

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Recent Groups",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )


                if (liveGroups.isNotEmpty()) {

                    TextButton(
                        onClick = onNavigateToGroups,
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 0.dp
                        )
                    ) {

                        Text(
                            text = "View all",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }


        // ------------------------------------------------
        // EMPTY GROUP STATE
        // ------------------------------------------------

        if (liveGroups.isEmpty()) {

            item {

                EmptyGroupsCard(
                    onClick = onNavigateToGroups
                )
            }

        } else {


            // ------------------------------------------------
            // ONLY SHOW FIRST THREE GROUPS
            // ------------------------------------------------

            items(
                items = liveGroups.take(3),
                key = { it.groupId }
            ) { group ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onGroupClick(group.groupId)
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
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
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                text = "${group.memberUids.size} members",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }


                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {

                            Text(
                                text = "›",
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}


// ====================================================
// BALANCE CARD
// ====================================================

@Composable
private fun BalanceCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    backgroundColor: Color,
    accentColor: Color
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {


            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.12f)
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "Rs",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            Spacer(
                modifier = Modifier.height(5.dp)
            )


            Text(
                text = formatMoney(amount),
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}


// ====================================================
// PENDING APPROVAL CARD
// ====================================================

@Composable
private fun PendingApprovalCard(
    pendingCount: Int,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiary.copy(
                    alpha = 0.13f
                )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.tertiary
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
                    text = "Action Required",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        if (pendingCount == 1) {
                            "1 payment is waiting for your approval."
                        } else {
                            "$pendingCount payments are waiting for your approval."
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ====================================================
// EMPTY GROUP CARD
// ====================================================

@Composable
private fun EmptyGroupsCard(
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 28.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            Text(
                text = "No groups yet",
                style = MaterialTheme.typography.titleMedium
            )


            Spacer(
                modifier = Modifier.height(4.dp)
            )


            Text(
                text = "Create a group to start sharing expenses.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            Text(
                text = "Create Group",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// ====================================================
// MONEY FORMATTER
// ====================================================

private fun formatMoney(
    amount: Double
): String {

    return if (amount % 1.0 == 0.0) {

        "Rs. ${amount.toLong()}"

    } else {

        "Rs. %.2f".format(amount)
    }
}