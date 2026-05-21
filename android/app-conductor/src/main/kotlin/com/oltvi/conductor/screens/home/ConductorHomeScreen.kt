package com.oltvi.conductor.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.oltvi.core.data.models.Servicio
import com.oltvi.core.data.models.SugerenciaCopiloto
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.PulseRing
import com.oltvi.core.ui.effects.neonGlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// ── ViewModel ─────────────────────────────────────────────────────────────

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oltvi.core.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ConductorHomeViewModel @Inject constructor() : ViewModel() {

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _solicitudEntrante = MutableStateFlow<Servicio?>(null)
    val solicitudEntrante: StateFlow<Servicio?> = _solicitudEntrante.asStateFlow()

    private val _sugerenciaCopiloto = MutableStateFlow<SugerenciaCopiloto?>(null)
    val sugerenciaCopiloto: StateFlow<SugerenciaCopiloto?> = _sugerenciaCopiloto.asStateFlow()

    private val _gananciasHoy = MutableStateFlow(12_450.0)
    val gananciasHoy: StateFlow<Double> = _gananciasHoy.asStateFlow()

    private val _viajesHoy = MutableStateFlow(8)
    val viajesHoy: StateFlow<Int> = _viajesHoy.asStateFlow()

    private val _rating = MutableStateFlow(4.87)
    val rating: StateFlow<Double> = _rating.asStateFlow()

    private val _showHeatOverlay = MutableStateFlow(false)
    val showHeatOverlay: StateFlow<Boolean> = _showHeatOverlay.asStateFlow()

    private val mockSolicitud = Servicio(
        id = "srv-mock-001",
        tipo = TipoServicio.PASAJERO,
        nivel = NivelServicio.PRIORITARIO,
        origen = PuntoGeo(-34.5960, -58.3745, "Centro Comercial Palermo", "Av. Santa Fe 3253, Palermo"),
        destino = PuntoGeo(-34.8218, -58.5356, "Aeropuerto Ezeiza", "Autopista Ezeiza-Cañuelas km 33"),
        idCliente = "usr-001",
        precio = 8_200.0,
        distanciaKm = 42.3,
        tiempoEstimadoMin = 38
    )

    private val mockSugerencia = SugerenciaCopiloto(
        titulo = "Zona caliente detectada",
        descripcion = "Alta demanda en Palermo Hollywood — 8 solicitudes sin conductor en los últimos 5 min",
        zonaRecomendada = PuntoGeo(-34.5886, -58.4362, "Palermo Hollywood"),
        potencialGanancia = 4_500.0,
        urgencia = "alta"
    )

    fun toggleOnline() {
        val newState = !_isOnline.value
        _isOnline.value = newState
        if (newState) {
            _sugerenciaCopiloto.value = mockSugerencia
            // Simulate incoming request after 5 seconds
            viewModelScope.launch {
                delay(5_000L)
                if (_isOnline.value) {
                    _solicitudEntrante.value = mockSolicitud
                }
            }
        } else {
            _solicitudEntrante.value = null
            _sugerenciaCopiloto.value = null
        }
    }

    fun aceptarViaje() {
        _solicitudEntrante.value = null
        _gananciasHoy.value += mockSolicitud.precio
        _viajesHoy.value += 1
    }

    fun rechazarViaje() {
        _solicitudEntrante.value = null
    }

    fun toggleHeatOverlay() {
        _showHeatOverlay.value = !_showHeatOverlay.value
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────

@Composable
fun ConductorHomeScreen(
    viewModel: ConductorHomeViewModel = hiltViewModel()
) {
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val solicitudEntrante by viewModel.solicitudEntrante.collectAsStateWithLifecycle()
    val sugerenciaCopiloto by viewModel.sugerenciaCopiloto.collectAsStateWithLifecycle()
    val gananciasHoy by viewModel.gananciasHoy.collectAsStateWithLifecycle()
    val viajesHoy by viewModel.viajesHoy.collectAsStateWithLifecycle()
    val rating by viewModel.rating.collectAsStateWithLifecycle()
    val showHeatOverlay by viewModel.showHeatOverlay.collectAsStateWithLifecycle()

    val buenosAires = LatLng(-34.6037, -58.3816)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(buenosAires)
            .zoom(14f)
            .tilt(45f)
            .bearing(30f)
            .build()
    }

    val scope = rememberCoroutineScope()

    // Offline overlay alpha
    val offlineAlpha by animateFloatAsState(
        targetValue = if (isOnline) 0f else 0.55f,
        animationSpec = tween(800),
        label = "offlineOverlay"
    )

    // Online glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "onlineGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glow"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Full-screen Map ──────────────────────────────────────────────
        val mapProperties = remember(isOnline) {
            MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = false
            )
        }
        val mapUiSettings = remember {
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false,
                mapToolbarEnabled = false
            )
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // Driver position marker
            Marker(
                state = MarkerState(position = buenosAires),
                title = "Tu posición",
                snippet = if (isOnline) "En línea" else "Fuera de línea"
            )

            // Heat overlay zones (mock circles)
            if (showHeatOverlay) {
                Circle(
                    center = LatLng(-34.5886, -58.4362),
                    radius = 600.0,
                    fillColor = Color(0x44E67E22),
                    strokeColor = Color(0xAAE67E22),
                    strokeWidth = 2f
                )
                Circle(
                    center = LatLng(-34.6083, -58.3712),
                    radius = 400.0,
                    fillColor = Color(0x44F1C40F),
                    strokeColor = Color(0xAAF1C40F),
                    strokeWidth = 2f
                )
                Circle(
                    center = LatLng(-34.6180, -58.3730),
                    radius = 500.0,
                    fillColor = Color(0x44E74C3C),
                    strokeColor = Color(0xAAE74C3C),
                    strokeWidth = 2f
                )
            }
        }

        // ── Offline desaturation overlay ─────────────────────────────────
        if (offlineAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OltviColors.PrincipalDeep.copy(alpha = offlineAlpha))
            )
        }

        // ── Top Bar: Online toggle + HUD stats ───────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Online/Offline toggle card
                OnlineToggleCard(
                    isOnline = isOnline,
                    glowAlpha = glowAlpha,
                    onToggle = { viewModel.toggleOnline() }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Heat overlay toggle
                    HudIconButton(
                        icon = Icons.Filled.Layers,
                        isActive = showHeatOverlay,
                        onClick = { viewModel.toggleHeatOverlay() }
                    )
                    // Center on location
                    HudIconButton(
                        icon = Icons.Filled.MyLocation,
                        isActive = false,
                        onClick = {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.Builder()
                                            .target(buenosAires)
                                            .zoom(15f)
                                            .tilt(45f)
                                            .build()
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }

        // ── Left side stats panel ────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .width(80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HudStatCard(
                label = "HOY",
                value = formatMoney(gananciasHoy),
                icon = Icons.Filled.AttachMoney,
                color = OltviColors.Success
            )
            HudStatCard(
                label = "VIAJES",
                value = "$viajesHoy",
                icon = Icons.Filled.DirectionsCar,
                color = OltviColors.Action
            )
            HudStatCard(
                label = "RATING",
                value = String.format("%.1f", rating),
                icon = Icons.Filled.Star,
                color = OltviColors.Warning
            )
        }

        // ── Bottom content: Sugerencia + Solicitud ───────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Copilot suggestion (only when online and no incoming request)
            AnimatedVisibility(
                visible = isOnline && sugerenciaCopiloto != null && solicitudEntrante == null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                sugerenciaCopiloto?.let { sug ->
                    CopilotoSugerenciaCard(
                        sugerencia = sug,
                        onIrAZona = {
                            sug.zonaRecomendada?.let { zona ->
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(zona.latitud, zona.longitud),
                                            15f
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Incoming request card
            AnimatedVisibility(
                visible = solicitudEntrante != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                solicitudEntrante?.let { servicio ->
                    SolicitudEntranteCard(
                        servicio = servicio,
                        onAceptar = { viewModel.aceptarViaje() },
                        onRechazar = { viewModel.rechazarViaje() }
                    )
                }
            }

            // Offline message
            AnimatedVisibility(
                visible = !isOnline,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.PowerOff,
                            null,
                            tint = OltviColors.OnSurfaceDim,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Fuera de línea — Activá el interruptor para recibir viajes",
                            style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
                        )
                    }
                }
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────

@Composable
private fun OnlineToggleCard(
    isOnline: Boolean,
    glowAlpha: Float,
    onToggle: () -> Unit
) {
    val glowColor = if (isOnline) OltviColors.Success.copy(alpha = glowAlpha * 0.6f) else Color.Transparent
    val bgColor = if (isOnline) OltviColors.Success.copy(0.15f) else OltviColors.SurfaceVariant.copy(0.9f)
    val borderColor = if (isOnline) OltviColors.Success.copy(alpha = glowAlpha) else OltviColors.Divider

    Box(
        modifier = Modifier
            .neonGlow(glowColor, 20f)
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (isOnline) OltviColors.Success else OltviColors.OnSurfaceDim,
                        CircleShape
                    )
            )
            Text(
                text = if (isOnline) "EN LINEA" else "FUERA DE LINEA",
                style = OltviTypography.hud.copy(
                    color = if (isOnline) OltviColors.Success else OltviColors.OnSurfaceDim,
                    fontSize = 12.sp
                )
            )
            Switch(
                checked = isOnline,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = OltviColors.White,
                    checkedTrackColor = OltviColors.Success,
                    uncheckedThumbColor = OltviColors.OnSurfaceDim,
                    uncheckedTrackColor = OltviColors.SurfaceVariant
                ),
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
private fun HudIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isActive) OltviColors.Action.copy(0.2f) else OltviColors.Surface.copy(0.9f)
            )
            .border(
                1.dp,
                if (isActive) OltviColors.Action.copy(0.6f) else OltviColors.Divider,
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            null,
            tint = if (isActive) OltviColors.Action else OltviColors.OnSurfaceDim,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun HudStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    GlassCard {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = OltviTypography.pequeno.copy(
                    color = OltviColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
            Text(
                text = label,
                style = OltviTypography.etiqueta.copy(
                    color = OltviColors.OnSurfaceDim,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun CopilotoSugerenciaCard(
    sugerencia: SugerenciaCopiloto,
    onIrAZona: () -> Unit
) {
    val urgencyColor = when (sugerencia.urgencia) {
        "alta", "critica" -> OltviColors.Error
        "media" -> OltviColors.Warning
        else -> OltviColors.Action
    }

    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Psychology,
                        null,
                        tint = OltviColors.Action,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "AGENTE COPILOTO",
                        style = OltviTypography.etiqueta.copy(
                            color = OltviColors.Action,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .background(urgencyColor.copy(0.2f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        sugerencia.urgencia.uppercase(),
                        style = OltviTypography.etiqueta.copy(color = urgencyColor, fontSize = 9.sp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                sugerencia.titulo,
                style = OltviTypography.subtitulo.copy(
                    color = OltviColors.OnSurface,
                    fontSize = 15.sp
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                sugerencia.descripcion,
                style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
            )

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.TrendingUp,
                        null,
                        tint = OltviColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Potencial: ${formatMoney(sugerencia.potencialGanancia)}",
                        style = OltviTypography.cuerpo.copy(
                            color = OltviColors.Success,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Button(
                    onClick = onIrAZona,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OltviColors.Action
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Navigation, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ir a la zona", style = OltviTypography.etiqueta.copy(color = OltviColors.White))
                }
            }
        }
    }
}

@Composable
private fun SolicitudEntranteCard(
    servicio: Servicio,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(15) }

    LaunchedEffect(servicio.id) {
        countdown = 15
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000L)
            countdown--
        }
        // Auto-reject when timer expires
        onRechazar()
    }

    val countdownFraction = countdown / 15f

    val infiniteTransition = rememberInfiniteTransition(label = "solicitudPulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "borderPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(OltviColors.Surface)
            .border(
                width = 2.dp,
                color = OltviColors.Action.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "NUEVO VIAJE",
                    style = OltviTypography.hud.copy(
                        color = OltviColors.Action,
                        fontWeight = FontWeight.Black
                    )
                )
                // Countdown ring
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { countdownFraction },
                        modifier = Modifier.size(44.dp),
                        color = when {
                            countdown > 10 -> OltviColors.Success
                            countdown > 5 -> OltviColors.Warning
                            else -> OltviColors.Error
                        },
                        strokeWidth = 3.dp,
                        trackColor = OltviColors.Divider
                    )
                    Text(
                        "$countdown",
                        style = OltviTypography.subtitulo.copy(
                            color = OltviColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(OltviColors.Action, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(28.dp)
                            .background(OltviColors.Divider)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(OltviColors.Success, CircleShape)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        servicio.origen.nombre,
                        style = OltviTypography.cuerpo.copy(
                            color = OltviColors.OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        servicio.origen.direccion,
                        style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        servicio.destino.nombre,
                        style = OltviTypography.cuerpo.copy(
                            color = OltviColors.OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        servicio.destino.direccion,
                        style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = OltviColors.Divider)
            Spacer(Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatPill(
                    icon = Icons.Filled.Route,
                    value = "${servicio.distanciaKm} km",
                    color = OltviColors.Action
                )
                StatPill(
                    icon = Icons.Filled.Schedule,
                    value = "${servicio.tiempoEstimadoMin} min",
                    color = OltviColors.ActionLight
                )
                StatPill(
                    icon = Icons.Filled.Payments,
                    value = formatMoney(servicio.precio),
                    color = OltviColors.Success
                )
            }

            Spacer(Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRechazar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OltviColors.Error.copy(0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        null,
                        tint = OltviColors.Error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "RECHAZAR",
                        style = OltviTypography.hud.copy(color = OltviColors.Error)
                    )
                }
                Button(
                    onClick = onAceptar,
                    modifier = Modifier
                        .weight(1f)
                        .neonGlow(OltviColors.Success, 12f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OltviColors.Success
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        null,
                        tint = OltviColors.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "ACEPTAR",
                        style = OltviTypography.hud.copy(color = OltviColors.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .background(color.copy(0.12f), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(value, style = OltviTypography.pequeno.copy(color = color, fontWeight = FontWeight.Bold))
    }
}

private fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "AR"))
    formatter.maximumFractionDigits = 0
    return "$${formatter.format(amount)}"
}
