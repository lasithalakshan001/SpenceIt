package com.example.spenceit.ui.theme.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // We use verticalScroll so the user can scroll if the screen gets too full
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Automatically puts a 16dp gap between every item!
    ) {

        // 1. WELCOME HEADER
        Text(
            text = "Hello, Lasitha!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 2. MAIN BALANCE CARD (Net Position)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Net Position", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "Rs. 2,500",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. ROW FOR SPLIT DEBT SUMMARIES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Card: Money to Receive (Takes up exactly half the space)
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "To Receive", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "Rs. 4,500",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Right Card: Money You Owe (Takes up the other half)
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "You Owe", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "Rs. 2,000",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error // Makes it red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. PERSONAL FUNDS SECTION
        Text(text = "Your Funds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // Pushes text to opposite ends
                ) {
                    Text("Cash")
                    Text("Rs. 5,000")
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp)) // A thin horizontal line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bank")
                    Text("Rs. 25,000")
                }
            }
        }
    }
}