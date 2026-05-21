package com.oltvi.conductor.screens.perfil

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oltvi.core.data.models.*
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.components.OltviRatingStars
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.neonGlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

// ── ViewModel ──────────────────────────────────────────────────────────────

data class VehiculoInfo(
    val marca: String,
    val modelo: String,
    val anio: Int,
    val patente: String,
    val color: String,
    val vtvVencimiento: String
)

@HiltViewModel
class PerfilConductorViewModel @Inject constructor() : ViewModel() {

    private val _conductor = MutableStateFlow(
        Usuario(
            id = "cond-001",
            perfil = TipoPerfil.CONDUCTOR,
            nombre = "Rodrigo Alejandro Fierro",
            correo = "rodrigo.fierro@oltvi.com",
            telefono = "+54 9 11 6789-0123",
            rating = 4.87,
            nivel = NivelUsuario.EXPERTO,
            puntos = 3_240,
            verificado = true
        )
    )
    val conductor: StateFlow<Usuario> = _conductor.asStateFlow()

    val vehiculo = VehiculoInfo(
        marca = "Toyota",
        modelo = "Corolla",
        anio = 2021,
        patente = "AB 456 CD",
        color = "Blanco Perla",
        vtvVencimiento = "Dic 2025"
    )

    val viajesTotal = 1_247
    val tasaAceptacion = 92
    val anosActivo = 3

    val gananciasMesActual = 248_200.0
    val gananciasMesAnterior = 219_500.0
}

// ── Screen ─────────────────────────────────────────────────────────────────

