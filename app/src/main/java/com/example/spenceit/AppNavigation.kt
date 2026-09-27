package com.example.spenceit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.spenceit.ui.theme.screens.AddScreen
import com.example.spenceit.ui.theme.screens.DebtsScreen
import com.example.spenceit.ui.theme.screens.FriendDetailScreen
import com.example.spenceit.ui.theme.screens.GroupDetailScreen
import com.example.spenceit.ui.theme.screens.GroupsScreen
import com.example.spenceit.ui.theme.screens.HomeScreen
import com.example.spenceit.ui.theme.screens.LoginScreen
import com.example.spenceit.ui.theme.screens.MoreScreen
import com.example.spenceit.ui.theme.screens.RegisterScreen

import com.example.spenceit.viewmodel.DebtsViewModel
import com.example.spenceit.viewmodel.FriendsViewModel
import com.example.spenceit.viewmodel.GroupsViewModel

import com.google.firebase.auth.FirebaseAuth


// ----------------------------------------------------
// BOTTOM NAVIGATION ITEM
// ----------------------------------------------------

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)


// ----------------------------------------------------
// MAIN APP NAVIGATION
// ----------------------------------------------------

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {

    val navController = rememberNavController()

    /*
     * We keep your existing Firebase login detection.
     *
     * null  -> Firebase is still checking
     * home  -> user already logged in
     * login -> user not logged in
     */
    var startDestination by remember {
        mutableStateOf<String?>(null)
    }


    // ----------------------------------------------------
    // FIREBASE AUTH CHECK
    // ----------------------------------------------------

    LaunchedEffect(Unit) {

        val auth = FirebaseAuth.getInstance()

        auth.addAuthStateListener { firebaseAuth ->

            startDestination =
                if (firebaseAuth.currentUser != null) {
                    "home"
                } else {
                    "login"
                }
        }
    }


    // ----------------------------------------------------
    // INITIAL LOADING SCREEN
    // ----------------------------------------------------

    if (startDestination == null) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }

        return
    }


    // ----------------------------------------------------
    // SHARED VIEWMODELS
    // ----------------------------------------------------

    /*
     * Keep one master instance of these ViewModels.
     *
     * This preserves the existing shared state between
     * Home, Groups, Debts and detail screens.
     */

    val sharedGroupsViewModel: GroupsViewModel = viewModel()

    val sharedFriendsViewModel: FriendsViewModel = viewModel()

    val sharedDebtsViewModel: DebtsViewModel = viewModel()


    // ----------------------------------------------------
    // BOTTOM NAVIGATION DESTINATIONS
    // ----------------------------------------------------

    val items = listOf(

        BottomNavItem(
            title = "Home",
            route = "home",
            icon = Icons.Default.Home
        ),

        BottomNavItem(
            title = "Groups",
            route = "groups",
            icon = Icons.Default.Person
        ),

        BottomNavItem(
            title = "Add",
            route = "add",
            icon = Icons.Default.AddCircle
        ),

        BottomNavItem(
            title = "Debts",
            route = "debts",
            icon = Icons.Default.List
        ),

        BottomNavItem(
            title = "More",
            route = "more",
            icon = Icons.Default.Menu
        )
    )


    val navBackStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        navBackStackEntry?.destination?.route


    /*
     * Bottom navigation only appears on the five
     * main application screens.
     */
    val showBottomBar =
        currentRoute in listOf(
            "home",
            "groups",
            "add",
            "debts",
            "more"
        )


    // ----------------------------------------------------
    // MAIN SCAFFOLD
    // ----------------------------------------------------

    Scaffold(

        modifier = modifier,

        containerColor = MaterialTheme.colorScheme.background,

        bottomBar = {

            if (showBottomBar) {

                NavigationBar(

                    modifier = Modifier.height(76.dp),

                    containerColor = MaterialTheme.colorScheme.surface,

                    tonalElevation = 0.dp
                ) {

                    items.forEach { item ->

                        val selected =
                            currentRoute == item.route


                        NavigationBarItem(

                            selected = selected,

                            onClick = {

                                navController.navigate(item.route) {

                                    popUpTo(
                                        navController.graph
                                            .findStartDestination()
                                            .id
                                    ) {
                                        saveState = true
                                    }

                                    launchSingleTop = true

                                    restoreState = true
                                }
                            },

                            icon = {

                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },

                            label = {

                                Text(
                                    text = item.title,
                                    style = MaterialTheme
                                        .typography
                                        .labelSmall
                                )
                            },

                            alwaysShowLabel = true,

                            colors =
                                NavigationBarItemDefaults.colors(

                                    selectedIconColor =
                                        MaterialTheme
                                            .colorScheme
                                            .primary,

                                    selectedTextColor =
                                        MaterialTheme
                                            .colorScheme
                                            .primary,

                                    indicatorColor =
                                        MaterialTheme
                                            .colorScheme
                                            .primaryContainer,

                                    unselectedIconColor =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant,

                                    unselectedTextColor =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                        )
                    }
                }
            }
        }

    ) { innerPadding ->


        // ----------------------------------------------------
        // NAVIGATION GRAPH
        // ----------------------------------------------------

        NavHost(

            navController = navController,

            startDestination = startDestination!!,

            modifier = Modifier.padding(innerPadding)

        ) {


            // ------------------------------------------------
            // LOGIN
            // ------------------------------------------------

            composable("login") {

                LoginScreen(

                    onNavigateToRegister = {

                        navController.navigate("register")
                    },

                    onLoginSuccess = {

                        navController.navigate("home")
                    }
                )
            }


            // ------------------------------------------------
            // REGISTER
            // ------------------------------------------------

            composable("register") {

                RegisterScreen(

                    onRegisterSuccess = {

                        navController.navigate("home")
                    }
                )
            }


            // ------------------------------------------------
            // HOME
            // ------------------------------------------------

            composable("home") {

                HomeScreen(

                    onNavigateToGroups = {

                        navController.navigate("groups") {

                            popUpTo(
                                navController.graph
                                    .findStartDestination()
                                    .id
                            ) {
                                saveState = true
                            }

                            launchSingleTop = true

                            restoreState = true
                        }
                    },

                    onNavigateToAdd = {

                        navController.navigate("add") {

                            popUpTo(
                                navController.graph
                                    .findStartDestination()
                                    .id
                            ) {
                                saveState = true
                            }

                            launchSingleTop = true

                            restoreState = true
                        }
                    },

                    onNavigateToDebts = {

                        navController.navigate("debts") {

                            popUpTo(
                                navController.graph
                                    .findStartDestination()
                                    .id
                            ) {
                                saveState = true
                            }

                            launchSingleTop = true

                            restoreState = true
                        }
                    },

                    onGroupClick = { groupId ->

                        navController.navigate(
                            "group_detail/$groupId"
                        )
                    },

                    debtsViewModel =
                        sharedDebtsViewModel,

                    groupsViewModel =
                        sharedGroupsViewModel
                )
            }


            // ------------------------------------------------
            // GROUPS
            // ------------------------------------------------

            composable("groups") {

                GroupsScreen(

                    onGroupClick = { groupId ->

                        navController.navigate(
                            "group_detail/$groupId"
                        )
                    },

                    onFriendClick = { friendUid ->

                        navController.navigate(
                            "friend_detail/$friendUid"
                        )
                    },

                    friendsViewModel =
                        sharedFriendsViewModel,

                    groupsViewModel =
                        sharedGroupsViewModel,

                    debtsViewModel =
                        sharedDebtsViewModel
                )
            }


            // ------------------------------------------------
            // GROUP DETAIL
            // ------------------------------------------------

            composable(
                "group_detail/{groupId}"
            ) { backStackEntry ->


                val groupId =
                    backStackEntry.arguments
                        ?.getString("groupId")
                        ?: return@composable


                GroupDetailScreen(

                    groupId = groupId,

                    onNavigateBack = {

                        navController.popBackStack()
                    },

                    groupsViewModel =
                        sharedGroupsViewModel,

                    friendsViewModel =
                        sharedFriendsViewModel
                )
            }


            // ------------------------------------------------
            // ADD DEBT
            // ------------------------------------------------

            composable("add") {

                /*
                 * Keeping your existing AddScreen call
                 * exactly as it was.
                 */
                AddScreen()
            }


            // ------------------------------------------------
            // DEBTS
            // ------------------------------------------------

            composable("debts") {

                DebtsScreen(

                    debtsViewModel =
                        sharedDebtsViewModel
                )
            }


            // ------------------------------------------------
            // FRIEND DETAIL
            // ------------------------------------------------

            composable(
                "friend_detail/{friendUid}"
            ) { backStackEntry ->


                val friendUid =
                    backStackEntry.arguments
                        ?.getString("friendUid")
                        ?: return@composable


                FriendDetailScreen(

                    friendUid = friendUid,

                    onNavigateBack = {

                        navController.popBackStack()
                    },

                    friendsViewModel =
                        sharedFriendsViewModel,

                    debtsViewModel =
                        sharedDebtsViewModel
                )
            }


            // ------------------------------------------------
            // MORE
            // ------------------------------------------------

            composable("more") {

                MoreScreen(

                    onLogoutSuccess = {

                        navController.navigate("login") {

                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}


// ----------------------------------------------------
// PLACEHOLDER
// ----------------------------------------------------

@Composable
fun PlaceholderScreen(
    title: String
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}