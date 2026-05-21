package com.oltvi.conductor.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberInfiniteTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.oltvi.conductor.screens.components.HeatZoneOverlay
import com.oltvi.core.data.models.Servicio
import com.oltvi.core.data.models.SugerenciaCopiloto
import com.oltvi.core.theme.LocalOltviColors
import com.oltvi.core.ui.effects.PulseRing
import com.oltvi.core.ui.effects.neonGlow
import kotlinx.coroutines.launch

/**
 * Darker, more orange-tinted variant of the OLTVI map style used by the driver
 * app — emphasises arterial roads with a stronger orange stroke so the driver
 * can read the network at a glance, even in bright sunlight.
 */
private const val DRIVER_MAP_STYLE = """
[{"elementType":"geometry","stylers":[{"color":"#0F1820"}]},
{"elementType":"labels.text.fill","stylers":[{"color":"#A8765A"}]},
{"elementType":"labels.text.stroke","stylers":[{"color":"#0A1218"}]},
{"featureType":"road","elementType":"geometry","stylers":[{"color":"#1F2B36"}]},
{"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#8B4A10"},{"weight":0.3}]},
{"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#2D3F4F"}]},
{"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#E67E22"},{"weight":0.9}]},
{"featureType":"road.arterial","elementType":"geometry.stroke","stylers":[{"color":"#F39C12"},{"weight":0.6}]},
{"featureType":"water","elementType":"geometry","stylers":[{"color":"#08111A"}]},
{"featureType":"poi","stylers":[{"visibility":"off"}]},
{"featureType":"transit","stylers":[{"visibility":"off"}]},
{"featureType":"landscape","elementType":"geometry","stylers":[{"color":"#141F2A"}]},
{"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#E67E22"},{"weight":0.4}]}]
"""

/**
 * Driver HUD home screen — the "fighter jet cockpit" view. Fullscreen map,
 * online/offline toggle, AgenteCopiloto suggestion, heat zones, top stats
 * HUD, bottom shift bar, and incoming-request modal sliding in from the right.
 *
 * @param onTripAccepted Hoists navigation up to the NavGraph when the driver
 *                       accepts a ride request.
 */
