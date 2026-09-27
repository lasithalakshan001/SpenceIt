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
import androidx.lifecycle.viewmodel.compose.viewModel // NEEDED FOR VIEWMODEL
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf

import com.example.spenceit.ui.theme.screens.HomeScreen
import com.example.spenceit.ui.theme.screens.LoginScreen
import com.example.spenceit.ui.theme.screens.RegisterScreen
import com.example.spenceit.ui.theme.screens.GroupsScreen
import com.example.spenceit.ui.theme.screens.MoreScreen
import com.example.spenceit.ui.theme.screens.AddScreen
import com.example.spenceit.ui.theme.screens.DebtsScreen
import com.example.spenceit.ui.theme.screens.GroupDetailScreen
import com.example.spenceit.ui.theme.screens.FriendDetailScreen
import com.example.spenceit.viewmodel.GroupsViewModel
import com.example.spenceit.viewmodel.FriendsViewModel
import com.example.spenceit.viewmodel.DebtsViewModel
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()



    var startDestination by remember { mutableStateOf<String?>(null) }

    // 2. Listen to Firebase in the background the moment the app launches
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                startDestination = "home"
            } else {
                startDestination = "login"
            }
        }
    }

    // 3. Show a quick loading spinner for the millisecond it takes Firebase to read the disk
    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return // Stop here until Firebase answers!
    }

    // 1. Create ONE master instance of the GroupsViewModel here
    val sharedGroupsViewModel: GroupsViewModel = viewModel()
    val sharedFriendsViewModel: FriendsViewModel = viewModel() // <-- ADD THIS
    val sharedDebtsViewModel: DebtsViewModel = viewModel()

    val items = listOf(
        BottomNavItem("Home", "home", Icons.Default.Home),
        BottomNavItem("Groups", "groups", Icons.Default.Person),
        BottomNavItem("Add", "add", Icons.Default.AddCircle),
        BottomNavItem("Debts", "debts", Icons.Default.List),
        BottomNavItem("More", "more", Icons.Default.Menu)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
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
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
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
        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = { navController.navigate("home") }
                )
            }
            composable("register") { RegisterScreen(onRegisterSuccess = { navController.navigate("home") }) }
            composable("home") {
                HomeScreen(
                    onNavigateToGroups = {
                        // Simulate a click on the bottom navigation bar to keep the state in sync
                        navController.navigate("groups") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAdd = {
                        navController.navigate("add") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToDebts = {
                        navController.navigate("debts") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onGroupClick = { groupId -> navController.navigate("group_detail/$groupId") },
                    debtsViewModel = sharedDebtsViewModel,
                    groupsViewModel = sharedGroupsViewModel
                )
            }

            composable("groups") {
                GroupsScreen(
                    onGroupClick = { groupId -> navController.navigate("group_detail/$groupId") },
                    onFriendClick = { friendUid -> navController.navigate("friend_detail/$friendUid") },
                    friendsViewModel = sharedFriendsViewModel,
                    groupsViewModel = sharedGroupsViewModel, // 2. Pass the shared instance
                    debtsViewModel = sharedDebtsViewModel
                )
            }

            composable("group_detail/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                GroupDetailScreen(
                    groupId = groupId,
                    onNavigateBack = { navController.popBackStack() },
                    groupsViewModel = sharedGroupsViewModel,
                    friendsViewModel = sharedFriendsViewModel // <-- ADD THIS LINE
                )
            }

            composable("add") { AddScreen() }
            composable("debts") { DebtsScreen(
                debtsViewModel = sharedDebtsViewModel
            ) }

            composable("friend_detail/{friendUid}") { backStackEntry ->
                val friendUid = backStackEntry.arguments?.getString("friendUid") ?: return@composable
                FriendDetailScreen(
                    friendUid = friendUid,
                    onNavigateBack = { navController.popBackStack() },
                    friendsViewModel = sharedFriendsViewModel,
                    debtsViewModel = sharedDebtsViewModel
                )
            }
            composable("more") {
                MoreScreen(
                    onLogoutSuccess = {
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}