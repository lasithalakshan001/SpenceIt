package com.example.spenceit.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spenceit.viewmodel.DebtsViewModel
import com.example.spenceit.viewmodel.FriendsViewModel
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendUid: String,
    onNavigateBack: () -> Unit,
    friendsViewModel: FriendsViewModel,
    debtsViewModel: DebtsViewModel
) {

    // ----------------------------------------------------
    // FRIEND DATA
    // ----------------------------------------------------

    val myFriends by
    friendsViewModel.friendsList.collectAsState()

    val friend =
        myFriends.find {
            it.uid == friendUid
        }


    // ----------------------------------------------------
    // DEBT DATA
    // ----------------------------------------------------

    val allDebts by
    debtsViewModel.activeDebts.collectAsState()


    val sharedDebts =
        allDebts.filter {

            it.otherPersonUid == friendUid &&
                    (
                            it.status == "active" ||
                                    it.status == "pending_approval"
                            )
        }


    // ----------------------------------------------------
    // EXISTING BALANCE CALCULATION
    // ----------------------------------------------------

    var totalIOweThem = 0.0

    var totalTheyOweMe = 0.0


    sharedDebts.forEach { debt ->

        /*
         * Keep your payment-credit logic exactly
         * the same.
         */
        if (
            debt.description.startsWith(
                "[PAYMENT]"
            )
        ) {

            if (debt.iOweThem) {

                totalTheyOweMe += debt.amount

            } else {

                totalIOweThem += debt.amount
            }

        } else {

            if (debt.iOweThem) {

                totalIOweThem += debt.amount

            } else {

                totalTheyOweMe += debt.amount
            }
        }
    }


    val netBalance =
        totalTheyOweMe - totalIOweThem


    // ----------------------------------------------------
    // PAYMENT DIALOG STATES
    // ----------------------------------------------------

    var showSettleDialog by remember {
        mutableStateOf(false)
    }

    var customPaymentAmount by remember {
        mutableStateOf("")
    }

    var paymentNote by remember {
        mutableStateOf("")
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
                        text =
                            friend?.name
                                ?: "Loading...",
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onNavigateBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription =
                                "Back"
                        )
                    }
                },

                colors =
                    TopAppBarDefaults
                        .topAppBarColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .background
                        )
            )
        }

    ) { innerPadding ->


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),

            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 28.dp
            ),

            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {


            // ====================================================
            // FRIEND HEADER
            // ====================================================

            item {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {


                    Surface(
                        modifier =
                            Modifier.size(58.dp),

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
                                    Modifier.size(30.dp),

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.width(14.dp)
                    )


                    Column {

                        Text(
                            text =
                                friend?.name
                                    ?: "Loading...",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge
                        )


                        if (friend != null) {

                            Spacer(
                                modifier =
                                    Modifier.height(2.dp)
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


            // ====================================================
            // BALANCE CARD
            // ====================================================

            item {

                BalanceSummaryCard(
                    friendName =
                        friend?.name
                            ?: "Friend",

                    netBalance =
                        netBalance,

                    onSettleClick = {

                        customPaymentAmount =
                            abs(netBalance)
                                .toString()

                        showSettleDialog = true
                    }
                )
            }


            // ====================================================
            // TRANSACTIONS TITLE
            // ====================================================

            item {

                Column {

                    Text(
                        text =
                            "Active Transactions",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )


                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )


                    Text(
                        text =
                            "Current expenses and pending payments with this friend.",

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


            // ====================================================
            // EMPTY TRANSACTIONS
            // ====================================================

            if (sharedDebts.isEmpty()) {

                item {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(18.dp),

                        colors =
                            CardDefaults
                                .cardColors(
                                    containerColor =
                                        MaterialTheme
                                            .colorScheme
                                            .surfaceVariant
                                ),

                        elevation =
                            CardDefaults
                                .cardElevation(
                                    defaultElevation =
                                        0.dp
                                )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 28.dp,
                                    horizontal = 20.dp
                                ),

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Surface(
                                modifier =
                                    Modifier.size(52.dp),

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
                                    Modifier.height(14.dp)
                            )


                            Text(
                                text =
                                    "No active transactions",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )


                            Text(
                                text =
                                    "You're all clear with this friend.",

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

            } else {


                // ====================================================
                // TRANSACTION CARDS
                // ====================================================

                items(
                    items = sharedDebts,
                    key = { it.debtId }
                ) { debt ->


                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(18.dp),

                        colors =
                            CardDefaults
                                .cardColors(
                                    containerColor =
                                        MaterialTheme
                                            .colorScheme
                                            .surface
                                ),

                        elevation =
                            CardDefaults
                                .cardElevation(
                                    defaultElevation =
                                        1.dp
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
                                modifier =
                                    Modifier.size(42.dp),

                                shape = CircleShape,

                                color =
                                    if (debt.iOweThem) {

                                        MaterialTheme
                                            .colorScheme
                                            .errorContainer

                                    } else {

                                        MaterialTheme
                                            .colorScheme
                                            .primaryContainer
                                    }
                            ) {

                                Box(
                                    modifier =
                                        Modifier.fillMaxSize(),

                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Text(
                                        text = "Rs",

                                        style =
                                            MaterialTheme
                                                .typography
                                                .labelSmall,

                                        fontWeight =
                                            FontWeight.Bold,

                                        color =
                                            if (debt.iOweThem) {

                                                MaterialTheme
                                                    .colorScheme
                                                    .error

                                            } else {

                                                MaterialTheme
                                                    .colorScheme
                                                    .primary
                                            }
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
                                        debt.description,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleSmall
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(3.dp)
                                )


                                if (
                                    debt.status ==
                                    "pending_approval"
                                ) {

                                    Surface(
                                        shape =
                                            RoundedCornerShape(
                                                50
                                            ),

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .primaryContainer
                                    ) {

                                        Text(
                                            text =
                                                "Pending approval",

                                            modifier =
                                                Modifier.padding(
                                                    horizontal =
                                                        8.dp,

                                                    vertical =
                                                        4.dp
                                                ),

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

                                } else {

                                    Text(
                                        text = "Active",

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
                                        formatFriendMoney(
                                            debt.amount
                                        ),

                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleSmall,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        amountColor
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(2.dp)
                                )


                                Text(
                                    text =
                                        if (debt.iOweThem) {
                                            "You owe"
                                        } else {
                                            "They owe"
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
                    }
                }
            }
        }
    }


    // ============================================================
    // SETTLE UP DIALOG
    // ============================================================

    if (showSettleDialog) {

        AlertDialog(

            onDismissRequest = {

                showSettleDialog = false

                paymentNote = ""
            },

            shape =
                RoundedCornerShape(24.dp),

            containerColor =
                MaterialTheme
                    .colorScheme
                    .surface,


            title = {

                Text(
                    text = "Settle Up",

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall
                )
            },


            text = {

                Column {

                    Text(
                        text =
                            "How much are you paying right now?",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )


                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )


                    OutlinedTextField(

                        value =
                            customPaymentAmount,

                        onValueChange = {
                            customPaymentAmount = it
                        },

                        label = {
                            Text("Amount (Rs.)")
                        },

                        placeholder = {
                            Text("0.00")
                        },

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            ),

                        modifier =
                            Modifier.fillMaxWidth(),

                        singleLine = true,

                        shape =
                            RoundedCornerShape(14.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    OutlinedTextField(

                        value =
                            paymentNote,

                        onValueChange = {
                            paymentNote = it
                        },

                        label = {
                            Text("Note (Optional)")
                        },

                        placeholder = {
                            Text(
                                "e.g. Paid cash at class"
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(14.dp),

                        maxLines = 3
                    )
                }
            },


            confirmButton = {

                Button(

                    onClick = {

                        if (friend != null) {

                            debtsViewModel
                                .sendPaymentRequest(
                                    friend.uid,
                                    customPaymentAmount,
                                    paymentNote
                                )
                        }

                        showSettleDialog =
                            false

                        paymentNote = ""
                    },

                    enabled =
                        customPaymentAmount
                            .toDoubleOrNull()
                            ?.let {
                                it > 0
                            } == true,

                    shape =
                        RoundedCornerShape(12.dp)

                ) {

                    Text("Send Payment")
                }
            },


            dismissButton = {

                TextButton(

                    onClick = {

                        showSettleDialog =
                            false

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
// BALANCE SUMMARY CARD
// ================================================================

@Composable
private fun BalanceSummaryCard(
    friendName: String,
    netBalance: Double,
    onSettleClick: () -> Unit
) {

    val isOwedToMe =
        netBalance > 0.01

    val iOwe =
        netBalance < -0.01

    val settled =
        !isOwedToMe && !iOwe


    val cardColor =
        when {

            isOwedToMe ->
                MaterialTheme
                    .colorScheme
                    .primaryContainer

            iOwe ->
                MaterialTheme
                    .colorScheme
                    .errorContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .secondaryContainer
        }


    val accentColor =
        when {

            isOwedToMe ->
                MaterialTheme
                    .colorScheme
                    .primary

            iOwe ->
                MaterialTheme
                    .colorScheme
                    .error

            else ->
                MaterialTheme
                    .colorScheme
                    .secondary
        }


    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    cardColor
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {


            Text(
                text = "Net Balance",

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            when {

                isOwedToMe -> {

                    Text(
                        text =
                            "$friendName owes you",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )


                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )


                    Text(
                        text =
                            formatFriendMoney(
                                abs(netBalance)
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .headlineMedium,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            accentColor
                    )
                }


                iOwe -> {

                    Text(
                        text =
                            "You owe $friendName",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )


                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )


                    Text(
                        text =
                            formatFriendMoney(
                                abs(netBalance)
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .headlineMedium,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            accentColor
                    )


                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )


                    Button(
                        onClick =
                            onSettleClick,

                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(14.dp),

                        colors =
                            ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                    ) {

                        Text("Settle Up")
                    }
                }


                settled -> {

                    Text(
                        text =
                            "You're all settled up!",

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            accentColor
                    )


                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )


                    Text(
                        text =
                            "There is no outstanding balance with $friendName.",

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


// ================================================================
// MONEY FORMATTER
// ================================================================

private fun formatFriendMoney(
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