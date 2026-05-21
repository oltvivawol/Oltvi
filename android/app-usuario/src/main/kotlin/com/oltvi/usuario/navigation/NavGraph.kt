package com.oltvi.usuario.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.usuario.screens.chat.ChatScreen
import com.oltvi.usuario.screens.home.HomeScreen
import com.oltvi.usuario.screens.payment.PaymentScreen
import com.oltvi.usuario.screens.profile.ProfileScreen
import com.oltvi.usuario.screens.splash.SplashScreen
import com.oltvi.usuario.screens.tracking.TrackingScreen

// ── Routes ────────────────────────────────────────────────────────────────────

sealed class OltviRoute(val route: String) {
    object Splash : OltviRoute("splash")
    object Home : OltviRoute("home")
    object Tracking : OltviRoute("tracking")
    object Payment : OltviRoute("payment")
    object Profile : OltviRoute("profile")
    object Chat : OltviRoute("chat")
}

private data class BottomNavItem(
    val route: OltviRoute,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(OltviRoute.Home, Icons.Filled.Map, "Viajar"),
    BottomNavItem(OltviRoute.Tracking, Icons.Filled.MyLocation, "Seguir"),
    BottomNavItem(OltviRoute.Payment, Icons.Filled.Payment, "Pagos"),
    BottomNavItem(OltviRoute.Profile, Icons.Filled.Person, "Perfil"),
    BottomNavItem(OltviRoute.Chat, Icons.Filled.Chat, "Olivi")
)

// ── NavGraph ──────────────────────────────────────────────────────────────────

@Composable
fun OltviUsuarioNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != OltviRoute.Splash.route

    Scaffold(
        modifier = modifier,
        containerColor = OltviColors.PrincipalDeep,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = OltviColors.Surface.copy(alpha = 0.97f),
                    contentColor = OltviColors.OnSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route.route) {
                                    popUpTo(OltviRoute.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) OltviColors.Action else OltviColors.OnSurfaceDim
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = OltviTypography.etiqueta,
                                    color = if (isSelected) OltviColors.Action else OltviColors.OnSurfaceDim
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = OltviColors.Action.copy(alpha = 0.15f),
                                selectedIconColor = OltviColors.Action,
                                unselectedIconColor = OltviColors.OnSurfaceDim,
                                selectedTextColor = OltviColors.Action,
                                unselectedTextColor = OltviColors.OnSurfaceDim
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = OltviRoute.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(OltviRoute.Splash.route) {
                SplashScreen(onNavigateToHome = {
                    navController.navigate(OltviRoute.Home.route) {
                        popUpTo(OltviRoute.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(OltviRoute.Home.route) {
                HomeScreen(
                    onNavigateToTracking = {
                        navController.navigate(OltviRoute.Tracking.route)
                    }
                )
            }
            composable(OltviRoute.Tracking.route) {
                TrackingScreen()
            }
            composable(OltviRoute.Payment.route) {
                PaymentScreen()
            }
            composable(OltviRoute.Profile.route) {
                ProfileScreen()
            }
            composable(OltviRoute.Chat.route) {
                ChatScreen()
            }
        }
    }
}
