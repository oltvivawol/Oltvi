package com.oltvi.jefe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTheme
import com.oltvi.jefe.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity host for OLTVI Mando.
 *
 * - Edge-to-edge so glass panels can blend with the system bars.
 * - Wraps the whole tree with [OltviTheme] so screens & components resolve the
 *   brand palette via `LocalOltviColors` and Material's `colorScheme`.
 * - Defers all navigation, screens, and tabs to [NavGraph].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OltviTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    OltviColors.PrincipalDeep,
                                    OltviColors.Surface,
                                    OltviColors.PrincipalDeep
                                )
                            )
                        )
                ) {
                    NavGraph(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