@Composable
fun PerfilConductorScreen(
    viewModel: PerfilConductorViewModel = hiltViewModel()
) {
    val conductor by viewModel.conductor.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OltviColors.PrincipalDeep),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header with avatar
        item {
            PerfilHeader(conductor = conductor)
        }

        // Stats row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.DirectionsCar,
                    value = "${viewModel.viajesTotal}",
                    label = "Viajes totales",
                    color = OltviColors.Action
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Percent,
                    value = "${viewModel.tasaAceptacion}%",
                    label = "Aceptacion",
                    color = OltviColors.Success
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.WorkHistory,
                    value = "${viewModel.anosActivo}",
                    label = "Anos activo",
                    color = OltviColors.ActionLight
                )
            }
        }

        // Earnings summary
        item {
            EarningsSummaryCard(
                mesActual = viewModel.gananciasMesActual,
                mesAnterior = viewModel.gananciasMesAnterior,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Vehicle info
        item {
            VehicleInfoCard(
                vehiculo = viewModel.vehiculo,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Documents status
        item {
            DocumentsCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        // Settings section
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "Configuracion",
                style = OltviTypography.subtitulo.copy(color = OltviColors.OnSurfaceDim),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            SettingsSection()
        }
    }
}

@Composable
private fun PerfilHeader(conductor: Usuario) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(OltviColors.Principal, OltviColors.PrincipalDeep)
                )
            )
            .padding(top = 56.dp, bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with glow
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .neonGlow(OltviColors.Action.copy(alpha = glowAlpha * 0.5f), 25f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(OltviColors.Action, OltviColors.ActionDim)
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(OltviColors.ActionLight, OltviColors.Action)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conductor.inicial,
                        style = OltviTypography.display.copy(
                            color = OltviColors.White,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                conductor.nombre,
                style = OltviTypography.titulo.copy(
                    color = OltviColors.OnSurface,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(6.dp))

            // Rating row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OltviRatingStars(rating = conductor.rating, modifier = Modifier)
                Text(
                    "${"%.2f".format(conductor.rating)}",
                    style = OltviTypography.cuerpo.copy(
                        color = OltviColors.Warning,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // Verified badge
            if (conductor.verificado) {
                Row(
                    modifier = Modifier
                        .background(OltviColors.Success.copy(0.15f), RoundedCornerShape(100.dp))
                        .border(1.dp, OltviColors.Success.copy(0.4f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Verified,
                        null,
                        tint = OltviColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Conductor Verificado",
                        style = OltviTypography.etiqueta.copy(
                            color = OltviColors.Success,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = OltviTypography.titulo.copy(
                    color = OltviColors.OnSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            )
            Text(
                label,
                style = OltviTypography.etiqueta.copy(
                    color = OltviColors.OnSurfaceDim,
                    fontSize = 10.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun EarningsSummaryCard(
    mesActual: Double,
    mesAnterior: Double,
    modifier: Modifier = Modifier
) {
    val delta = mesActual - mesAnterior
    val pct = if (mesAnterior > 0) (delta / mesAnterior) * 100 else 0.0
    val isUp = pct >= 0

    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Ganancias del mes",
                style = OltviTypography.subtitulo.copy(color = OltviColors.OnSurface)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Este mes",
                        style = OltviTypography.etiqueta.copy(color = OltviColors.OnSurfaceDim)
                    )
                    Text(
                        formatMoney(mesActual),
                        style = OltviTypography.titulo.copy(
                            color = OltviColors.OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Delta indicator
                Row(
                    modifier = Modifier
                        .background(
                            if (isUp) OltviColors.Success.copy(0.15f) else OltviColors.Error.copy(0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isUp) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        null,
                        tint = if (isUp) OltviColors.Success else OltviColors.Error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${"%.1f".format(kotlin.math.abs(pct))}%",
                        style = OltviTypography.cuerpo.copy(
                            color = if (isUp) OltviColors.Success else OltviColors.Error,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Mes anterior",
                        style = OltviTypography.etiqueta.copy(color = OltviColors.OnSurfaceDim)
                    )
                    Text(
                        formatMoney(mesAnterior),
                        style = OltviTypography.cuerpo.copy(color = OltviColors.OnSurfaceDim)
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleInfoCard(
    vehiculo: VehiculoInfo,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DirectionsCar, null, tint = OltviColors.Action, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Vehiculo",
                    style = OltviTypography.subtitulo.copy(color = OltviColors.OnSurface)
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VehicleDetail(label = "Marca / Modelo", value = "${vehiculo.marca} ${vehiculo.modelo}")
                    VehicleDetail(label = "Ano", value = "${vehiculo.anio}")
                    VehicleDetail(label = "Color", value = vehiculo.color)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VehicleDetail(label = "Patente", value = vehiculo.patente)
                    VehicleDetail(label = "VTV vence", value = vehiculo.vtvVencimiento)
                }
            }
        }
    }
}

@Composable
private fun VehicleDetail(label: String, value: String) {
    Column {
        Text(label, style = OltviTypography.etiqueta.copy(color = OltviColors.OnSurfaceDim))
        Text(
            value,
            style = OltviTypography.cuerpo.copy(
                color = OltviColors.OnSurface,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun DocumentsCard(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Documentos",
                style = OltviTypography.subtitulo.copy(color = OltviColors.OnSurface)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentChip(label = "VTV", isValid = true, modifier = Modifier.weight(1f))
                DocumentChip(label = "Seguro", isValid = true, modifier = Modifier.weight(1f))
                DocumentChip(label = "Licencia", isValid = true, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DocumentChip(
    label: String,
    isValid: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isValid) OltviColors.Success else OltviColors.Error

    Box(
        modifier = modifier
            .background(color.copy(0.12f), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(0.4f), RoundedCornerShape(10.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (isValid) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = OltviTypography.etiqueta.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun SettingsSection() {
    val items = listOf(
        Triple(Icons.Filled.Notifications, "Notificaciones", OltviColors.Action),
        Triple(Icons.Filled.SupportAgent, "Soporte", OltviColors.ActionLight),
        Triple(Icons.Filled.Logout, "Cerrar sesion", OltviColors.Error)
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(OltviColors.Surface)
    ) {
        items.forEachIndexed { index, (icon, label, color) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    style = OltviTypography.cuerpo.copy(
                        color = if (label == "Cerrar sesion") OltviColors.Error else OltviColors.OnSurface
                    )
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    null,
                    tint = OltviColors.OnSurfaceDim,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (index < items.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp),
                    color = OltviColors.Divider
                )
            }
        }
    }
}

private fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "AR"))
    formatter.maximumFractionDigits = 0
    return "$${formatter.format(amount)}"
}
