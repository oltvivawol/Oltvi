package com.oltvi.neural.ui.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.Logro
import com.oltvi.neural.data.NivelNeural
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import com.oltvi.neural.data.NeuralSeedData
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import com.oltvi.neural.ui.components.AvatarBadge
import com.oltvi.neural.ui.components.NeuralCard
import com.oltvi.neural.ui.components.StatChip
import com.oltvi.neural.ui.components.XpProgressBar

@Composable
fun ProfileScreen(
    perfil: PerfilNeural = PerfilNeural(
        id = "demo",
        nombre = "Jugador",
        clase = ClaseRPG.EXPLORADOR,
        objetivo = ObjetivoVida.TRABAJO,
        xpActual = 350,
        reputacion = 120,
        misionesCompletadas = 3,
        logros = NeuralSeedData.logrosIniciales().mapIndexed { i, l -> l.copy(obtenido = i == 0) }
    ),
    onBack: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val claseColor = Color(android.graphics.Color.parseColor(perfil.clase.colorHex))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(nc.deep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    "MI PERFIL",
                    style = MaterialTheme.typography.labelLarge,
                    color = nc.electric,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(40.dp))
            }

            Spacer(Modifier.height(32.dp))

            // Hero section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(claseColor.copy(0.15f), nc.surface)
                        )
                    )
                    .border(1.dp, claseColor.copy(0.3f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    AvatarBadge(
                        inicial = perfil.inicial,
                        clase = perfil.clase,
                        numeroNivel = perfil.numeroNivel,
                        size = 96.dp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        perfil.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        color = nc.textPrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "${perfil.clase.icon} ${perfil.clase.displayName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = claseColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "\"${perfil.nivel.title}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = nc.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    XpProgressBar(
                        xpActual = perfil.xpActual,
                        xpSiguiente = perfil.xpParaSiguienteNivel,
                        progreso = perfil.progresoNivel,
                        nivel = perfil.nivel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    label = "Reputación",
                    value = "${perfil.reputacion}",
                    icon = "⭐",
                    color = NeuralColors.xpGold,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Misiones",
                    value = "${perfil.misionesCompletadas}",
                    icon = "⚡",
                    color = NeuralColors.electric,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Logros",
                    value = "${perfil.logros.count { it.obtenido }}/${perfil.logros.size}",
                    icon = "🏆",
                    color = NeuralColors.neural,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Objetivo
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeuralColors.neural.copy(0.4f)
            ) {
                Column {
                    Text(
                        "OBJETIVO DE VIDA",
                        style = MaterialTheme.typography.labelMedium,
                        color = nc.electric,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(perfil.objetivo.icon, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                perfil.objetivo.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = nc.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "La IA construye tu camino hacia este objetivo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = nc.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Nivel path
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeuralColors.glassBorder
            ) {
                Column {
                    Text(
                        "CAMINO DE PROGRESO",
                        style = MaterialTheme.typography.labelMedium,
                        color = nc.electric,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    NivelNeural.entries.forEach { nivel ->
                        val isActual = nivel == perfil.nivel
                        val isPasado = nivel.minXp < perfil.nivel.minXp
                        val color = when {
                            isActual -> NeuralColors.neural
                            isPasado -> NeuralColors.success
                            else -> nc.textSecondary.copy(0.3f)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    nivel.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isPasado || isActual) nc.textPrimary else nc.textSecondary.copy(0.5f),
                                    fontWeight = if (isActual) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (isActual) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeuralColors.neural.copy(0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "ACTUAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeuralColors.neural,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    "${nivel.minXp} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = nc.textSecondary.copy(if (isPasado) 0.7f else 0.3f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Logros
            Text(
                "LOGROS",
                style = MaterialTheme.typography.labelMedium,
                color = nc.electric,
                letterSpacing = 1.5.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            perfil.logros.forEach { logro ->
                LogroItem(logro = logro)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LogroItem(logro: Logro) {
    val nc = LocalNeuralColors.current
    val color = if (logro.obtenido) NeuralColors.xpGold else nc.textSecondary.copy(0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (logro.obtenido) NeuralColors.xpGold.copy(0.08f) else nc.elevated)
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                logro.icono,
                fontSize = 20.sp,
                color = if (logro.obtenido) Color.Unspecified else Color.Transparent
            )
            if (!logro.obtenido) {
                Text("🔒", fontSize = 16.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                logro.titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = if (logro.obtenido) nc.textPrimary else nc.textSecondary.copy(0.5f),
                fontWeight = if (logro.obtenido) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                logro.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = nc.textSecondary.copy(if (logro.obtenido) 0.8f else 0.4f)
            )
        }
        if (logro.obtenido) {
            Text(
                "+${logro.xpBonus} XP",
                style = MaterialTheme.typography.labelSmall,
                color = NeuralColors.xpGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
