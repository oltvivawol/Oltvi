package com.oltvi.usuario.screens.profile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.data.models.NivelUsuario
import com.oltvi.core.data.services.MockDataService
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.neonGlow

private fun parseHexColor(hex: String): Color {
    return try {
        val cleaned = hex.trimStart('#')
        val value = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color(0xFF000000 or value)
            8 -> Color(value)
            else -> OltviColors.Surface
        }
    } catch (e: Exception) {
        OltviColors.Surface
    }
}

private data class SettingsItem(
    val icon: ImageVector,
    val label: String,
    val iconColor: Color
)

@Composable
fun ProfileScreen() {
    val mockService = remember { MockDataService() }
    val user = remember { mockService.getMockUser() }

    // Animate progress bar
    var progressVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { progressVisible = true }

    val allLevels = NivelUsuario.values()
    val nextLevel = allLevels.firstOrNull { it.puntosRequeridos > user.puntos }
    val progressFraction = nextLevel?.let { next ->
        val prev = allLevels.lastOrNull { lvl -> lvl.puntosRequeridos <= user.puntos && lvl != next }
        val prevPts = prev?.puntosRequeridos?.toFloat() ?: 0f
        val span = (next.puntosRequeridos - prevPts).coerceAtLeast(1f)
        (user.puntos - prevPts) / span
    } ?: 1f

    val animatedProgress by animateFloatAsState(
        targetValue = if (progressVisible) progressFraction else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "gamificationProgress"
    )

    val levelColor = parseHexColor(user.nivel.colorHex)

    val settingsItems = remember {
        listOf(
            SettingsItem(Icons.Filled.Notifications, "Notificaciones", OltviColors.Action),
            SettingsItem(Icons.Filled.Payment, "Métodos de pago", OltviColors.Success),
            SettingsItem(Icons.Filled.Description, "Documentos", OltviColors.Warning),
            SettingsItem(Icons.Filled.SupportAgent, "Soporte", OltviColors.OnSurfaceDim),
            SettingsItem(Icons.Filled.ExitToApp, "Cerrar sesión", OltviColors.Error)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(OltviColors.PrincipalDeep, OltviColors.Surface.copy(alpha = 0.4f))
                )
            )
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Avatar + name header ──────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .neonGlow(OltviColors.Action, blurRadius = 24f)
                            .shadow(12.dp, CircleShape)
                            .background(
                                Brush.radialGradient(listOf(OltviColors.Action, OltviColors.ActionDim)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.inicial,
                            style = OltviTypography.display.copy(fontSize = 42.sp, color = OltviColors.White)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(user.nombre, style = OltviTypography.titulo, color = OltviColors.OnSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(user.correo, style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(levelColor.copy(alpha = 0.15f))
                            .border(1.dp, levelColor.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(user.nivel.displayName, style = OltviTypography.hud, color = levelColor)
                    }
                }
            }

            // ── Stats row ─────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Viajes", "247", modifier = Modifier.weight(1f))
                    StatCard("Rating", "${"%.1f".format(user.rating)}★", valueColor = OltviColors.Warning, modifier = Modifier.weight(1f))
                    StatCard("Puntos", "${user.puntos}", valueColor = levelColor, modifier = Modifier.weight(1f))
                }
            }

            // ── Gamification progress ─────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Progreso hacia ${nextLevel?.displayName ?: "nivel máximo"}",
                                    style = OltviTypography.etiqueta,
                                    color = OltviColors.OnSurfaceDim
                                )
                                Text(
                                    "${user.puntos} / ${nextLevel?.puntosRequeridos ?: user.puntos} pts",
                                    style = OltviTypography.pequeno,
                                    color = OltviColors.OnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "${(animatedProgress * 100).toInt()}%",
                                style = OltviTypography.hud,
                                color = levelColor
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = levelColor,
                            trackColor = OltviColors.Divider,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            // ── Settings header ───────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Configuración",
                    style = OltviTypography.etiqueta,
                    color = OltviColors.OnSurfaceDim,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Settings items (each as separate item) ────────────────────
            settingsItems.forEach { setting ->
                item(key = setting.label) {
                    SettingsRow(
                        icon = setting.icon,
                        label = setting.label,
                        iconColor = setting.iconColor,
                        onClick = {}
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = OltviColors.OnSurface
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = OltviTypography.subtitulo.copy(fontWeight = FontWeight.Black), color = valueColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, style = OltviTypography.etiqueta, color = OltviColors.OnSurfaceDim)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = OltviTypography.cuerpo,
                color = if (iconColor == OltviColors.Error) OltviColors.Error else OltviColors.OnSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = OltviColors.OnSurfaceDim,
                modifier = Modifier.size(20.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 68.dp)
                .height(1.dp)
                .background(OltviColors.Divider)
        )
    }
}
