package com.oltvi.conductor.screens.operacion

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.oltvi.core.data.models.*
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.neonGlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// ── ViewModel ──────────────────────────────────────────────────────────────

enum class EstadoConductorViaje(val label: String, val buttonLabel: String, val icon: ImageVector) {
    EN_CAMINO("Ir al origen", "Pasajero recogido", Icons.Filled.DirectionsCar),
    EN_RUTA("En ruta al destino", "Completar viaje", Icons.Filled.Route),
    ENTREGADO("Viaje completado", "Nuevo viaje", Icons.Filled.CheckCircle)
}

@HiltViewModel
class OperacionViewModel @Inject constructor() : ViewModel() {

    private val _estado = MutableStateFlow(EstadoConductorViaje.EN_CAMINO)
    val estado: StateFlow<EstadoConductorViaje> = _estado.asStateFlow()

    private val mockPasajero = Usuario(
        id = "usr-mock-002",
        perfil = TipoPerfil.CLIENTE,
        nombre = "Valentina Morales",
        correo = "vale@email.com",
        telefono = "+54 9 11 5555-4321",
        rating = 4.6,
        nivel = NivelUsuario.COLABORADOR
    )

    private val mockServicio = Servicio(
        id = "srv-mock-002",
        tipo = TipoServicio.PASAJERO,
        nivel = NivelServicio.PRIORITARIO,
        origen = PuntoGeo(-34.5960, -58.3745, "Centro Comercial Palermo", "Av. Santa Fe 3253, Palermo"),
        destino = PuntoGeo(-34.8218, -58.5356, "Aeropuerto Ezeiza", "Autopista Ezeiza-Cañuelas km 33"),
        idCliente = "usr-mock-002",
        precio = 8_200.0,
        distanciaKm = 42.3,
        tiempoEstimadoMin = 38,
        estado = EstadoServicio.EN_CAMINO
    )

    val pasajero: Usuario = mockPasajero
    val servicio: Servicio = mockServicio

    fun avanzarEstado() {
        _estado.value = when (_estado.value) {
            EstadoConductorViaje.EN_CAMINO -> EstadoConductorViaje.EN_RUTA
            EstadoConductorViaje.EN_RUTA -> EstadoConductorViaje.ENTREGADO
            EstadoConductorViaje.ENTREGADO -> EstadoConductorViaje.EN_CAMINO // reset to simulate new trip
        }
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────

@Composable
fun OperacionScreen(
    viewModel: OperacionViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val origenLatLng = LatLng(-34.5960, -58.3745)
    val destinoLatLng = LatLng(-34.8218, -58.5356)
    val conductorLatLng = LatLng(-34.6037, -58.3816)

    val mapTarget = when (estado) {
        EstadoConductorViaje.EN_CAMINO -> origenLatLng
        EstadoConductorViaje.EN_RUTA -> destinoLatLng
        EstadoConductorViaje.ENTREGADO -> destinoLatLng
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(conductorLatLng, 13f)
    }

    LaunchedEffect(mapTarget) {
        cameraPositionState.animate(
            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(mapTarget, 14f)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            Marker(state = MarkerState(conductorLatLng), title = "Tu posición")
            Marker(
                state = MarkerState(origenLatLng),
                title = viewModel.servicio.origen.nombre,
                snippet = "Origen"
            )
            Marker(
                state = MarkerState(destinoLatLng),
                title = viewModel.servicio.destino.nombre,
                snippet = "Destino"
            )
            Polyline(
                points = listOf(conductorLatLng, origenLatLng, destinoLatLng),
                color = OltviColors.Action,
                width = 6f
            )
        }

        // Emergency SOS button (top right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .size(48.dp)
                .neonGlow(OltviColors.Error, 12f)
                .clip(CircleShape)
                .background(OltviColors.Error)
                .clickable {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                    context.startActivity(intent)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "SOS",
                style = OltviTypography.etiqueta.copy(
                    color = OltviColors.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            )
        }

        // Bottom panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, OltviColors.PrincipalDeep)
                    )
                )
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Step indicator
            EstadoStepIndicator(estadoActual = estado)

            // Passenger card
            PasajeroCard(
                pasajero = viewModel.pasajero,
                servicio = viewModel.servicio,
                onCall = {
                    val intent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${viewModel.pasajero.telefono}")
                    )
                    context.startActivity(intent)
                },
                onChat = { /* navigate to chat */ }
            )

