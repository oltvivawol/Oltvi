package com.oltvi.jefe.screens.analiticas

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.oltvi.core.theme.*
import com.oltvi.core.ui.components.*

private val weekRevenue = listOf(42f, 38f, 55f, 61f, 87f, 94f, 71f)
private val weekLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

@Composable
fun AnaliticasScreen() {
    var selectedPeriod by remember { mutableStateOf(0) }
    val periods = listOf("Hoy", "Semana", "Mes", "Año")

    Column(
        Modifier.fillMaxSize().background(OltviColors.PrincipalDeep)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("Analíticas", style = OltviTypography.titulo, color = OltviColors.OnSurface, fontWeight = FontWeight.Black)
            Text("Panel de métricas operacionales", style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
        }

        // Period selector
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            periods.forEachIndexed { idx, period ->
                Surface(
                    onClick = { selectedPeriod = idx },
                    color = if (selectedPeriod == idx) OltviColors.Action else OltviColors.SurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        period,
                        style = OltviTypography.etiqueta,
                        color = if (selectedPeriod == idx) OltviColors.White else OltviColors.OnSurfaceDim,
                        fontWeight = if (selectedPeriod == idx) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // Key metrics
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("1,284", "Viajes", "+18%", true, Modifier.weight(1f))
            MetricCard("$284,750", "Revenue ARS", "+12%", true, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("4.82 ⭐", "Rating prom.", "+0.15", true, Modifier.weight(1f))
            MetricCard("6.4 min", "ETA prom.", "-0.8", false, Modifier.weight(1f))
        }

        // Revenue chart
        GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Revenue por día", style = OltviTypography.cuerpo, color = OltviColors.OnSurface, fontWeight = FontWeight.Bold)
                    Text("Esta semana", style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
                }
                Spacer(Modifier.height(16.dp))
                RevenueBarChart(data = weekRevenue, labels = weekLabels, modifier = Modifier.fillMaxWidth().height(140.dp))
            }
        }

        // Comparison table
        GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Este período vs. anterior", style = OltviTypography.cuerpo, color = OltviColors.OnSurface, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                listOf(
                    Triple("Viajes", "1,284", "+18%"),
                    Triple("Revenue", "$284,750", "+12%"),
                    Triple("Rating promedio", "4.82", "+0.15"),
                    Triple("Conductores activos", "47", "+3"),
                    Triple("Cancelaciones", "3.2%", "-0.8%")
                ).forEach { (label, value, change) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(label, style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim, modifier = Modifier.weight(1f))
                        Text(value, style = OltviTypography.pequeno, color = OltviColors.OnSurface, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        val isPositive = !change.startsWith("-")
                        Surface(color = (if (isPositive) OltviColors.Success else OltviColors.Error).copy(0.15f), shape = RoundedCornerShape(4.dp)) {
                            Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward, null, tint = if (isPositive) OltviColors.Success else OltviColors.Error, modifier = Modifier.size(10.dp))
                                Text(change.trimStart('+', '-'), style = OltviTypography.etiqueta, color = if (isPositive) OltviColors.Success else OltviColors.Error)
                            }
                        }
                    }
                    Divider(color = OltviColors.Divider.copy(0.5f), thickness = 0.5.dp)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RevenueBarChart(data: List<Float>, labels: List<String>, modifier: Modifier) {
    val maxVal = data.max()
    val animSpec = remember { Animatable(0f) }
    LaunchedEffect(Unit) { animSpec.animateTo(1f, tween(1000, easing = EaseOutCubic)) }
    val anim = animSpec.value

    Canvas(modifier) {
        val barWidth = size.width / (data.size * 1.5f)
        val gap = barWidth * 0.5f
        val chartHeight = size.height - 24.dp.toPx()

        data.forEachIndexed { idx, value ->
            val x = idx * (barWidth + gap) + gap / 2
            val barHeight = (value / maxVal) * chartHeight * anim
            val y = chartHeight - barHeight

            // Bar with gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(OltviColors.ActionLight, OltviColors.Action),
                    startY = y, endY = chartHeight
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, change: String, positive: Boolean, modifier: Modifier) {
    GlassCard(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = OltviTypography.subtitulo, color = OltviColors.OnSurface, fontWeight = FontWeight.Black)
            Text(label, style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (positive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward, null, tint = if (positive) OltviColors.Success else OltviColors.Error, modifier = Modifier.size(12.dp))
                Text(change, style = OltviTypography.etiqueta, color = if (positive) OltviColors.Success else OltviColors.Error)
            }
        }
    }
}
