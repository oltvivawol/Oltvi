package com.oltvi.neural.ui.world

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.oltvi.neural.data.AvatarConfig
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.NeuralSeedData
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import com.oltvi.neural.data.ZonaBarrio
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import com.oltvi.neural.ui.avatar.AvatarView
import com.oltvi.neural.ui.components.XpProgressBar
import kotlinx.coroutines.delay

@Composable
fun WorldScreen(
    perfil: PerfilNeural = PerfilNeural(
        id = "demo",
        nombre = "Jugador",
        clase = ClaseRPG.EXPLORADOR,
        objetivo = ObjetivoVida.TRABAJO
    ),
    onNavigateToProfile: () -> Unit,
    onNavigateToMisiones: () -> Unit,
    onNavigateToGuia: () -> Unit,
    onNavigateToAvatar: () -> Unit = {},
    onNavigateToTienda: () -> Unit = {},
    onNavigateToWorld3D: () -> Unit = {}
) {
    val nc = LocalNeuralColors.current
    val zonas = remember { NeuralSeedData.zonasBarrio() }
    var selectedZona by remember { mutableStateOf<ZonaBarrio?>(null) }
    var hudVisible by remember { mutableStateOf(false) }

    // Player starts roughly at Buenos Aires center
    val playerPosition = remember { LatLng(-34.6037, -58.3816) }

    LaunchedEffect(Unit) {
        delay(500)
        hudVisible = true
    }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(playerPosition, 13f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── MAP ──────────────────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = false
            )
        ) {
            // Zone circles
            zonas.forEach { zona ->
                val zoneColor = Color(android.graphics.Color.parseColor(zona.colorHex))
                Circle(
                    center = zona.centro,
                    radius = zona.radio,
                    fillColor = zoneColor.copy(alpha = 0.1f),
                    strokeColor = zoneColor.copy(alpha = 0.45f),
                    strokeWidth = 2.5f,
                    onClick = { selectedZona = zona }
                )
            }

            // Avatar marker — the player's character on the map
            MarkerComposable(
                state = rememberMarkerState(position = playerPosition),
                title = perfil.nombre,
                snippet = "${perfil.clase.displayName} · Nv.${perfil.numeroNivel}"
            ) {
                AvatarMapMarker(perfil = perfil)
            }
        }

        // ── Season particle overlay (passes touches through to map) ─────────
        WorldSeasonOverlay()

        // ── TOP HUD ──────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = hudVisible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopHud(
                perfil = perfil,
                onTapAvatar = onNavigateToProfile
            )
        }

        // ── ZONE INFO PANEL ──────────────────────────────────────────────────
        selectedZona?.let { zona ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            ) {
                ZonaInfoPanel(
                    zona = zona,
                    onVerMisiones = { selectedZona = null; onNavigateToMisiones() },
                    onDismiss = { selectedZona = null }
                )
            }
        }

        // ── BOTTOM NAV ───────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = hudVisible && selectedZona == null,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            BottomHUD(
                misionesActivas = zonas.sumOf { it.misionesActivas },
                onMisiones = onNavigateToMisiones,
                onGuia = onNavigateToGuia,
                onAvatar = onNavigateToAvatar,
                onPerfil = onNavigateToProfile,
                onTienda = onNavigateToTienda,
                onWorld3D = onNavigateToWorld3D
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Avatar as map marker
// ---------------------------------------------------------------------------

