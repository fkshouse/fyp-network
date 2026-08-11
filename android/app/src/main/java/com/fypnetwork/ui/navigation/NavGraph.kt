package com.fypnetwork.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fypnetwork.ui.auth.LoginScreen
import com.fypnetwork.ui.auth.RegisterScreen
import com.fypnetwork.ui.connections.ConnectionsScreen
import com.fypnetwork.ui.feed.CreatePostScreen
import com.fypnetwork.ui.feed.FeedScreen
import com.fypnetwork.ui.feed.PostDetailScreen
import com.fypnetwork.ui.groups.GroupDetailScreen
import com.fypnetwork.ui.groups.GroupsScreen
import com.fypnetwork.ui.notifications.NotificationTarget
import com.fypnetwork.ui.notifications.NotificationsScreen
import com.fypnetwork.ui.profile.ProfileScreen
import com.fypnetwork.ui.profile.UserProfileScreen

private data class BottomNavTab(val route: String, val label: String, val icon: ImageVector)

private val bottomNavTabs = listOf(
    BottomNavTab(Destinations.FEED, "Feed", Icons.Filled.Home),
    BottomNavTab(Destinations.GROUPS, "Groups", Icons.Filled.Groups),
    BottomNavTab(Destinations.NOTIFICATIONS, "Alerts", Icons.Filled.Notifications),
    BottomNavTab(Destinations.PROFILE, "Profile", Icons.Filled.Person),
)

@Composable
fun FypNavGraph(startDestination: String) {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in Destinations.bottomNavTabs

    // Scoped to this composable (i.e. to the whole authenticated app session,
    // not to any single screen) so the unread count survives switching tabs
    // rather than resetting every time you leave the Notifications screen.
    val badgeViewModel: NotificationBadgeViewModel = hiltViewModel()
    val unreadCount by badgeViewModel.unreadCount.collectAsState()

    // Not real-time, but correct at the moment that actually matters: every
    // tab switch re-checks the count, so opening Alerts and coming back
    // clears the badge without needing a full cross-ViewModel event bus.
    LaunchedEffect(currentRoute) { badgeViewModel.refresh() }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (tab.route == Destinations.NOTIFICATIONS && unreadCount > 0) {
                                    BadgedBox(badge = { Badge { Text(if (unreadCount > 99) "99+" else "$unreadCount") } }) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(scaffoldPadding),
        ) {
            composable(Destinations.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Destinations.FEED) {
                            popUpTo(Destinations.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Destinations.REGISTER) },
                )
            }

            composable(Destinations.REGISTER) {
                // Registering no longer signs the user in - RegisterScreen
                // shows its own confirmation and this button just goes back
                // to the login screen (popping REGISTER off the stack).
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                )
            }

            composable(Destinations.FEED) {
                FeedScreen(
                    onCreatePost = { navController.navigate(Destinations.CREATE_POST) },
                    onOpenPost = { postId -> navController.navigate(Destinations.postDetail(postId)) },
                    onOpenProfile = { userId -> navController.navigate(Destinations.userProfile(userId)) },
                )
            }

            composable(Destinations.CREATE_POST) {
                CreatePostScreen(
                    onPostCreated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Destinations.POST_DETAIL,
                arguments = listOf(navArgument("postId") { type = NavType.StringType }),
            ) {
                PostDetailScreen(onBack = { navController.popBackStack() })
            }

            composable(Destinations.GROUPS) {
                GroupsScreen(
                    onOpenGroup = { groupId -> navController.navigate(Destinations.groupDetail(groupId)) },
                )
            }

            composable(
                route = Destinations.GROUP_DETAIL,
                arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
            ) {
                GroupDetailScreen(onBack = { navController.popBackStack() })
            }

            composable(Destinations.NOTIFICATIONS) {
                NotificationsScreen(
                    onNavigate = { target ->
                        when (target) {
                            is NotificationTarget.Connections -> navController.navigate(Destinations.CONNECTIONS)
                            is NotificationTarget.Post -> navController.navigate(Destinations.postDetail(target.postId))
                            is NotificationTarget.Group -> navController.navigate(Destinations.groupDetail(target.groupId))
                        }
                    },
                )
            }

            composable(Destinations.PROFILE) {
                ProfileScreen(
                    onViewConnections = { navController.navigate(Destinations.CONNECTIONS) },
                    onLoggedOut = {
                        navController.navigate(Destinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable(Destinations.CONNECTIONS) {
                ConnectionsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenProfile = { userId -> navController.navigate(Destinations.userProfile(userId)) },
                )
            }

            composable(
                route = Destinations.USER_PROFILE,
                arguments = listOf(navArgument("userId") { type = NavType.StringType }),
            ) {
                UserProfileScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
