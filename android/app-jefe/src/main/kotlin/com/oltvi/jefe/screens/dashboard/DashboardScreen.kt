package com.oltvi.jefe.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.oltvi.core.ai.orchestrator.OltviOrchestrator
import com.oltvi.core.data.models.*
import com.oltvi.core.theme.*
import com.oltvi.core.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val orchestrator: OltviOrchestrator
) : ViewModel() {

    private val _insights = MutableStateFlow<List<InsightOperaciones>>(emptyList())
    val insights: StateFlow<List<InsightOperaciones>> = _insights.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Live metrics that "tick" every 30 seconds
    private val _viajesActivos = MutableStateFlow(23)
    private val _conductoresOnline = MutableStateFlow(47)
    private val _etaPromedio = MutableStateFlow(6)
    val viajesActivos: StateFlow<Int> = _viajesActivos.asStateFlow()
    val conductoresOnline: StateFlow<Int> = _conductoresOnline.asStateFlow()
    val etaPromedio: StateFlow<Int> = _etaPromedio.asStateFlow()

    init {
        loadInsights()
        tickMetrics()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = orchestrator.generarReporteOperaciones()
                _insights.value = result
            } catch (e: Exception) {
                _insights.value = mockInsights()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun tickMetrics() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                _viajesActivos.update { (it + (-2..3).random()).coerceIn(10, 60) }
                _conductoresOnline.update { (it + (-1..2).random()).coerceIn(20, 80) }
                _etaPromedio.update { (it + (-1..1).random()).coerceIn(3, 12) }
            }
        }
    }

    private fun mockInsights() = listOf(
        InsightOperaciones(TipoInsight.DEMANDA, "Pico de demanda en Palermo 18-20h", "Incremento del 340% esperado el viernes próximo en Palermo y Recoleta.", "+$180,000 ARS revenue", 1),
        InsightOperaciones(TipoInsight.FLOTA, "7 vehículos requieren mantenimiento", "VTV a vencer en próximos 15 días en 7 unidades de la flota.", "Previene pérdida de 23 conductores", 2),
        InsightOperaciones(TipoInsight.REVENUE, "Revenue 12% bajo objetivo semanal", "Caídas del 18% el martes y miércoles correlacionadas con corte eléctrico.", "Recuperar $95,000 en 48h", 2),
        InsightOperaciones(TipoInsight.SEGURIDAD, "3 incidentes en Av. Corrientes nocturna", "Reportes menores entre 22:00-02:00 esta semana. Zona requiere monitoreo.", "Reducción 60% incidentes", 1),
        InsightOperaciones(TipoInsight.GENERAL, "Rating plataforma subió a 4.82 ⭐", "Incremento de 0.15 puntos vs mes anterior. Programa de incentivos funcionando.", "Retención +8%", 4)
    )
}

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val insights by viewModel.insights.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val viajesActivos by viewModel.viajesActivos.collectAsStateWithLifecycle()
    val conductoresOnline by viewModel.conductoresOnline.collectAsStateWithLifecycle()
    val etaPromedio by viewModel.etaPromedio.collectAsStateWithLifecycle()

    // Live clock
    var horaActual by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); horaActual = LocalTime.now() } }

    // Critical alert if any SEGURIDAD insight
    val alertaCritica = insights.firstOrNull { it.tipo == TipoInsight.SEGURIDAD && it.prioridad == 1 }

    LazyColumn(
        Modifier.fillMaxSize().background(OltviColors.PrincipalDeep),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Torre de Control", style = OltviTypography.titulo, color = OltviColors.OnSurface, fontWeight = FontWeight.Black)
                    Text("Panel de administración OLTVI", style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
                }
                GlassCard {
                    Text(
                        horaActual.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = OltviTypography.subtitulo,
                        color = OltviColors.Action,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Critical alert banner
        if (alertaCritica != null) {
            item {
                val alpha by rememberInfiniteTransition(label = "blink").animateFloat(
                    initialValue = 0.6f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "alpha"
                )
                Surface(
                    color = OltviColors.Error.copy(0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().alpha(alpha).border(1.dp, OltviColors.Error.copy(0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = OltviColors.Error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(alertaCritica.titulo, style = OltviTypography.cuerpo, color = OltviColors.Error, fontWeight = FontWeight.Bold)
                            Text(alertaCritica.descripcion, style = OltviTypography.pequeno, color = OltviColors.Error.copy(0.8f))
                        }
                    }
                }
            }
        }

        // Live metrics
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedMetricCard("Viajes activos", viajesActivos.toString(), Icons.Filled.DirectionsCar, OltviColors.Action, Modifier.weight(1f))
                AnimatedMetricCard("Conductores", conductoresOnline.toString(), Icons.Filled.People, OltviColors.Success, Modifier.weight(1f))
                AnimatedMetricCard("ETA prom.", "$etaPromedio min", Icons.Filled.Timer, OltviColors.Warning, Modifier.weight(1f))
            }
        }

        // Revenue today
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Revenue hoy", style = OltviTypography.etiqueta, color = OltviColors.OnSurfaceDim)
                        Text("$284,750 ARS", style = OltviTypography.titulo, color = OltviColors.OnSurface, fontWeight = FontWeight.Black)
                    }
                    Surface(color = OltviColors.Success.copy(0.15f), shape = RoundedCornerShape(8.dp)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, null, tint = OltviColors.Success, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("+12% vs ayer", style = OltviTypography.pequeno, color = OltviColors.Success)
                        }
                    }
                }
            }
        }

        item {
            Text("Insights de IA", style = OltviTypography.subtitulo, color = OltviColors.OnSurface, fontWeight = FontWeight.Bold)
        }

        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OltviColors.Action)
                }
            }
        } else {
            items(insights) { insight ->
                InsightCard(insight)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun AnimatedMetricCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    GlassCard(modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = OltviTypography.subtitulo, color = OltviColors.OnSurface, fontWeight = FontWeight.Black)
            Text(label, style = OltviTypography.etiqueta, color = OltviColors.OnSurfaceDim, fontSize = 9.sp)
        }
    }
}

