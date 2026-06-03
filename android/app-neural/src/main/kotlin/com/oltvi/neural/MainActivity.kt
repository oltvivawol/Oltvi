package com.oltvi.neural

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.oltvi.neural.data.PerfilNeural
import com.oltvi.neural.navigation.NeuralNavGraph
import com.oltvi.neural.navigation.NeuralRoute
import com.oltvi.neural.theme.NeuralColors
import com.oltvi.neural.theme.NeuralTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NeuralTheme {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        darkIcons = false
                    )
                    systemUiController.setNavigationBarColor(
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        darkIcons = false,
                        navigationBarContrastEnforced = false
                    )
                }

                var perfil by remember { mutableStateOf<PerfilNeural?>(null) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NeuralColors.deep)
                ) {
                    NeuralNavGraph(
                        modifier = Modifier.fillMaxSize(),
                        startDestination = if (perfil != null) NeuralRoute.World.route
                        else NeuralRoute.Onboarding.route,
                        onboardingComplete = { nuevoPerfil ->
                            perfil = nuevoPerfil
                        },
                        perfil = perfil
                    )
                }
            }
        }
    }
}
