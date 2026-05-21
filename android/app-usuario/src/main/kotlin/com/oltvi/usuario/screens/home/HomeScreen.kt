package com.oltvi.usuario.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.components.OltviButton
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.PulseRing
import com.oltvi.usuario.screens.booking.BookingSheet
import kotlinx.coroutines.launch

// Dark map style JSON — cinematic OLTVI palette
private const val DARK_MAP_STYLE = """
[{"elementType":"geometry","stylers":[{"color":"#1a2b3c"}]},
{"elementType":"labels.text.fill","stylers":[{"color":"#8ba4bb"}]},
{"elementType":"labels.text.stroke","stylers":[{"color":"#0d1a26"}]},
{"featureType":"road","elementType":"geometry","stylers":[{"color":"#23374d"}]},
{"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#2d4357"}]},
{"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#e67e22"},{"weight":0.5}]},
{"featureType":"water","elementType":"geometry","stylers":[{"color":"#0d1a26"}]},
{"featureType":"poi","stylers":[{"visibility":"off"}]},
{"featureType":"transit","stylers":[{"visibility":"off"}]},
{"featureType":"landscape","elementType":"geometry","stylers":[{"color":"#162232"}]}]
"""

@Composable
fun HomeScreen(
    onNavigateToTracking: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val buenosAiresLatLng = LatLng(-34.6037, -58.3816)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(uiState.userLocation ?: buenosAiresLatLng)
            .zoom(15f)
            .tilt(30f)
            .bearing(0f)
            .build()
    }

    // Animate camera when user location arrives
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { loc ->
            scope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(loc)
                            .zoom(15f)
                            .tilt(30f)
                            .build()
                    ),
                    durationMs = 1200
                )
            }
        }
    }

    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = MapStyleOptions(DARK_MAP_STYLE),
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

    Box(modifier = Modifier.fillMaxSize()) {

        // ── FULL-SCREEN DARK MAP ─────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // User location marker with pulse ring
            val userLoc = uiState.userLocation ?: buenosAiresLatLng
            Marker(
                state = MarkerState(position = userLoc),
                title = "Tu ubicación"
            )

            // Nearby driver markers
            uiState.nearbyDrivers.forEach { driver ->
                val driverPos = LatLng(driver.posicion.latitud, driver.posicion.longitud)
                Marker(
                    state = MarkerState(position = driverPos),
                    title = driver.nombre,
                    snippet = "${driver.tiempoEstimadoMin} min · ${driver.vehiculo.marca} ${driver.vehiculo.modelo}"
                )
            }
        }

        // User location pulse ring overlay
        val userLoc = uiState.userLocation ?: buenosAiresLatLng
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            PulseRing(
                color = OltviColors.Action,
                radiusDp = 40.dp,
                modifier = Modifier.size(80.dp)
            )
        }

        // ── TOP: SEARCH BAR ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OltviColors.Surface.copy(alpha = 0.94f))
                    .border(1.dp, OltviColors.GlassBorder, RoundedCornerShape(16.dp))
                    .clickable { viewModel.showBookingSheet(true) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(OltviColors.Action, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "¿A dónde vas?",
                    style = OltviTypography.cuerpo,
                    color = OltviColors.OnSurfaceDim,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Buscar por voz",
                    tint = OltviColors.Action,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ── RIGHT: MAP LAYER BUTTONS ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MapIconButton(
                icon = { Icon(Icons.Filled.MyLocation, null, tint = OltviColors.OnSurface, modifier = Modifier.size(22.dp)) },
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                uiState.userLocation ?: buenosAiresLatLng, 16f
                            ),
                            durationMs = 800
                        )
                    }
                }
            )
            MapIconButton(
                icon = { Icon(Icons.Filled.Layers, null, tint = OltviColors.OnSurface, modifier = Modifier.size(22.dp)) },
                onClick = { /* layer toggle */ }
            )
        }

        // ── BOTTOM: HUD CARD (always visible, sits above bottom nav) ────────
        AnimatedVisibility(
            visible = !uiState.showBookingSheet,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 92.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            BottomHudCard(
                nearbyCount = uiState.nearbyDrivers.size,
                onSearchTap = { viewModel.showBookingSheet(true) },
                onQuickDestination = { destino -> viewModel.setDestino(destino) }
            )
        }

        // ── FAB: AI Chat (Olivi) ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = !uiState.showBookingSheet,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 220.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FloatingActionButton(
                onClick = onNavigateToChat,
                containerColor = OltviColors.Action,
                contentColor = OltviColors.White,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(12.dp, CircleShape)
            ) {
                Icon(Icons.Filled.Chat, contentDescription = "Chat Olivi", modifier = Modifier.size(26.dp))
            }
        }

        // ── BOOKING SHEET (modal bottom sheet) ──────────────────────────────
        if (uiState.showBookingSheet) {
            BookingSheet(
                uiState = uiState,
                onDismiss = { viewModel.dismissBookingSheet() },
                onDestinoSelected = { destino -> viewModel.setDestino(destino) },
                onConfirm = {
                    viewModel.confirmRide()
                    onNavigateToTracking()
                }
            )
        }
    }
}

// ── Bottom HUD Card ───────────────────────────────────────────────────────────

@Composable
private fun BottomHudCard(
    nearbyCount: Int,
    onSearchTap: () -> Unit,
    onQuickDestination: (PuntoGeo) -> Unit
) {
    val quickDestinations = listOf(
        Triple("Trabajo", "Av. Corrientes 1234", PuntoGeo(-34.5960, -58.3904, "Trabajo")),
        Triple("Casa", "Av. de Mayo 567", PuntoGeo(-34.6037, -58.3816, "Casa")),
        Triple("Aeropuerto", "Aeroparque", PuntoGeo(-34.5592, -58.4156, "Aeropuerto"))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(OltviColors.Surface.copy(alpha = 0.97f), OltviColors.PrincipalDark)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(OltviColors.GlassBorder, Color.Transparent, OltviColors.GlassBorder)),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 3.dp)
                .background(OltviColors.Divider, RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nearby drivers indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(OltviColors.Success, CircleShape)
            )
            Text(
                text = "$nearbyCount conductores disponibles cerca",
                style = OltviTypography.etiqueta,
                color = OltviColors.Success,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main search CTA
        OltviButton(
            text = "Solicitar viaje",
            onClick = onSearchTap,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Destinos frecuentes",
            style = OltviTypography.etiqueta,
            color = OltviColors.OnSurfaceDim
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick destination chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickDestinations.forEach { (label, subtitle, destino) ->
                QuickDestinationChip(
                    label = label,
                    subtitle = subtitle,
                    onClick = { onQuickDestination(destino) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickDestinationChip(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(OltviColors.SurfaceVariant)
            .border(1.dp, OltviColors.Divider, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(label, style = OltviTypography.hud.copy(fontSize = 12.sp), color = OltviColors.OnSurface)
        Text(subtitle, style = OltviTypography.etiqueta, color = OltviColors.OnSurfaceDim, maxLines = 1)
    }
}

@Composable
private fun MapIconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(OltviColors.Surface.copy(alpha = 0.92f))
            .border(1.dp, OltviColors.GlassBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
