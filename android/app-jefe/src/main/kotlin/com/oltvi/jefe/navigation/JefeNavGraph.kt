package com.oltvi.jefe.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.jefe.screens.dashboard.DashboardScreen
import com.oltvi.jefe.screens.flota.FlotaScreen
import com.oltvi.jefe.screens.analiticas.AnaliticasScreen
import com.oltvi.jefe.screens.incidentes.IncidentesScreen

sealed class JefeRoute(val route: String) {
    object Dashboard : JefeRoute("dashboard")
    object Flota : JefeRoute("flota")
    object Analiticas : JefeRoute("analiticas")
    object Incidentes : JefeRoute("incidentes")
}

@Composable
fun JefeNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = listOf(
        Triple(JefeRoute.Dashboard, Icons.Filled.Dashboard, "Dashboard"),
        Triple(JefeRoute.Flota, Icons.Filled.LocalShipping, "Flota"),
        Triple(JefeRoute.Analiticas, Icons.Filled.BarChart, "Analiticas"),
        Triple(JefeRoute.Incidentes, Icons.Filled.Warning, "Incidentes")
    )

    Scaffold(
        modifier = modifier,
        containerColor = OltviColors.PrincipalDeep,
        bottomBar = {
            NavigationBar(containerColor = OltviColors.Surface.copy(alpha = 0.95f)) {
                tabs.forEach { (route, icon, label) ->
                    val isSelected = currentRoute == route.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(route.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        },
                        icon = {
                            Icon(
                                icon,
                                label,
                                tint = if (isSelected) OltviColors.Action else OltviColors.OnSurfaceDim
                            )
                        },
                        label = {
                            Text(
                                label,
                                color = if (isSelected) OltviColors.Action else OltviColors.OnSurfaceDim,
                                style = OltviTypography.etiqueta
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = OltviColors.Action.copy(0.15f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = JefeRoute.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(JefeRoute.Dashboard.route) { DashboardScreen() }
            composable(JefeRoute.Flota.route) { FlotaScreen() }
            composable(JefeRoute.Analiticas.route) { AnaliticasScreen() }
            composable(JefeRoute.Incidentes.route) { IncidentesScreen() }
        }
    }
}
