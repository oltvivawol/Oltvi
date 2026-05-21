package com.oltvi.conductor.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oltvi.conductor.screens.components.ConductorBottomNavBar
import com.oltvi.conductor.screens.ganancias.GananciasScreen
import com.oltvi.conductor.screens.home.HomeScreen
import com.oltvi.conductor.screens.operacion.OperacionScreen
import com.oltvi.conductor.screens.perfil.PerfilScreen
import com.oltvi.core.theme.LocalOltviColors

/**
 * Top-level driver-app routes. Kept as a sealed class so the nav code is
 * exhaustive and refactor-safe; the [route] string is the actual key that
 * `NavHost`/`navigate()` resolves against.
 */
sealed class ConductorRoute(val route: String) {
    /** HUD-style command centre: map, online toggle, suggestions, incoming requests. */
    object Home : ConductorRoute("home")
    /** Active trip management — pickup, in-route, complete. */
    object Operacion : ConductorRoute("operacion")
    /** Earnings dashboard (Hoy / Semana / Mes + bar chart + insights). */
    object Ganancias : ConductorRoute("ganancias")
    /** Driver profile, vehicle and document status. */
    object Perfil : ConductorRoute("perfil")
}

/**
 * Top-level navigation graph for OLTVI Trabajo (driver app).
 *
 * Routes: `home` (default) -> `operacion` -> `ganancias` -> `perfil`.
 * Bottom-nav has 4 tabs and is always visible (no splash on the driver side —
 * we want the cockpit to be the first thing the driver sees).
 */
@Composable
fun ConductorNavGraph(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val colors = LocalOltviColors.current

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            ConductorBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route.route) {
                        // Save state for each top-level destination; collapse
                        // back-stack so tabs don't pile up.
                        popUpTo(ConductorRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
                startDestination = ConductorRoute.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(ConductorRoute.Home.route) {
                    HomeScreen(
                        onTripAccepted = {
                            navController.navigate(ConductorRoute.Operacion.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(ConductorRoute.Operacion.route) {
                    OperacionScreen(
                        onTripCompleted = {
                            navController.navigate(ConductorRoute.Home.route) {
                                popUpTo(ConductorRoute.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(ConductorRoute.Ganancias.route) {
                    GananciasScreen()
                }
                composable(ConductorRoute.Perfil.route) {
                    PerfilScreen()
                }
            }
        }
    }
}
