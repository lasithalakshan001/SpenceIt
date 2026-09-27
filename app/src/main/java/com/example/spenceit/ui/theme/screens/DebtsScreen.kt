package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
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

    // ----------------------------------------------------
    // TAB STATE
    // ----------------------------------------------------

    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }

    val tabs = listOf(
        "Active",
        "Pending",
        "History"
    )


    // ----------------------------------------------------
    // LIVE DEBT DATA
    // ----------------------------------------------------

    val liveDebts by
    debtsViewModel.activeDebts.collectAsState()


    // ----------------------------------------------------
    // PAYMENT DIALOG STATE
    // ----------------------------------------------------

    var debtToPay by remember {
        mutableStateOf<ActiveDebt?>(null)
    }

    var paymentNote by remember {
        mutableStateOf("")
    }


    // ----------------------------------------------------
    // SCREEN
    // ----------------------------------------------------

    Column(
        modifier = modifier.fillMaxSize()
    ) {


        // ------------------------------------------------
        // HEADER
        // ------------------------------------------------

        Column(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 18.dp
            )
        ) {

            Text(
                text = "Debts",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "Track active balances, approvals and history.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }


        // ------------------------------------------------
        // SEGMENTED TAB SELECTOR
        // ------------------------------------------------

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {

            Row(
                modifier = Modifier.padding(4.dp)
            ) {

                tabs.forEachIndexed { index, title ->

                    val selected =
                        selectedTabIndex == index

                    Surface(
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color =
                            if (selected) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        tonalElevation =
                            if (selected) 1.dp else 0.dp,
                        onClick = {
                            selectedTabIndex = index
                        }
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 11.dp,
                                    horizontal = 4.dp
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
                                    }
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
            // ACTIVE
            // ====================================================

            0 -> {

                val activeOnly =
                    liveDebts.filter {
                        it.status == "active"
                    }


                if (activeOnly.isEmpty()) {

                    EmptyStateMessage(
                        title = "All caught up!",
                        subtitle = "You have no active debts.",
                        icon = Icons.Default.ThumbUp
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
                            items = activeOnly,
                            key = { it.debtId }
                        ) { debt ->

                            ModernDebtCard(
                                debt = debt,
                                onMarkPaidClick = {
                                    debtToPay = debt
                                },
                                onAccept = {},
                                onReject = {}
                            )
                        }
                    }
                }
            }


            // ====================================================
            // PENDING
            // ====================================================

            1 -> {

                val pendingOnly =
                    liveDebts.filter {
                        it.status == "pending_approval"
                    }


                if (pendingOnly.isEmpty()) {

                    EmptyStateMessage(
                        title = "No pending requests",
                        subtitle = "You're all clear here.",
                        icon = Icons.Default.CheckCircle
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
                            items = pendingOnly,
                            key = { it.debtId }
                        ) { debt ->

                            ModernDebtCard(
                                debt = debt,
                                onMarkPaidClick = {},
                                onAccept = {

                                    debtsViewModel.acceptDebt(
                                        debt
                                    )
                                },
                                onReject = {

                                    debtsViewModel.rejectDebt(
                                        debt
                                    )
                                }
                            )
                        }
                    }
                }
            }


            // ====================================================
            // HISTORY
            // ====================================================

            2 -> {

                val historyOnly =
                    liveDebts
                        .filter {
                            it.status == "settled"
                        }
                        .sortedByDescending {
                            it.timestamp
                        }


                if (historyOnly.isEmpty()) {

                    EmptyStateMessage(
                        title = "No recent history",
                        subtitle = "Settled debts will appear here.",
                        icon = Icons.Default.Check
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
                            Arrangement.spacedBy(10.dp)
                    ) {

                        items(
                            items = historyOnly,
                            key = { it.debtId }
                        ) { debt ->

                            HistoryDebtCard(
                                debt = debt
                            )
                        }
                    }
                }
            }
        }
    }


    // ========================================================
    // PAYMENT DIALOG
    // ========================================================

    if (debtToPay != null) {

        AlertDialog(

            onDismissRequest = {

                debtToPay = null

                paymentNote = ""
            },

            shape = RoundedCornerShape(24.dp),

            containerColor =
                MaterialTheme.colorScheme.surface,


            title = {

                Text(
                    text = "Mark as Paid",
                    style = MaterialTheme.typography.headlineSmall
                )
            },


            text = {

                Column {

                    Text(
                        text =
                            "Send a payment confirmation to " +
                                    "${debtToPay?.otherPersonName} " +
                                    "for Rs. ${debtToPay?.amount}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )


                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )


                    OutlinedTextField(
                        value = paymentNote,

                        onValueChange = {
                            paymentNote = it
                        },

                        label = {
                            Text("Note (Optional)")
                        },

                        placeholder = {
                            Text("e.g. Paid in cash")
                        },

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(14.dp)
                    )
                }
            },


            confirmButton = {

                Button(

                    onClick = {

                        debtsViewModel.updateDebtStatus(
                            debtToPay!!.debtId,
                            "pending_approval",
                            paymentNote
                        )

                        debtToPay = null

                        paymentNote = ""
                    },

                    shape =
                        RoundedCornerShape(12.dp)

                ) {

                    Text("Submit")
                }
            },


            dismissButton = {

                TextButton(

                    onClick = {

                        debtToPay = null

                        paymentNote = ""
                    }

                ) {

                    Text("Cancel")
                }
            }
        )
    }
}


