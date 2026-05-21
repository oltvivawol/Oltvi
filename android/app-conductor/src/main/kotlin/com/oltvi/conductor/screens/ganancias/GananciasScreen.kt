package com.oltvi.conductor.screens.ganancias

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oltvi.core.data.models.*
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.effects.GlassCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale
import javax.inject.Inject

// ── Data ───────────────────────────────────────────────────────────────────

data class GananciaEntry(
    val label: String,
    val monto: Double,
    val esPico: Boolean = false
)

data class ViajeResumen(
    val origen: String,
    val destino: String,
    val monto: Double,
    val hora: String,
    val duracionMin: Int
)

enum class PeriodoGanancias(val label: String) {
    HOY("Hoy"),
    SEMANA("Semana"),
    MES("Mes")
}

// ── ViewModel ──────────────────────────────────────────────────────────────

@HiltViewModel
class GananciasViewModel @Inject constructor() : ViewModel() {

    private val _periodo = MutableStateFlow(PeriodoGanancias.SEMANA)
    val periodo: StateFlow<PeriodoGanancias> = _periodo.asStateFlow()

    private val datosHoy = listOf(
        GananciaEntry("06h", 0.0), GananciaEntry("08h", 1200.0), GananciaEntry("10h", 850.0),
        GananciaEntry("12h", 2100.0, esPico = true), GananciaEntry("14h", 900.0),
        GananciaEntry("16h", 1800.0), GananciaEntry("18h", 2800.0, esPico = true),
        GananciaEntry("20h", 2200.0), GananciaEntry("22h", 1600.0), GananciaEntry("00h", 800.0)
    )

    private val datosSemana = listOf(
        GananciaEntry("Lun", 8500.0), GananciaEntry("Mar", 9200.0), GananciaEntry("Mie", 11_000.0),
        GananciaEntry("Jue", 7800.0), GananciaEntry("Vie", 15_400.0, esPico = true),
        GananciaEntry("Sab", 13_200.0), GananciaEntry("Dom", 6_100.0)
    )

    private val datosMes = listOf(
        GananciaEntry("S1", 52_000.0), GananciaEntry("S2", 67_000.0, esPico = true),
        GananciaEntry("S3", 58_000.0), GananciaEntry("S4", 71_200.0, esPico = true)
    )

    val ultimos5Viajes = listOf(
        ViajeResumen("Palermo", "Aeropuerto Ezeiza", 8_200.0, "14:32", 38),
        ViajeResumen("Recoleta", "Microcentro", 3_400.0, "12:15", 18),
        ViajeResumen("Belgrano", "Villa Crespo", 2_100.0, "10:55", 12),
        ViajeResumen("Caballito", "Palermo", 2_900.0, "09:20", 16),
        ViajeResumen("San Telmo", "Puerto Madero", 1_800.0, "08:05", 10)
    )

    val datosActuales: StateFlow<List<GananciaEntry>> = MutableStateFlow(datosSemana).asStateFlow()

    private val _datosParaGrafico = MutableStateFlow(datosSemana)
    val datosParaGrafico: StateFlow<List<GananciaEntry>> = _datosParaGrafico.asStateFlow()

    private val _totalActual = MutableStateFlow(71_200.0)
    val totalActual: StateFlow<Double> = _totalActual.asStateFlow()

    private val _viajesActual = MutableStateFlow(47)
    val viajesActual: StateFlow<Int> = _viajesActual.asStateFlow()

    private val _horasTrabajadas = MutableStateFlow(38.5)
    val horasTrabajadas: StateFlow<Double> = _horasTrabajadas.asStateFlow()

