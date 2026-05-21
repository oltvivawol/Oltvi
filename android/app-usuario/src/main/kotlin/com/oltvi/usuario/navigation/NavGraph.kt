package com.oltvi.usuario.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oltvi.core.theme.OltviColors
import com.oltvi.usuario.screens.chat.ChatScreen
import com.oltvi.usuario.screens.components.DefaultBottomNavItems
import com.oltvi.usuario.screens.components.OltviBottomNavBar
import com.oltvi.usuario.screens.home.HomeScreen
import com.oltvi.usuario.screens.login.LoginScreen
import com.oltvi.usuario.screens.payment.PaymentScreen
import com.oltvi.usuario.screens.profile.ProfileScreen
import com.oltvi.usuario.screens.splash.SplashScreen
import com.oltvi.usuario.screens.tracking.TrackingScreen

sealed class OltviRoute(val route: String) {
    data object Login : OltviRoute("login")
    data object Splash : OltviRoute("splash")
    data object Home : OltviRoute("home")
    data object Trips : OltviRoute("trips")
    data object Payments : OltviRoute("payment")
    data object Profile : OltviRoute("profile")
    data object Chat : OltviRoute("chat")
    data object Tracking : OltviRoute("tracking/{servicioId}") {
        const val ARG_SERVICIO_ID = "servicioId"
        fun build(servicioId: String): String = "tracking/$servicioId"
    }
}

private val MainTabs = setOf(
    OltviRoute.Home.route,
    OltviRoute.Trips.route,
    OltviRoute.Payments.route,
    OltviRoute.Profile.route
)

@Composable
fun OltviUsuarioNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in MainTabs

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OltviColors.surfaceDark)
    ) {
        NavHost(
            navController = navController,
            startDestination = OltviRoute.Login.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(OltviRoute.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(OltviRoute.Splash.route) {
                        popUpTo(OltviRoute.Login.route) { inclusive = true }
                    }
                })
            }
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
                        navController.navigate(OltviRoute.Tracking.build("active-001"))
                    },
                    onNavigateToChat = {
                        navController.navigate(OltviRoute.Chat.route)
                    }
                )
            }
            composable(
                route = OltviRoute.Tracking.route,
                arguments = listOf(navArgument(OltviRoute.Tracking.ARG_SERVICIO_ID) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val servicioId = backStackEntry.arguments
                    ?.getString(OltviRoute.Tracking.ARG_SERVICIO_ID)
                    ?: "active-001"
                TrackingScreen(servicioId = servicioId)
            }
            composable(OltviRoute.Trips.route) {
                ChatScreen()
            }
            composable(OltviRoute.Payments.route) {
                PaymentScreen()
            }
            composable(OltviRoute.Profile.route) {
                ProfileScreen()
            }
            composable(OltviRoute.Chat.route) {
                ChatScreen()
            }
        }

        AnimatedVisibility(
            visible = showBottomBar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            OltviBottomNavBar(
                items = DefaultBottomNavItems,
                currentRoute = currentRoute,
                onItemSelected = { item ->
                    navController.navigate(item.route) {
                        popUpTo(OltviRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
