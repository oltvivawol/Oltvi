package com.oltvi.conductor.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.conductor.screens.home.ConductorHomeScreen
import com.oltvi.conductor.screens.operacion.OperacionScreen
import com.oltvi.conductor.screens.ganancias.GananciasScreen
import com.oltvi.conductor.screens.perfil.PerfilConductorScreen

sealed class ConductorRoute(val route: String) {
    object Home : ConductorRoute("home")
    object Operacion : ConductorRoute("operacion")
    object Ganancias : ConductorRoute("ganancias")
    object Perfil : ConductorRoute("perfil")
}

@Composable
fun ConductorNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = listOf(
        Triple(ConductorRoute.Home, Icons.Filled.Map, "Mapa"),
        Triple(ConductorRoute.Operacion, Icons.Filled.DirectionsCar, "Operación"),
        Triple(ConductorRoute.Ganancias, Icons.Filled.AttachMoney, "Ganancias"),
        Triple(ConductorRoute.Perfil, Icons.Filled.Person, "Perfil")
    )

    Scaffold(
        modifier = modifier,
        containerColor = OltviColors.PrincipalDeep,
        bottomBar = {
            NavigationBar(containerColor = OltviColors.Surface.copy(alpha = 0.95f)) {
                tabs.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        selected = currentRoute == route.route,
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
                                tint = if (currentRoute == route.route) OltviColors.Action else OltviColors.OnSurfaceDim
                            )
                        },
                        label = {
                            Text(
                                label,
                                color = if (currentRoute == route.route) OltviColors.Action else OltviColors.OnSurfaceDim,
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
            startDestination = ConductorRoute.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ConductorRoute.Home.route) { ConductorHomeScreen() }
            composable(ConductorRoute.Operacion.route) { OperacionScreen() }
            composable(ConductorRoute.Ganancias.route) { GananciasScreen() }
            composable(ConductorRoute.Perfil.route) { PerfilConductorScreen() }
        }
    }
}
