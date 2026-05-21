package com.oltvi.conductor.screens.ganancias

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.ui.effects.GlassCard

private data class TripEntry(
    val destino: String,
    val precio: String,
    val estrellas: Int,
    val hora: String,
)

private enum class Rango { HOY, SEMANA, MES }

private data class RangoData(
    val total: String,
    val viajes: Int,
    val barLabels: List<String>,
    val barValues: List<Float>,
    val insight: String,
)

private val RANGOS = mapOf(
    Rango.HOY to RangoData(
        total = "$2,450", viajes = 8,
        barLabels = listOf("6h","8h","10h","12h","14h","16h","18h","20h","22h"),
        barValues   = listOf(0f, 120f, 280f, 350f, 180f, 420f, 650f, 310f, 140f),
        insight = "Tu mejor momento fue a las 18:00 en Palermo. ¡Arranque perfecto!"
    ),
    Rango.SEMANA to RangoData(
        total = "$14,800", viajes = 51,
        barLabels = listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom"),
        barValues   = listOf(1800f, 2100f, 2400f, 1950f, 2700f, 3200f, 650f),
        insight = "El sábado fue tu mejor día: $3,200 en 13 viajes. Seguí apostando a los fines de semana."
    ),
    Rango.MES to RangoData(
        total = "$62,000", viajes = 205,
        barLabels = listOf("S1","S2","S3","S4"),
        barValues   = listOf(13500f, 16800f, 17200f, 14500f),
        insight = "Tu mejor semana fue la 3era: ganaste 23% más que tu promedio mensual."
    ),
)

private val TRIPS = listOf(
    TripEntry("Aeropuerto Jorge Newbery", "$3,200", 5, "09:15"),
    TripEntry("Belgrano, Ciudad Autónoma", "$1,850", 5, "11:30"),
    TripEntry("Recoleta - Museo de Bellas Artes", "$950", 4, "13:45"),
    TripEntry("Palermo Hollywood", "$1,100", 5, "16:00"),
    TripEntry("Villa Crespo - Corrientes", "$780", 5, "17:30"),
    TripEntry("San Telmo - Mercado", "$1,250", 4, "19:00"),
    TripEntry("Once - Pueyrredón", "$620", 5, "20:15"),
    TripEntry("Caballito - Rivadavia", "$890", 5, "21:45"),
)

@Composable
fun GananciasScreen() {
    var rangoSeleccionado by remember { mutableStateOf(Rango.SEMANA) }
    val data = RANGOS[rangoSeleccionado]!!

    val animatedTotal by animateIntAsState(
        targetValue = data.total.filter { it.isDigit() }.toIntOrNull() ?: 0,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "total"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1820))
            .navigationBarsPadding(),
        contentPadding = PaddingValues(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Ganancias", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Rango.values().forEach { r ->
                    val selected = r == rangoSeleccionado
                    FilterChip(
                        selected = selected,
                        onClick = { rangoSeleccionado = r },
                        label = { Text(r.label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OltviColors.action,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(0.08f),
                            labelColor = Color.White.copy(0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selected,
                            borderColor = Color.Transparent,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }

        item {
            GlassCard {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("$", color = OltviColors.action, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "%,d".format(animatedTotal),
                            color = Color.White,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "${data.viajes} viajes · ${rangoSeleccionado.label.lowercase()}",
                        color = Color.White.copy(0.6f),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    EarningsBarChart(
                        labels = data.barLabels,
                        values = data.barValues,
                        color = OltviColors.action,
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            }
        }

        item {
            GlassCard {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = OltviColors.action, modifier = Modifier.size(20.dp))
                    Column {
                        Text("Consejo de tu copiloto IA", color = OltviColors.action, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(data.insight, color = Color.White.copy(0.85f), fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        }

        item {
            Text("Viajes recientes", color = Color.White.copy(0.7f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        items(TRIPS) { trip ->
            GlassCard {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                            .background(OltviColors.action.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.TrendingUp, null, tint = OltviColors.action, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(trip.destino, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(trip.hora, color = Color.White.copy(0.5f), fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            repeat(trip.estrellas) {
                                Icon(Icons.Filled.Star, null, tint = Color(0xFFF1C40F), modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                    Text(trip.precio, color = OltviColors.action, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun EarningsBarChart(
    labels: List<String>,
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val maxVal = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val transition = rememberInfiniteTransition(label = "bar")
    val anims = values.mapIndexed { i, _ ->
        val animVal by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(800, delayMillis = i * 60, easing = EaseOutCubic),
            label = "bar$i"
        )
        animVal
    }

    Canvas(modifier = modifier) {
        val barW = (size.width / (labels.size * 1.6f)).coerceAtMost(32.dp.toPx())
        val spacing = (size.width - barW * labels.size) / (labels.size + 1)
        values.forEachIndexed { i, v ->
            val barH = (v / maxVal) * (size.height - 20.dp.toPx()) * anims[i]
            val x = spacing + i * (barW + spacing)
            val y = size.height - 20.dp.toPx() - barH
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(0.2f), color),
                    startY = y, endY = size.height - 20.dp.toPx()
                ),
                topLeft = Offset(x, y),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawContext.canvas.nativeCanvas.drawText(
                labels[i],
                x + barW / 2f,
                size.height,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 10.dp.toPx()
                    setColor(android.graphics.Color.parseColor("#99FFFFFF"))
                }
            )
        }
    }
}

private val Rango.label get() = when (this) {
    Rango.HOY -> "Hoy"
    Rango.SEMANA -> "Esta semana"
    Rango.MES -> "Este mes"
}
