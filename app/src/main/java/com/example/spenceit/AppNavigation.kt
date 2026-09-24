package com.example.spenceit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spenceit.ui.theme.screens.HomeScreen
import com.example.spenceit.ui.theme.screens.LoginScreen
import com.example.spenceit.ui.theme.screens.RegisterScreen

// 1. A blueprint for what a Bottom Tab needs
data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // 2. The list of tabs in your master specification
    val items = listOf(
        BottomNavItem("Home", "home", Icons.Default.Home),
        BottomNavItem("Groups", "groups", Icons.Default.Person),
        BottomNavItem("Add", "add", Icons.Default.AddCircle),
        BottomNavItem("Debts", "debts", Icons.Default.List),
        BottomNavItem("More", "more", Icons.Default.Menu)
    )

    // 3. The GPS: Tracks exactly which screen the user is currently on
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 4. Logic: Should we show the bottom bar? (False if on login/register)
    val showBottomBar = currentRoute in listOf("home", "groups", "add", "debts", "more")

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route, // Highlights the active tab
                            onClick = {
                                // 5. The Taxi Driver takes us to the clicked tab
                                navController.navigate(item.route) {
                                    // This prevents the app from opening 50 Home screens if the user spams the Home button
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        // 6. The Map (NavHost) is now INSIDE the Scaffold padding
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = { navController.navigate("home") }
                )
            }
            composable("register") { RegisterScreen() }
            composable("home") { HomeScreen() }

            // Placeholder screens so the app doesn't crash when you click a tab
            composable("groups") { PlaceholderScreen("Groups & Friends") }
            composable("add") { PlaceholderScreen("Add Transaction") }
            composable("debts") { PlaceholderScreen("My Debts") }
            composable("more") { PlaceholderScreen("Settings & Reports") }
        }
    }
}

// A temporary UI function just to show text on our unfinished screens
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}