@Composable
fun HomeScreen(
    onTripAccepted: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navTarget by viewModel.navegarAOperacion.collectAsStateWithLifecycle()
    val colors = LocalOltviColors.current
    val scope = rememberCoroutineScope()

    // Hoisted navigation effect — kept side-effect-free w.r.t. the ViewModel.
    LaunchedEffect(navTarget) {
        if (navTarget != null) {
            onTripAccepted()
            viewModel.navegacionConsumida()
        }
    }

    val fallback = remember { LatLng(-34.6037, -58.3816) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(state.userLocation ?: fallback)
            .zoom(14f)
            .tilt(48f)
            .bearing(25f)
            .build()
    }

    LaunchedEffect(state.userLocation) {
        state.userLocation?.let { loc ->
            scope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(loc)
                            .zoom(15f)
                            .tilt(50f)
                            .bearing(cameraPositionState.position.bearing)
                            .build()
                    ),
                    durationMs = 1200
                )
            }
        }
    }

    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = MapStyleOptions(DRIVER_MAP_STYLE),
            isMyLocationEnabled = false
        )
    }
    val mapUiSettings = remember {
        MapUiSettings(
            compassEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            tiltGesturesEnabled = true,
            rotationGesturesEnabled = true
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.surfaceDark)) {

        // ── Fullscreen map ────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            val userPos = state.userLocation ?: fallback

            // Heat zones (only when online — gives a clear visual reward for
            // going online).
            if (state.isOnline) {
                HeatZoneOverlay(zonas = state.zonasCalientes)
            }

            Marker(
                state = MarkerState(position = userPos),
                title = "Tu posición",
                snippet = if (state.isOnline) "ONLINE" else "OFFLINE"
            )
        }

        // ── Driver pulse ring on map centre ──────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            PulseRing(
                color = if (state.isOnline) colors.success else colors.glassBorder,
                maxRadiusDp = 48.dp,
                modifier = Modifier.size(96.dp)
            )
        }

        // ── Dim overlay when offline so the HUD chrome reads stronger ────
        val dim by animateFloatAsState(
            targetValue = if (state.isOnline) 0f else 0.45f,
            animationSpec = tween(800),
            label = "offline-dim"
        )
        if (dim > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.surfaceDark.copy(alpha = dim))
            )
        }

        // ── TOP-CENTER: ONLINE / OFFLINE toggle ──────────────────────────
        OnlineToggle(
            isOnline = state.isOnline,
            onClick = { viewModel.toggleOnline() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
        )

        // ── TOP-RIGHT: stats HUD ─────────────────────────────────────────
        StatsHud(
            ganancias = state.gananciasHoy,
            viajes = state.viajesHoy,
            rating = state.ratingActual,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 12.dp)
        )

        // ── Recenter button (left side) ─────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(colors.surfaceMid.copy(alpha = 0.92f))
                .border(1.dp, colors.glassBorder, CircleShape)
                .clickable {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                state.userLocation ?: fallback, 16f
                            ),
                            durationMs = 800
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.MyLocation,
                contentDescription = "Centrar mapa",
                tint = colors.action,
                modifier = Modifier.size(22.dp)
            )
        }

        // ── Floating SugerenciaCopiloto card ─────────────────────────────
        AnimatedVisibility(
            visible = state.sugerencia != null && state.solicitudEntrante == null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 110.dp, start = 16.dp, end = 16.dp),
            enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
        ) {
            state.sugerencia?.let { sugerencia ->
                CopilotSuggestionCard(
                    sugerencia = sugerencia,
                    onAction = { viewModel.aplicarSugerencia() },
                    onDismiss = { viewModel.limpiarSugerencia() }
                )
            }
        }

        // ── Incoming request modal (slides in from right) ────────────────
        AnimatedVisibility(
            visible = state.solicitudEntrante != null,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            state.solicitudEntrante?.let { servicio ->
                IncomingRequestCard(
                    servicio = servicio,
                    distanciaKm = state.distanciaSolicitudKm,
                    ratingPasajero = state.ratingPasajero,
                    onAccept = { viewModel.aceptarSolicitud(servicio) },
                    onReject = { viewModel.rechazarSolicitud(servicio) },
                    onAutoReject = { viewModel.rechazarSolicitud(servicio) }
                )
            }
        }

        // ── Bottom mini stats bar ────────────────────────────────────────
        ShiftHudBar(
            isOnline = state.isOnline,
            tiempoActivoMs = state.tiempoActivo,
            gananciasHoy = state.gananciasHoy,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// HUD sub-components
// ──────────────────────────────────────────────────────────────────────────────

/**
 * The signature element of the HUD: a circular ONLINE / OFFLINE toggle.
 * Green neon glow + pulsing aura when online, muted grey when offline.
 */
@Composable
private fun OnlineToggle(
    isOnline: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalOltviColors.current
    val targetColor = if (isOnline) colors.success else Color(0xFF4A5A6A)

    val transition = rememberInfiniteTransition(label = "online-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val effectiveScale = if (isOnline) pulse else 1f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .scale(effectiveScale)
                .size(96.dp)
                .neonGlow(
                    color = if (isOnline) colors.success else Color.Transparent,
                    blurRadius = 30.dp,
                    cornerRadius = 48.dp
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            targetColor.copy(alpha = 0.85f),
                            colors.surfaceDark.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            targetColor,
                            targetColor.copy(alpha = 0.4f),
                            targetColor
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PowerSettingsNew,
                contentDescription = if (isOnline) "Desconectarse" else "Conectarse",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isOnline) "ONLINE" else "OFFLINE",
            color = if (isOnline) colors.success else Color(0xFF8BA4BB),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Text(
            text = if (isOnline) "Recibiendo solicitudes" else "Tocá para conectarte",
            color = Color(0xFF8BA4BB),
            fontSize = 11.sp
        )
    }
}

/**
 * Top-right stats HUD — todays earnings, trips, rating. Glass card with thin
 * action-coloured border.
 */
@Composable
private fun StatsHud(
    ganancias: Double,
    viajes: Int,
    rating: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalOltviColors.current
    Column(
        modifier = modifier
            .width(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.surfaceMid.copy(alpha = 0.92f),
                        colors.surfaceDark.copy(alpha = 0.95f)
                    )
                )
            )
            .border(1.dp, colors.glassBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            "HOY",
            color = colors.action,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "$" + "%,.0f".format(ganancias),
            color = colors.success,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bolt, null, tint = colors.actionLight, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("$viajes viajes", color = Color(0xFFE8F0F8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, null, tint = colors.warning, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("%.2f".format(rating), color = colors.warning, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Floating glass card showing the AgenteCopiloto suggestion. Tappable
 * action button on the right; small dismiss tap target.
 */
@Composable
private fun CopilotSuggestionCard(
    sugerencia: SugerenciaCopiloto,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalOltviColors.current
    val urgent = sugerencia.urgencia.equals("alta", ignoreCase = true) ||
        sugerencia.urgencia.equals("critica", ignoreCase = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neonGlow(
                color = if (urgent) colors.action.copy(alpha = 0.5f) else colors.actionLight.copy(alpha = 0.3f),
                blurRadius = 22.dp,
                cornerRadius = 18.dp
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        colors.surfaceMid.copy(alpha = 0.97f),
                        colors.surfaceDark.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(colors.action, colors.actionLight)
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colors.action.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = colors.action,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                sugerencia.titulo,
                color = Color(0xFFE8F0F8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                sugerencia.descripcion,
                color = Color(0xFFB8C4D0),
                fontSize = 12.sp,
                maxLines = 2
            )
            if (sugerencia.potencialGanancia > 0.0) {
                Text(
                    "Potencial: $" + "%,.0f".format(sugerencia.potencialGanancia),
                    color = colors.success,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.action)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    sugerencia.accionInmediata ?: "Ir",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "✕ omitir",
                color = Color(0xFF8BA4BB),
                fontSize = 10.sp,
                modifier = Modifier.clickable(onClick = onDismiss)
            )
        }
    }
}

/**
 * Bottom glass bar showing how long the driver has been online and total
 * earnings so far in this shift.
 */
@Composable
private fun ShiftHudBar(
    isOnline: Boolean,
    tiempoActivoMs: Long,
    gananciasHoy: Double,
    modifier: Modifier = Modifier
) {
    val colors = LocalOltviColors.current
    val seconds = tiempoActivoMs / 1000
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val timeStr = if (h > 0) "${h}h ${m}m" else "${m}m"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        colors.surfaceMid.copy(alpha = 0.95f),
                        colors.surfaceDark.copy(alpha = 0.97f),
                        colors.surfaceMid.copy(alpha = 0.95f),
                    )
                )
            )
            .border(1.dp, colors.glassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    if (isOnline) colors.success else Color(0xFF8BA4BB),
                    CircleShape
                )
        )
        Spacer(Modifier.width(10.dp))
        Icon(Icons.Filled.Timer, null, tint = colors.action, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            "Activo: $timeStr",
            color = Color(0xFFE8F0F8),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Ganando:",
            color = Color(0xFF8BA4BB),
            fontSize = 11.sp
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "$" + "%,.0f".format(gananciasHoy),
            color = colors.success,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

// Helper used by IncomingRequestCard to expose the navigation listener cleanly.
@Composable
internal fun rememberIncomingHandler(
    onAccept: (Servicio) -> Unit
): (Servicio) -> Unit = remember { onAccept }