    fun seleccionarPeriodo(p: PeriodoGanancias) {
        _periodo.value = p
        when (p) {
            PeriodoGanancias.HOY -> {
                _datosParaGrafico.value = datosHoy
                _totalActual.value = 12_450.0
                _viajesActual.value = 8
                _horasTrabajadas.value = 6.5
            }
            PeriodoGanancias.SEMANA -> {
                _datosParaGrafico.value = datosSemana
                _totalActual.value = 71_200.0
                _viajesActual.value = 47
                _horasTrabajadas.value = 38.5
            }
            PeriodoGanancias.MES -> {
                _datosParaGrafico.value = datosMes
                _totalActual.value = 248_200.0
                _viajesActual.value = 187
                _horasTrabajadas.value = 152.0
            }
        }
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────

@Composable
fun GananciasScreen(
    viewModel: GananciasViewModel = hiltViewModel()
) {
    val periodo by viewModel.periodo.collectAsStateWithLifecycle()
    val datos by viewModel.datosParaGrafico.collectAsStateWithLifecycle()
    val total by viewModel.totalActual.collectAsStateWithLifecycle()
    val viajes by viewModel.viajesActual.collectAsStateWithLifecycle()
    val horas by viewModel.horasTrabajadas.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OltviColors.PrincipalDeep)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 56.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.AccountBalance,
                    null,
                    tint = OltviColors.Action,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Ganancias",
                    style = OltviTypography.titulo.copy(color = OltviColors.OnSurface)
                )
            }
        }

        // Period selector
        item {
            PeriodoSelector(
                selectedPeriodo = periodo,
                onSelect = { viewModel.seleccionarPeriodo(it) }
            )
        }

        // Main earnings card
        item {
            MainEarningsCard(total = total, viajes = viajes, horas = horas)
        }

        // Bar chart
        item {
            GananciasBarChart(datos = datos)
        }

        // Copilot insight
        item {
            CopilotoInsightCard()
        }

        // Last trips header
        item {
            Text(
                "Ultimos viajes",
                style = OltviTypography.subtitulo.copy(color = OltviColors.OnSurface)
            )
        }

        // Last 5 trips
        items(viewModel.ultimos5Viajes) { viaje ->
            ViajeResumenCard(viaje = viaje)
        }
    }
}

@Composable
private fun PeriodoSelector(
    selectedPeriodo: PeriodoGanancias,
    onSelect: (PeriodoGanancias) -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PeriodoGanancias.entries.forEach { p ->
                val isSelected = selectedPeriodo == p
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) OltviColors.Action else Color.Transparent
                        )
                        .clickable { onSelect(p) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        p.label,
                        style = OltviTypography.hud.copy(
                            color = if (isSelected) OltviColors.White else OltviColors.OnSurfaceDim,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MainEarningsCard(total: Double, viajes: Int, horas: Double) {
    var animatedTotal by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(total) {
        val start = 0.0
        val steps = 60
        val stepValue = total / steps
        repeat(steps) { i ->
            animatedTotal = stepValue * (i + 1)
            kotlinx.coroutines.delay(16L)
        }
        animatedTotal = total
    }

    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "TOTAL GANANCIAS",
                style = OltviTypography.etiqueta.copy(
                    color = OltviColors.OnSurfaceDim,
                    letterSpacing = 2.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                formatMoney(animatedTotal),
                style = OltviTypography.display.copy(
                    color = OltviColors.OnSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp
                )
            )
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = OltviColors.Divider)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EarningsStat(
                    icon = Icons.Filled.DirectionsCar,
                    value = "$viajes",
                    label = "Viajes",
                    color = OltviColors.Action
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(OltviColors.Divider)
                )
                EarningsStat(
                    icon = Icons.Filled.AccessTime,
                    value = "${"%.1f".format(horas)}h",
                    label = "Trabajadas",
                    color = OltviColors.ActionLight
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(OltviColors.Divider)
                )
                EarningsStat(
                    icon = Icons.Filled.Speed,
                    value = formatMoney(if (horas > 0) total / horas else 0.0),
                    label = "Por hora",
                    color = OltviColors.Success
                )
            }
        }
    }
}

@Composable
private fun EarningsStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = OltviTypography.subtitulo.copy(color = OltviColors.OnSurface, fontWeight = FontWeight.Bold)
        )
        Text(
            label,
            style = OltviTypography.etiqueta.copy(color = OltviColors.OnSurfaceDim)
        )
    }
}