@Composable
private fun AvatarMapMarker(perfil: PerfilNeural) {
    val nc = LocalNeuralColors.current
    val claseColor = Color(android.graphics.Color.parseColor(perfil.clase.colorHex))
    val infinite = rememberInfiniteTransition(label = "marker_bob")
    val bob by infinite.animateFloat(
        initialValue = 0f, targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        // Name tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(nc.deep.copy(0.88f))
                .border(1.dp, claseColor.copy(0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                perfil.nombre,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
        Spacer(Modifier.height(2.dp))

        // Avatar floating with bob
        Box(modifier = Modifier.padding(bottom = (-bob).dp.coerceAtLeast(0.dp))) {
            AvatarView(
                config = perfil.avatar,
                clase = perfil.clase,
                size = 72.dp,
                animated = false,
                showGlow = true
            )
        }

        // Pin anchor
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(claseColor)
        )
    }
}

// ---------------------------------------------------------------------------
// HUD components
// ---------------------------------------------------------------------------

@Composable
private fun TopHud(perfil: PerfilNeural, onTapAvatar: () -> Unit) {
    val nc = LocalNeuralColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(nc.deep.copy(0.88f))
                .border(1.dp, nc.glassBorder, RoundedCornerShape(16.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini avatar tap → profile
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onTapAvatar() }
            ) {
                AvatarView(
                    config = perfil.avatar,
                    clase = perfil.clase,
                    size = 48.dp,
                    animated = true,
                    showGlow = false
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    perfil.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    color = nc.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                XpProgressBar(
                    xpActual = perfil.xpActual,
                    xpSiguiente = perfil.xpParaSiguienteNivel,
                    progreso = perfil.progresoNivel,
                    nivel = perfil.nivel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ZonaInfoPanel(
    zona: ZonaBarrio,
    onVerMisiones: () -> Unit,
    onDismiss: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val zoneColor = Color(android.graphics.Color.parseColor(zona.colorHex))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(nc.deep.copy(0.95f))
            .border(1.dp, zoneColor.copy(0.5f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(zoneColor))
                    Spacer(Modifier.width(8.dp))
                    Text(zona.nombre, style = MaterialTheme.typography.titleLarge, color = nc.textPrimary, fontWeight = FontWeight.Bold)
                }
                Text("✕", color = nc.textSecondary, fontSize = 18.sp, modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(zoneColor.copy(0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("⚡", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text("${zona.misionesActivas} misiones activas en este barrio", style = MaterialTheme.typography.bodyMedium, color = zoneColor)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(zoneColor.copy(0.15f))
                    .border(1.dp, zoneColor.copy(0.5f), RoundedCornerShape(12.dp))
                    .clickable { onVerMisiones() },
                contentAlignment = Alignment.Center
            ) {
                Text("Ver misiones del barrio", style = MaterialTheme.typography.titleSmall, color = zoneColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BottomHUD(
    misionesActivas: Int,
    onMisiones: () -> Unit,
    onGuia: () -> Unit,
    onAvatar: () -> Unit,
    onPerfil: () -> Unit,
    onTienda: () -> Unit = {},
    onWorld3D: () -> Unit = {}
) {
    val nc = LocalNeuralColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(nc.deep.copy(0.92f))
            .border(1.dp, nc.glassBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HudNavItem("Misiones", "⚡", misionesActivas.toString(), NeuralColors.xpGold, onMisiones)
            HudNavItem("Tienda", "🛍️", "", NeuralColors.neural, onTienda)
            GuiaButton(onClick = onGuia)
            World3DButton(onClick = onWorld3D)
            HudNavItem("Perfil", "📊", "", NeuralColors.electric, onPerfil)
        }
    }
}

@Composable
private fun HudNavItem(
    label: String,
    icon: String,
    badge: String,
    color: Color,
    onClick: () -> Unit
) {
    val nc = LocalNeuralColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Text(icon, fontSize = 22.sp)
            if (badge.isNotEmpty() && badge != "0") {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badge, fontSize = 8.sp, color = nc.deep, fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = nc.textSecondary)
    }
}

@Composable
private fun GuiaButton(onClick: () -> Unit) {
    val nc = LocalNeuralColors.current
    val infinite = rememberInfiniteTransition(label = "guia_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "guia_scale"
    )
    Box(
        modifier = Modifier
            .size(58.dp)
            .scale(pulse)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(nc.neural, nc.neural.copy(0.7f))))
            .border(2.dp, nc.electric.copy(0.6f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("🧠", fontSize = 26.sp)
    }
}

@Composable
private fun World3DButton(onClick: () -> Unit) {
    val nc = LocalNeuralColors.current
    val infinite = rememberInfiniteTransition(label = "3d_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "3d_scale"
    )
    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(pulse)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(NeuralColors.xpGold, NeuralColors.xpGold.copy(0.6f))))
            .border(2.dp, Color.White.copy(0.3f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("🕹️", fontSize = 22.sp)
    }
}