@Composable
private fun InsightCard(insight: InsightOperaciones) {
    val borderColor = when (insight.tipo) {
        TipoInsight.DEMANDA -> OltviColors.Warning
        TipoInsight.FLOTA -> OltviColors.Action
        TipoInsight.REVENUE -> OltviColors.Success
        TipoInsight.SEGURIDAD -> OltviColors.Error
        TipoInsight.GENERAL -> OltviColors.OnSurfaceDim
    }
    val icon = when (insight.tipo) {
        TipoInsight.DEMANDA -> Icons.Filled.TrendingUp
        TipoInsight.FLOTA -> Icons.Filled.DirectionsCar
        TipoInsight.REVENUE -> Icons.Filled.AttachMoney
        TipoInsight.SEGURIDAD -> Icons.Filled.Shield
        TipoInsight.GENERAL -> Icons.Filled.Lightbulb
    }

    Surface(
        color = OltviColors.Surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(
            width = 1.dp,
            color = OltviColors.Divider,
            shape = RoundedCornerShape(14.dp)
        )
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(borderColor, RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)))
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = borderColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(insight.titulo, style = OltviTypography.cuerpo, color = OltviColors.OnSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Surface(color = borderColor.copy(0.15f), shape = RoundedCornerShape(4.dp)) {
                        Text("P${insight.prioridad}", style = OltviTypography.etiqueta, color = borderColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(insight.descripcion, style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
                Spacer(Modifier.height(6.dp))
                Surface(color = OltviColors.SurfaceVariant, shape = RoundedCornerShape(6.dp)) {
                    Text(insight.impactoEstimado, style = OltviTypography.etiqueta, color = OltviColors.OnSurface, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}
