package com.oltvi.conductor.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.oltvi.conductor.screens.login.ConductorLoginScreen
import com.oltvi.conductor.screens.operacion.OperacionScreen
import com.oltvi.conductor.screens.perfil.PerfilScreen

sealed class ConductorRoute(val route: String) {
    object Login : ConductorRoute("login")
    object Home : ConductorRoute("home")
    object Operacion : ConductorRoute("operacion")
    object Ganancias : ConductorRoute("ganancias")
    object Perfil : ConductorRoute("perfil")
}

private val BottomNavRoutes = setOf(
    ConductorRoute.Home.route,
    ConductorRoute.Ganancias.route,
    ConductorRoute.Perfil.route,
)

@Composable
fun ConductorNavGraph(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in BottomNavRoutes

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar, enter = fadeIn(), exit = fadeOut()) {
                ConductorBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route.route) {
                            popUpTo(ConductorRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
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
                startDestination = ConductorRoute.Login.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(ConductorRoute.Login.route) {
                    ConductorLoginScreen(onLoginSuccess = {
                        navController.navigate(ConductorRoute.Home.route) {
                            popUpTo(ConductorRoute.Login.route) { inclusive = true }
                        }
                    })
                }
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
