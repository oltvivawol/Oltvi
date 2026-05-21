package com.oltvi.conductor.screens.operacion

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.ConfettiExplosion
import com.oltvi.core.ui.components.OltviButton
import androidx.compose.ui.geometry.Offset

enum class FaseViaje { EN_CAMINO_ORIGEN, ESPERANDO_PASAJERO, EN_RUTA, COMPLETADO }

private val DARK_MAP_STYLE = """
[{"elementType":"geometry","stylers":[{"color":"#0d1a26"}]},
{"elementType":"labels.text.fill","stylers":[{"color":"#6b8299"}]},
{"featureType":"road","elementType":"geometry","stylers":[{"color":"#1a2b3c"}]},
{"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#e67e22"},{"weight":0.5}]},
{"featureType":"water","elementType":"geometry","stylers":[{"color":"#091522"}]},
{"featureType":"poi","stylers":[{"visibility":"off"}]}]
"""

@Composable
fun OperacionScreen(
    servicioId: String = "demo",
    onTripCompleted: () -> Unit = {},
) {
    val context = LocalContext.current
    var fase by remember { mutableStateOf(FaseViaje.EN_CAMINO_ORIGEN) }
    var showConfetti by remember { mutableStateOf(false) }

    val origenLatLng = LatLng(-34.6037, -58.3816)
    val destinoLatLng = LatLng(-34.5950, -58.3700)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder().target(origenLatLng).zoom(14f).tilt(30f).build()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapStyleOptions = com.google.android.gms.maps.model.MapStyleOptions(DARK_MAP_STYLE)
            ),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
        ) {
            Marker(state = MarkerState(origenLatLng), title = "Pasajero")
            Marker(state = MarkerState(destinoLatLng), title = "Destino")
            Polyline(
                points = listOf(origenLatLng, LatLng(-34.5990, -58.3760), destinoLatLng),
                color = Color(0xFFE67E22),
                width = 8f,
            )
        }

        // Phase indicator
        PhaseIndicator(fase = fase, modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(16.dp))

        // Bottom action card
        GlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(OltviColors.action),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("José Ramírez", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, null, tint = Color(0xFFF39C12), modifier = Modifier.size(14.dp))
                            Text(" 4.7 • ${fase.displayLabel}", color = Color.White.copy(0.7f), fontSize = 13.sp)
                        }
                    }
                }

                AnimatedContent(targetState = fase, label = "action") { f ->
                    OltviButton(
                        text = f.buttonLabel,
                        onClick = {
                            when (f) {
                                FaseViaje.EN_CAMINO_ORIGEN -> fase = FaseViaje.ESPERANDO_PASAJERO
                                FaseViaje.ESPERANDO_PASAJERO -> fase = FaseViaje.EN_RUTA
                                FaseViaje.EN_RUTA -> {
                                    showConfetti = true
                                    fase = FaseViaje.COMPLETADO
                                }
                                FaseViaje.COMPLETADO -> onTripCompleted()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("google.navigation:q=${destinoLatLng.latitude},${destinoLatLng.longitude}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF27AE60)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27AE60))
                    ) {
                        Icon(Icons.Filled.Navigation, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Navegar", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE74C3C)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE74C3C))
                    ) {
                        Icon(Icons.Filled.Warning, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("SOS", fontSize = 12.sp)
                    }
                }
            }
        }

        if (showConfetti) {
            ConfettiExplosion(
                modifier = Modifier.fillMaxSize(),
                center = Offset(400f, 800f),
                onDone = { showConfetti = false }
            )
        }
    }
}

@Composable
private fun PhaseIndicator(fase: FaseViaje, modifier: Modifier = Modifier) {
    val fases = FaseViaje.values()
    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            fases.forEachIndexed { idx, f ->
                val isReached = f.ordinal <= fase.ordinal
                val icon = when (f) {
                    FaseViaje.EN_CAMINO_ORIGEN -> Icons.Filled.DirectionsCar
                    FaseViaje.ESPERANDO_PASAJERO -> Icons.Filled.Person
                    FaseViaje.EN_RUTA -> Icons.Filled.Route
                    FaseViaje.COMPLETADO -> Icons.Filled.CheckCircle
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isReached) OltviColors.action else Color.White.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                if (idx < fases.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (f.ordinal < fase.ordinal) OltviColors.action else Color.White.copy(0.15f))
                    )
                }
            }
        }
    }
}

private val FaseViaje.buttonLabel get() = when (this) {
    FaseViaje.EN_CAMINO_ORIGEN -> "Llegué al origen"
    FaseViaje.ESPERANDO_PASAJERO -> "Iniciar viaje"
    FaseViaje.EN_RUTA -> "Completar viaje"
    FaseViaje.COMPLETADO -> "Volver al inicio"
}

private val FaseViaje.displayLabel get() = when (this) {
    FaseViaje.EN_CAMINO_ORIGEN -> "En camino al origen"
    FaseViaje.ESPERANDO_PASAJERO -> "Esperando al pasajero"
    FaseViaje.EN_RUTA -> "En ruta al destino"
    FaseViaje.COMPLETADO -> "Viaje completado"
}
