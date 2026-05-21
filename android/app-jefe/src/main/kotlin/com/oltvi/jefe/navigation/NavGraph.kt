package com.oltvi.jefe.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oltvi.jefe.screens.analiticas.AnaliticasScreen
import com.oltvi.jefe.screens.components.BottomNavBar
import com.oltvi.jefe.screens.dashboard.DashboardScreen
import com.oltvi.jefe.screens.flota.FlotaScreen
import com.oltvi.jefe.screens.incidentes.IncidentesScreen
import com.oltvi.jefe.screens.login.JefeLoginScreen

sealed class JefeRoute(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Login : JefeRoute("login", "Login", Icons.Filled.SpaceDashboard)
    object Dashboard : JefeRoute("dashboard", "Mando", Icons.Filled.SpaceDashboard)
    object Flota : JefeRoute("flota", "Flota", Icons.Filled.LocalShipping)
    object Analiticas : JefeRoute("analiticas", "Analíticas", Icons.Filled.Analytics)
    object Incidentes : JefeRoute("incidentes", "Incidentes", Icons.Filled.WarningAmber)

    companion object {
        val all: List<JefeRoute> = listOf(Dashboard, Flota, Analiticas, Incidentes)
    }
}

private val BottomNavRoutes = JefeRoute.all.map { it.route }.toSet()

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: JefeRoute.Login.route

    val showBottomBar = currentRoute in BottomNavRoutes

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar, enter = fadeIn(), exit = fadeOut()) {
                BottomNavBar(
                    items = JefeRoute.all,
                    currentRoute = currentRoute,
                    onSelect = { route ->
                        if (route.route != currentRoute) {
                            navController.navigate(route.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = JefeRoute.Login.route
            ) {
                composable(JefeRoute.Login.route) {
                    JefeLoginScreen(onLoginSuccess = {
                        navController.navigate(JefeRoute.Dashboard.route) {
                            popUpTo(JefeRoute.Login.route) { inclusive = true }
                        }
                    })
                }
                composable(JefeRoute.Dashboard.route) { DashboardScreen() }
                composable(JefeRoute.Flota.route) { FlotaScreen() }
                composable(JefeRoute.Analiticas.route) { AnaliticasScreen() }
                composable(JefeRoute.Incidentes.route) { IncidentesScreen() }
            }
        }
    }
}
