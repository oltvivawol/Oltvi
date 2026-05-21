package com.oltvi.usuario.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.components.OltviLogo
import com.oltvi.core.ui.effects.ParticleField
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {

    // Animation trigger state
    var animating by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200L)
        animating = true
        delay(500L)
        showTagline = true
        delay(2100L)
        onNavigateToHome()
    }

    // Logo fade + scale
    val logoAlpha by animateFloatAsState(
        targetValue = if (animating) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (animating) 1f else 0.72f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    // Tagline fades in after logo
    val taglineAlpha by animateFloatAsState(
        targetValue = if (showTagline) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "taglineAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(OltviColors.PrincipalDeep, OltviColors.PrincipalDark, OltviColors.PrincipalDeep)
                )
            )
    ) {
        // Animated particle starfield background
        ParticleField(modifier = Modifier.fillMaxSize())

        // Radial glow behind logo — atmospheric amber aura
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .alpha(logoAlpha * 0.7f)
                .background(
                    Brush.radialGradient(
                        listOf(
                            OltviColors.Action.copy(alpha = 0.28f),
                            OltviColors.ActionLight.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Center content: logo + tagline
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(logoAlpha)
                .scale(logoScale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OltviLogo(size = 52.sp)

            Spacer(modifier = Modifier.height(10.dp))

            // Amber divider line
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, OltviColors.Action, Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tu ruta, nuestra inteligencia",
                style = OltviTypography.subtitulo,
                color = OltviColors.OnSurfaceDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }

        // Bottom AI badge
        Text(
            text = "Impulsado por Inteligencia Artificial",
            style = OltviTypography.etiqueta,
            color = OltviColors.OnSurfaceDim.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp)
                .alpha(taglineAlpha)
        )
    }
}