@Composable
private fun GananciasBarChart(datos: List<GananciaEntry>) {
    val maxValue = datos.maxOfOrNull { it.monto } ?: 1.0

    // Animate bars
    val animatedHeights = datos.mapIndexed { i, _ ->
        val fraction by animateFloatAsState(
            targetValue = (datos[i].monto / maxValue).toFloat(),
            animationSpec = tween(
                durationMillis = 600,
                delayMillis = i * 60,
                easing = FastOutSlowInEasing
            ),
            label = "bar$i"
        )
        fraction
    }

    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "DESGLOSE",
                style = OltviTypography.etiqueta.copy(
                    color = OltviColors.OnSurfaceDim,
                    letterSpacing = 1.5.sp
                )
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Y-axis labels
                Column(
                    modifier = Modifier
                        .height(140.dp)
                        .width(40.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatMoneyShort(maxValue),
                        style = OltviTypography.etiqueta.copy(
                            color = OltviColors.OnSurfaceDim,
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        formatMoneyShort(maxValue / 2),
                        style = OltviTypography.etiqueta.copy(
                            color = OltviColors.OnSurfaceDim,
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        "$0",
                        style = OltviTypography.etiqueta.copy(
                            color = OltviColors.OnSurfaceDim,
                            fontSize = 9.sp
                        )
                    )
                }

                // Bars
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    datos.forEachIndexed { index, entry ->
                        val fraction = animatedHeights.getOrElse(index) { 0f }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Peak dot
                            if (entry.esPico) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(OltviColors.Action, CircleShape)
                                )
                                Spacer(Modifier.height(2.dp))
                            }

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((120 * fraction).coerceAtLeast(4f).dp)
                            ) {
                                val gradientBrush = Brush.verticalGradient(
                                    colors = listOf(
                                        if (entry.esPico) Color(0xFFFFB74D) else OltviColors.Action,
                                        OltviColors.ActionDim
                                    )
                                )
                                drawRoundRect(
                                    brush = gradientBrush,
                                    size = Size(size.width, size.height),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            // X-axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                datos.forEach { entry ->
                    Text(
                        entry.label,
                        modifier = Modifier.weight(1f),
                        style = OltviTypography.etiqueta.copy(
                            color = OltviColors.OnSurfaceDim,
                            fontSize = 9.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun CopilotoInsightCard() {
    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(OltviColors.Action.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        null,
                        tint = OltviColors.Action,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "INSIGHT DE IA",
                    style = OltviTypography.etiqueta.copy(
                        color = OltviColors.Action,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Este viernes ganaste 23% mas en Palermo entre 18-20h",
                style = OltviTypography.cuerpo.copy(
                    color = OltviColors.OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Zona Hollywood: 8 viajes, promedio $ 3,200 por viaje. Tendencia en aumento para el proximo viernes.",
                style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .background(OltviColors.Success.copy(0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Filled.TipsAndUpdates,
                    null,
                    tint = OltviColors.Success,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Recomendacion: Posicionate en Palermo los viernes desde las 17h",
                    style = OltviTypography.pequeno.copy(color = OltviColors.Success)
                )
            }
        }
    }
}

@Composable
private fun ViajeResumenCard(viaje: ViajeResumen) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Route dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(OltviColors.Action, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(OltviColors.Divider)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(OltviColors.Success, CircleShape)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    viaje.origen,
                    style = OltviTypography.cuerpo.copy(
                        color = OltviColors.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    viaje.destino,
                    style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${viaje.hora}  ·  ${viaje.duracionMin} min",
                    style = OltviTypography.etiqueta.copy(color = OltviColors.OnSurfaceDim)
                )
            }

            Text(
                formatMoney(viaje.monto),
                style = OltviTypography.subtitulo.copy(
                    color = OltviColors.Success,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

private fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "AR"))
    formatter.maximumFractionDigits = 0
    return "$${formatter.format(amount)}"
}

private fun formatMoneyShort(amount: Double): String {
    return when {
        amount >= 1_000_000 -> "${"%.1f".format(amount / 1_000_000)}M"
        amount >= 1_000 -> "${"%.0f".format(amount / 1_000)}k"
        else -> "$${amount.toInt()}"
    }
}