// ================================================================
// EMPTY STATE
// ================================================================

@Composable
fun EmptyStateMessage(
    title: String,
    subtitle: String,
    icon: ImageVector
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
                modifier = Modifier.size(78.dp),
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
                        imageVector = icon,
                        contentDescription = null,
                        modifier =
                            Modifier.size(36.dp),
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
        }
    }
}


// ================================================================
// ACTIVE / PENDING DEBT CARD
// ================================================================

@Composable
fun ModernDebtCard(
    debt: ActiveDebt,
    onMarkPaidClick: () -> Unit,
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

        Column {


            // ------------------------------------------------
            // CARD MAIN CONTENT
            // ------------------------------------------------

            Column(
                modifier =
                    Modifier.padding(16.dp)
            ) {


                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {


                    Surface(
                        modifier =
                            Modifier.size(46.dp),
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
                                modifier =
                                    Modifier.size(23.dp),
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
                            text =
                                debt.otherPersonName,
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
                            text =
                                debt.description,
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


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    Column(
                        horizontalAlignment =
                            Alignment.End
                    ) {


                        val amountColor =
                            if (debt.iOweThem) {

                                MaterialTheme
                                    .colorScheme
                                    .error

                            } else {

                                MaterialTheme
                                    .colorScheme
                                    .primary
                            }


                        Text(
                            text =
                                formatDebtMoney(
                                    debt.amount
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                amountColor
                        )


                        Spacer(
                            modifier =
                                Modifier.height(3.dp)
                        )


                        Text(
                            text =
                                if (debt.iOweThem) {
                                    "You owe"
                                } else {
                                    "Owes you"
                                },
                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,
                            color =
                                amountColor
                        )
                    }
                }


                // --------------------------------------------
                // PENDING STATUS CHIP
                // --------------------------------------------

                if (
                    debt.status ==
                    "pending_approval"
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    Surface(
                        shape =
                            RoundedCornerShape(50),

                        color =
                            MaterialTheme
                                .colorScheme
                                .primaryContainer
                    ) {

                        Text(
                            text =
                                "Awaiting approval",
                            modifier =
                                Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,
                            fontWeight =
                                FontWeight.SemiBold,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )
                    }
                }


                // --------------------------------------------
                // SETTLEMENT NOTE
                // --------------------------------------------

                if (
                    debt.status ==
                    "pending_approval" &&
                    !debt
                        .settlementNote
                        .isNullOrEmpty()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(12.dp),

                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                    ) {

                        Text(
                            text =
                                "Note: " +
                                        debt
                                            .settlementNote,
                            modifier =
                                Modifier.padding(
                                    12.dp
                                ),
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


            // ------------------------------------------------
            // ACTIVE DEBT: MARK AS PAID
            // ------------------------------------------------

            if (
                debt.iOweThem &&
                debt.status == "active"
            ) {

                HorizontalDivider(
                    color =
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                )


                Button(
                    onClick =
                        onMarkPaidClick,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(46.dp),

                    shape =
                        RoundedCornerShape(13.dp)

                ) {

                    Text(
                        text = "Mark as Paid",
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge
                    )
                }
            }


            // ------------------------------------------------
            // PENDING: ACCEPT / REJECT
            // ------------------------------------------------

            else if (
                !debt.iOweThem &&
                debt.status ==
                "pending_approval"
            ) {

                HorizontalDivider(
                    color =
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),

                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {


                    OutlinedButton(
                        onClick = onReject,

                        modifier =
                            Modifier.weight(1f),

                        shape =
                            RoundedCornerShape(13.dp)
                    ) {

                        Text("Reject")
                    }


                    Button(
                        onClick = onAccept,

                        modifier =
                            Modifier.weight(1f),

                        shape =
                            RoundedCornerShape(13.dp)
                    ) {

                        Text("Accept")
                    }
                }
            }
        }
    }
}


// ================================================================
// HISTORY CARD
// ================================================================

@Composable
fun HistoryDebtCard(
    debt: ActiveDebt
) {

    val dateFormat =
        SimpleDateFormat(
            "MMM dd, yyyy - hh:mm a",
            Locale.getDefault()
        )


    val dateString =
        dateFormat.format(
            Date(debt.timestamp)
        )


    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {


            Surface(
                modifier =
                    Modifier.size(40.dp),

                shape = CircleShape,

                color =
                    MaterialTheme
                        .colorScheme
                        .secondaryContainer
            ) {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Check,
                        contentDescription =
                            "Settled",
                        modifier =
                            Modifier.size(20.dp),
                        tint =
                            MaterialTheme
                                .colorScheme
                                .secondary
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
                    text =
                        "Settled with " +
                                debt.otherPersonName,
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,
                    fontWeight =
                        FontWeight.SemiBold
                )


                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )


                Text(
                    text =
                        debt.description,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )


                Text(
                    text = dateString,
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }


            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )


            Text(
                text =
                    formatDebtMoney(
                        debt.amount
                    ),
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


// ================================================================
// MONEY FORMAT
// ================================================================

private fun formatDebtMoney(
    amount: Double
): String {

    return if (
        amount % 1.0 == 0.0
    ) {

        "Rs. ${amount.toLong()}"

    } else {

        "Rs. %.2f".format(
            amount
        )
    }
}