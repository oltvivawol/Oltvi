package com.oltvi.usuario.screens.tracking

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.components.OltviButton
import com.oltvi.core.ui.components.OltviButtonVariant
import com.oltvi.core.ui.effects.GlassCard
import kotlinx.coroutines.delay

private const val DARK_MAP_STYLE = """
[{"elementType":"geometry","stylers":[{"color":"#1a2b3c"}]},
{"elementType":"labels.text.fill","stylers":[{"color":"#8ba4bb"}]},
{"elementType":"labels.text.stroke","stylers":[{"color":"#0d1a26"}]},
{"featureType":"road","elementType":"geometry","stylers":[{"color":"#23374d"}]},
{"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#2d4357"}]},
{"featureType":"water","elementType":"geometry","stylers":[{"color":"#0d1a26"}]},
{"featureType":"poi","stylers":[{"visibility":"off"}]},
{"featureType":"transit","stylers":[{"visibility":"off"}]}]
"""

// Trip states
private enum class TripState(val label: String, val step: Int) {
    EN_CAMINO("En camino", 0),
    LLEGANDO("Llegando", 1),
    EN_RUTA("En ruta", 2)
}

@Composable
fun TrackingScreen(servicioId: String = "active-001") {
    // Hold the active service id for future telemetry/monitor calls.
    @Suppress("UNUSED_VARIABLE")
    val trackedId = servicioId
    // User destination (fixed)
    val userDestination = LatLng(-34.5797, -58.4325)
    val userOrigin = LatLng(-34.6037, -58.3816)

    // Driver starts 850m away, moves closer
    var driverLat by remember { mutableDoubleStateOf(-34.6037 - 0.003) }
    var driverLng by remember { mutableDoubleStateOf(-58.3816 + 0.005) }

    var etaMinutes by remember { mutableIntStateOf(7) }
    var tripState by remember { mutableStateOf(TripState.EN_CAMINO) }
    var isSecure by remember { mutableStateOf(true) }

    // Smooth lat/lng animation
    val animatedLat by animateFloatAsState(
        targetValue = driverLat.toFloat(),
        animationSpec = tween(durationMillis = 4800, easing = FastOutSlowInEasing),
        label = "driverLat"
    )
    val animatedLng by animateFloatAsState(
        targetValue = driverLng.toFloat(),
        animationSpec = tween(durationMillis = 4800, easing = FastOutSlowInEasing),
        label = "driverLng"
    )

    // Mock driver movement: approaches every 5 seconds
    LaunchedEffect(Unit) {
        var steps = 0
        while (steps < 10) {
            delay(5000L)
            steps++
            // Gradually move driver toward user origin
            val progress = steps / 10f
            driverLat = (-34.6037 - 0.003) + 0.003 * progress
            driverLng = (-58.3816 + 0.005) - 0.005 * progress
            etaMinutes = (7 * (1f - progress)).toInt().coerceAtLeast(1)

            tripState = when {
                progress < 0.3f -> TripState.EN_CAMINO
                progress < 0.7f -> TripState.LLEGANDO
                else -> TripState.EN_RUTA
            }
        }
    }

    val driverPosition = LatLng(animatedLat.toDouble(), animatedLng.toDouble())

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(driverPosition)
            .zoom(14f)
            .tilt(45f)
            .build()
    }

    val mapProperties = remember {
        MapProperties(mapStyleOptions = MapStyleOptions(DARK_MAP_STYLE))
    }
    val mapUiSettings = remember {
        MapUiSettings(
            compassEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── FULL-SCREEN MAP ──────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // Neon orange route polyline
            Polyline(
                points = listOf(driverPosition, userOrigin, userDestination),
                color = OltviColors.Action,
                width = 10f
            )

            // Driver marker
            Marker(
                state = MarkerState(position = driverPosition),
                title = "Carlos M. · Toyota Corolla",
                snippet = "$etaMinutes min restantes"
            )

            // User origin marker
            Marker(
                state = MarkerState(position = userOrigin),
                title = "Tu ubicación"
            )

            // Destination marker
            Marker(
                state = MarkerState(position = userDestination),
                title = "Destino: Palermo"
            )
        }

        // ── BOTTOM TRACKING CARD ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(OltviColors.Surface.copy(alpha = 0.98f), OltviColors.PrincipalDark)
                    )
                )
                .border(
                    1.dp,
                    OltviColors.GlassBorder,
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            // ── Trip progress indicator ──────────────────────────────────────
            TripProgressIndicator(currentState = tripState)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Driver + ETA row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Driver info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                Brush.radialGradient(listOf(OltviColors.Action, OltviColors.ActionDim)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("C", style = OltviTypography.titulo.copy(color = OltviColors.White))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Carlos M.", style = OltviTypography.subtitulo.copy(color = OltviColors.OnSurface))
                        Text(
                            "Toyota Corolla · AB123CD",
                            style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(5) {
                                Text("★", style = OltviTypography.pequeno.copy(color = OltviColors.Warning, fontSize = 10.sp))
                            }
                        }
                    }
                }

                // ETA counter with pulse
                EtaBadge(eta = etaMinutes)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Security badge ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSecure) OltviColors.Success.copy(alpha = 0.1f)
                        else OltviColors.Error.copy(alpha = 0.1f)
                    )
                    .border(
                        1.dp,
                        if (isSecure) OltviColors.Success.copy(alpha = 0.4f)
                        else OltviColors.Error.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSecure) Icons.Filled.Shield else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (isSecure) OltviColors.Success else OltviColors.Error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSecure) "Viaje seguro · Ruta verificada" else "Atención: desviación de ruta",
                    style = OltviTypography.etiqueta,
                    color = if (isSecure) OltviColors.Success else OltviColors.Error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Action buttons ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OltviButton(
                    text = "Cancelar",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    variant = OltviButtonVariant.Ghost
                )
                OltviButton(
                    text = "SOS",
                    onClick = {},
                    modifier = Modifier.weight(0.7f),
                    variant = OltviButtonVariant.Danger
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OltviColors.Action.copy(alpha = 0.15f))
                        .border(1.dp, OltviColors.Action.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {},
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = "Chat", tint = OltviColors.Action, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

// ── Trip progress 3-step indicator ───────────────────────────────────────────

@Composable
private fun TripProgressIndicator(currentState: TripState) {
    val steps = TripState.values()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isDone = step.step < currentState.step
            val isActive = step.step == currentState.step

            // Step circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        when {
                            isDone -> OltviColors.Success
                            isActive -> OltviColors.Action
                            else -> OltviColors.SurfaceVariant
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = OltviTypography.etiqueta.copy(fontWeight = FontWeight.Bold),
                    color = if (isDone || isActive) OltviColors.White else OltviColors.OnSurfaceDim
                )
            }

            // Connecting line (not after last step)
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (isDone) OltviColors.Success else OltviColors.Divider,
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Step labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEach { step ->
            val isActive = step.step == currentState.step
            Text(
                text = step.label,
                style = OltviTypography.etiqueta,
                color = if (isActive) OltviColors.Action else OltviColors.OnSurfaceDim,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ── ETA Badge with pulse ──────────────────────────────────────────────────────

@Composable
private fun EtaBadge(eta: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "etaPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(OltviColors.Action.copy(alpha = 0.12f))
            .border(1.dp, OltviColors.Action.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = "$eta",
            style = OltviTypography.display.copy(fontSize = 30.sp),
            color = OltviColors.Action,
            modifier = Modifier.alpha(if (eta <= 2) pulseAlpha else 1f)
        )
        Text(
            text = "min",
            style = OltviTypography.etiqueta,
            color = OltviColors.OnSurfaceDim
        )
    }
}
