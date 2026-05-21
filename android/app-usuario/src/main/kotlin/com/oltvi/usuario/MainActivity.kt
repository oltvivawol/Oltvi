package com.oltvi.usuario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTheme
import com.oltvi.usuario.navigation.OltviUsuarioNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge: app drives status/navigation bar visuals
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            OltviTheme {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )
                    systemUiController.setNavigationBarColor(
                        color = Color.Transparent,
                        darkIcons = false,
                        navigationBarContrastEnforced = false
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OltviColors.PrincipalDeep)
                ) {
                    OltviUsuarioNavGraph(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