            // Navigation button
            Button(
                onClick = {
                    val target = if (estado == EstadoConductorViaje.EN_CAMINO) origenLatLng else destinoLatLng
                    val uri = Uri.parse("google.navigation:q=${target.latitude},${target.longitude}")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallback = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(fallback)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = OltviColors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Navigation, null, tint = OltviColors.Action)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Abrir Google Maps",
                    style = OltviTypography.hud.copy(color = OltviColors.OnSurface)
                )
            }

            // Advance state button
            if (estado != EstadoConductorViaje.ENTREGADO) {
                Button(
                    onClick = { viewModel.avanzarEstado() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .neonGlow(OltviColors.Action, 12f),
                    colors = ButtonDefaults.buttonColors(containerColor = OltviColors.Action),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(estado.icon, null, tint = OltviColors.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        estado.buttonLabel.uppercase(),
                        style = OltviTypography.hud.copy(color = OltviColors.White, fontWeight = FontWeight.Black)
                    )
                }
            } else {
                // Completion card
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = OltviColors.Success,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Viaje completado",
                                style = OltviTypography.subtitulo.copy(color = OltviColors.Success)
                            )
                            Text(
                                "+$8,200 acreditados",
                                style = OltviTypography.cuerpo.copy(color = OltviColors.OnSurfaceDim)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoStepIndicator(estadoActual: EstadoConductorViaje) {
    val steps = listOf(
        Pair(Icons.Filled.DirectionsCar, "Ir al origen"),
        Pair(Icons.Filled.PersonPin, "Recoger pasajero"),
        Pair(Icons.Filled.Route, "En ruta"),
        Pair(Icons.Filled.CheckCircle, "Completado")
    )

    val currentIndex = when (estadoActual) {
        EstadoConductorViaje.EN_CAMINO -> 0
        EstadoConductorViaje.EN_RUTA -> 2
        EstadoConductorViaje.ENTREGADO -> 3
    }

    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (icon, label) ->
                val isActive = index == currentIndex
                val isDone = index < currentIndex
                val color = when {
                    isActive -> OltviColors.Action
                    isDone -> OltviColors.Success
                    else -> OltviColors.OnSurfaceDim.copy(0.4f)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(70.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color.copy(0.15f), CircleShape)
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = color,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isDone) Icons.Filled.Check else icon,
                            null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        style = OltviTypography.etiqueta.copy(
                            color = color,
                            fontSize = 9.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(1.dp)
                            .background(
                                if (index < currentIndex) OltviColors.Success
                                else OltviColors.Divider
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PasajeroCard(
    pasajero: Usuario,
    servicio: Servicio,
    onCall: () -> Unit,
    onChat: () -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(OltviColors.Action, OltviColors.ActionDim)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    pasajero.inicial,
                    style = OltviTypography.titulo.copy(color = OltviColors.White, fontWeight = FontWeight.Black)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pasajero.nombre,
                    style = OltviTypography.cuerpo.copy(
                        color = OltviColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        null,
                        tint = OltviColors.Warning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        String.format("%.1f", pasajero.rating),
                        style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    servicio.origen.direccion,
                    style = OltviTypography.etiqueta.copy(color = OltviColors.OnSurfaceDim),
                    maxLines = 1
                )
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onChat,
                    modifier = Modifier
                        .size(40.dp)
                        .background(OltviColors.SurfaceVariant, CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Chat,
                        null,
                        tint = OltviColors.Action,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(40.dp)
                        .background(OltviColors.Success.copy(0.15f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Call,
                        null,
                        tint = OltviColors.Success,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
