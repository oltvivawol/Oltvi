package com.oltvi.jefe.navigation

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

/**
 * Top-level routes the admin app exposes through its bottom navigation bar.
 *
 * Keeping these as a sealed class lets the bottom bar declare tabs as a list
 * of strongly-typed entries, and lets the [NavGraph] match composables to
 * routes without stringly-typed bookkeeping.
 */
sealed class JefeRoute(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Dashboard : JefeRoute("dashboard", "Mando", Icons.Filled.SpaceDashboard)
    object Flota : JefeRoute("flota", "Flota", Icons.Filled.LocalShipping)
    object Analiticas : JefeRoute("analiticas", "Analíticas", Icons.Filled.Analytics)
    object Incidentes : JefeRoute("incidentes", "Incidentes", Icons.Filled.WarningAmber)

    companion object {
        val all: List<JefeRoute> = listOf(Dashboard, Flota, Analiticas, Incidentes)
    }
}

/**
 * Main navigation graph for OLTVI Mando.
 *
 * Renders a [Scaffold] with the custom [BottomNavBar] and a [NavHost] that
 * swaps between the four operational screens. Default destination is the
 * Dashboard (operations command center).
 */
@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: JefeRoute.Dashboard.route

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = JefeRoute.Dashboard.route
            ) {
                composable(JefeRoute.Dashboard.route) { DashboardScreen() }
                composable(JefeRoute.Flota.route) { FlotaScreen() }
                composable(JefeRoute.Analiticas.route) { AnaliticasScreen() }
                composable(JefeRoute.Incidentes.route) { IncidentesScreen() }
            }
        }
    }
}
