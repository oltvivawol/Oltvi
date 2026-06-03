package com.oltvi.neural.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oltvi.neural.data.AvatarConfig
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import com.oltvi.neural.ui.avatar.AvatarCustomizerScreen
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
    object AvatarCustomizer : NeuralRoute("avatar_customizer")
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
    onboardingComplete: ((PerfilNeural) -> Unit)? = null,
    onPerfilUpdate: ((PerfilNeural) -> Unit)? = null
) {
    var perfilLocal by remember { mutableStateOf(perfil ?: DEFAULT_PERFIL) }

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
                    perfilLocal = nuevoPerfil
                    onboardingComplete?.invoke(nuevoPerfil)
                    navController.navigate(NeuralRoute.AvatarCustomizer.route) {
                        popUpTo(NeuralRoute.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NeuralRoute.AvatarCustomizer.route) {
            AvatarCustomizerScreen(
                clase = perfilLocal.clase,
                initialConfig = perfilLocal.avatar,
                onConfirm = { avatarConfig ->
                    perfilLocal = perfilLocal.copy(avatar = avatarConfig)
                    onPerfilUpdate?.invoke(perfilLocal)
                    navController.navigate(NeuralRoute.World.route) {
                        popUpTo(NeuralRoute.AvatarCustomizer.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NeuralRoute.World.route) {
            WorldScreen(
                perfil = perfilLocal,
                onNavigateToProfile = { navController.navigate(NeuralRoute.Profile.route) },
                onNavigateToMisiones = { navController.navigate(NeuralRoute.Misiones.route) },
                onNavigateToGuia = { navController.navigate(NeuralRoute.Guia.route) },
                onNavigateToAvatar = { navController.navigate(NeuralRoute.AvatarCustomizer.route) }
            )
        }

        composable(NeuralRoute.Profile.route) {
            ProfileScreen(
                perfil = perfilLocal,
                onBack = { navController.popBackStack() },
                onCustomizeAvatar = { navController.navigate(NeuralRoute.AvatarCustomizer.route) }
            )
        }

        composable(NeuralRoute.Misiones.route) {
            MisionesScreen(
                objetivo = perfilLocal.objetivo,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NeuralRoute.Guia.route) {
            GuiaScreen(
                perfil = perfilLocal,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
