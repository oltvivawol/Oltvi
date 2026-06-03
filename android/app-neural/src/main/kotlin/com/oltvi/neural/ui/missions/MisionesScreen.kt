package com.oltvi.neural.ui.missions

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.Mision
import com.oltvi.neural.data.NeuralSeedData
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.TipoMision
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import com.oltvi.neural.ui.components.MisionCard
import com.oltvi.neural.ui.components.NeuralCard

@Composable
fun MisionesScreen(
    objetivo: ObjetivoVida = ObjetivoVida.TRABAJO,
    onBack: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val misiones = remember {
        mutableStateListOf(*NeuralSeedData.misionesPorObjetivo(objetivo).toTypedArray())
    }
    var filtroActivo by remember { mutableStateOf<TipoMision?>(null) }
    var xpTotal by remember { mutableStateOf(0) }
    var misionesCompletadas by remember { mutableStateOf(0) }

    val misionesFiltradas = if (filtroActivo == null) misiones
    else misiones.filter { it.tipo == filtroActivo }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(nc.deep)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "←",
                        color = nc.textSecondary,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBack() }
                            .padding(8.dp)
                    )
                    Text(
                        "MISIONES",
                        style = MaterialTheme.typography.labelLarge,
                        color = nc.electric,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.width(40.dp))
                }
                Spacer(Modifier.height(20.dp))
            }

            // XP Counter header
            item {
                AnimatedVisibility(
                    visible = xpTotal > 0,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it }
                ) {
                    NeuralCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeuralColors.xpGold.copy(0.4f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "+$xpTotal XP ganados esta sesión",
                                style = MaterialTheme.typography.titleSmall,
                                color = NeuralColors.xpGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Progress summary
            item {
                MisionesSummary(
                    total = misiones.size,
                    completadas = misionesCompletadas
                )
                Spacer(Modifier.height(20.dp))
            }

            // Filtros
            item {
                Text(
                    "FILTRAR POR TIPO",
                    style = MaterialTheme.typography.labelMedium,
                    color = nc.textSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroChip(
                        label = "Todas",
                        selected = filtroActivo == null,
                        color = nc.electric,
                        onClick = { filtroActivo = null }
                    )
                    val tiposPresentes = misiones.map { it.tipo }.distinct()
                    tiposPresentes.forEach { tipo ->
                        FiltroChip(
                            label = tipo.displayName,
                            selected = filtroActivo == tipo,
                            color = Color(android.graphics.Color.parseColor(tipo.colorHex)),
                            onClick = { filtroActivo = if (filtroActivo == tipo) null else tipo }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Pendientes
            if (misionesFiltradas.any { !it.completada }) {
                item {
                    Text(
                        "PENDIENTES",
                        style = MaterialTheme.typography.labelMedium,
                        color = nc.electric,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(10.dp))
                }
                items(misionesFiltradas.filter { !it.completada }) { mision ->
                    MisionCard(
                        mision = mision,
                        onComplete = { m ->
                            val idx = misiones.indexOfFirst { it.id == m.id }
                            if (idx >= 0) {
                                misiones[idx] = m.copy(completada = true, progreso = 1f)
                                xpTotal += m.xpRecompensa
                                misionesCompletadas++
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Completadas
            if (misionesFiltradas.any { it.completada }) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "COMPLETADAS",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeuralColors.success,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(10.dp))
                }
                items(misionesFiltradas.filter { it.completada }) { mision ->
                    MisionCard(mision = mision, onComplete = {})
                    Spacer(Modifier.height(10.dp))
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun MisionesSummary(total: Int, completadas: Int) {
    val nc = LocalNeuralColors.current
    val progreso = if (total > 0) completadas.toFloat() / total else 0f

    NeuralCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Progreso de misiones",
                    style = MaterialTheme.typography.titleSmall,
                    color = nc.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "$completadas / $total",
                    style = MaterialTheme.typography.titleSmall,
                    color = NeuralColors.electric,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(nc.glassOverlay)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progreso)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeuralColors.neural, NeuralColors.electric)
                            )
                        )
                )
            }
            if (completadas == total && total > 0) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeuralColors.success.copy(0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🏆 Todas las misiones completadas. +500 XP bonus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeuralColors.success,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltroChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val nc = LocalNeuralColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) color.copy(0.2f) else nc.elevated)
            .border(1.dp, if (selected) color else nc.glassBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else nc.textSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
