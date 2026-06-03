package com.oltvi.neural.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import com.oltvi.neural.ui.guide.GuiaScreen
import com.oltvi.neural.ui.missions.MisionesScreen
import com.oltvi.neural.ui.onboarding.OnboardingScreen
import com.oltvi.neural.ui.profile.ProfileScreen
import com.oltvi.neural.ui.world.WorldScreen

sealed class NeuralRoute(val route: String) {
    object Onboarding : NeuralRoute("onboarding")
    object World : NeuralRoute("world")
    object Profile : NeuralRoute("profile")
    object Misiones : NeuralRoute("misiones")
    object Guia : NeuralRoute("guia")
}

private val DEFAULT_PERFIL = PerfilNeural(
    id = "default",
    nombre = "Jugador",
    clase = ClaseRPG.EXPLORADOR,
    objetivo = ObjetivoVida.TRABAJO
)

@Composable
fun NeuralNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NeuralRoute.Onboarding.route,
    perfil: PerfilNeural? = null,
    onboardingComplete: ((PerfilNeural) -> Unit)? = null
) {
    val perfilActual = perfil ?: DEFAULT_PERFIL

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(280)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(280)
            )
        },
        exitTransition = {
            fadeOut(tween(200)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(200)
            )
        },
        popEnterTransition = {
            fadeIn(tween(280)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(280)
            )
        },
        popExitTransition = {
            fadeOut(tween(200)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(200)
            )
        }
    ) {
        composable(NeuralRoute.Onboarding.route) {
            OnboardingScreen(
                onComplete = { nuevoPerfil ->
                    onboardingComplete?.invoke(nuevoPerfil)
                    navController.navigate(NeuralRoute.World.route) {
                        popUpTo(NeuralRoute.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NeuralRoute.World.route) {
            WorldScreen(
                perfil = perfilActual,
                onNavigateToProfile = { navController.navigate(NeuralRoute.Profile.route) },
                onNavigateToMisiones = { navController.navigate(NeuralRoute.Misiones.route) },
                onNavigateToGuia = { navController.navigate(NeuralRoute.Guia.route) }
            )
        }

        composable(NeuralRoute.Profile.route) {
            ProfileScreen(
                perfil = perfilActual,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NeuralRoute.Misiones.route) {
            MisionesScreen(
                objetivo = perfilActual.objetivo,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NeuralRoute.Guia.route) {
            GuiaScreen(
                perfil = perfilActual,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
