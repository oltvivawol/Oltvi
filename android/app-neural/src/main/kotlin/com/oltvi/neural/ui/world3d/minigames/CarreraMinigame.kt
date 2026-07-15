package com.oltvi.neural.ui.world3d.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// VAWOL — Carrera de Barrio · 5 checkpoints, 60 s
// World-space positions (x, z in scene units where 1 unit = 1 m)
// ---------------------------------------------------------------------------

enum class CarreraEvento { NINGUNO, CHECKPOINT, COMPLETADA, TIEMPO_AGOTADO }

class CarreraState {
    val checkpoints: List<Pair<Float, Float>> = listOf(
        Pair(0f, 0f),      // start (near spawn)
        Pair(-18f, -22f),  // left arc
        Pair(0f, -40f),    // far end of neighborhood
        Pair(22f, -24f),   // right side
        Pair(14f, -8f)     // loop back to finish
    )

    var activa by mutableStateOf(false)
        private set
    var tiempoRestante by mutableIntStateOf(60)
        private set
    var checkpointActual by mutableIntStateOf(0)
        private set
    var completada by mutableStateOf(false)
        private set
    var tiempoFinal by mutableStateOf<Int?>(null)
        private set
    var mejorTiempo by mutableStateOf<Int?>(null)
        private set

    private var timerAccum = 0f

    fun iniciar() {
        activa = true
        tiempoRestante = 60
        checkpointActual = 0
        completada = false
        tiempoFinal = null
        timerAccum = 0f
    }

    fun cancelar() {
        activa = false
        checkpointActual = 0
    }

    // Called every frame from onFrame (main thread). Returns event for this frame.
    fun update(dt: Float, playerX: Float, playerZ: Float): CarreraEvento {
        if (!activa) return CarreraEvento.NINGUNO

        timerAccum += dt
        if (timerAccum >= 1f) {
            timerAccum -= 1f
            tiempoRestante = (tiempoRestante - 1).coerceAtLeast(0)
        }
        if (tiempoRestante == 0) {
            activa = false
            return CarreraEvento.TIEMPO_AGOTADO
        }

        val (cpX, cpZ) = checkpoints[checkpointActual]
        val dx = playerX - cpX
        val dz = playerZ - cpZ
        if (sqrt(dx * dx + dz * dz) < 2.0f) {
            checkpointActual++
            if (checkpointActual >= checkpoints.size) {
                val elapsed = 60 - tiempoRestante
                completada = true
                tiempoFinal = elapsed
                if (mejorTiempo == null || elapsed < mejorTiempo!!) mejorTiempo = elapsed
                activa = false
                return CarreraEvento.COMPLETADA
            }
            return CarreraEvento.CHECKPOINT
        }
        return CarreraEvento.NINGUNO
    }
}

// ---------------------------------------------------------------------------
// HUD overlay — shown on top of the 3D scene
// ---------------------------------------------------------------------------

@Composable
fun CarreraHUD(
    state: CarreraState,
    xpGanado: Int,
    modifier: Modifier = Modifier
) {
    val nc = LocalNeuralColors.current

    Box(modifier = modifier) {
        when {
            state.activa -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(nc.deep.copy(0.88f))
                        .border(1.dp, NeuralColors.xpGold.copy(0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🏁 CARRERA DE BARRIO",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeuralColors.xpGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "%d:%02d".format(state.tiempoRestante / 60, state.tiempoRestante % 60),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.tiempoRestante <= 10) Color(0xFFE74C3C) else nc.textPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(state.checkpoints.size) { i ->
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            i < state.checkpointActual -> NeuralColors.success
                                            i == state.checkpointActual -> NeuralColors.xpGold
                                            else -> nc.glassOverlay
                                        }
                                    )
                            )
                        }
                    }
                    Text(
                        "Checkpoint ${state.checkpointActual + 1} / ${state.checkpoints.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = nc.textSecondary
                    )
                    if (state.mejorTiempo != null) {
                        Text(
                            "Récord: ${state.mejorTiempo}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeuralColors.electric
                        )
                    }
                }
            }

            state.completada && state.tiempoFinal != null -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(nc.deep.copy(0.92f))
                        .border(2.dp, NeuralColors.success, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "¡Carrera completada!",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeuralColors.success,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tiempo: ${state.tiempoFinal}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = nc.textPrimary
                        )
                        if (state.mejorTiempo == state.tiempoFinal) {
                            Spacer(Modifier.height(2.dp))
                            Text("⚡ ¡Nuevo récord!", color = NeuralColors.xpGold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "+$xpGanado XP  ·  Insignia Velocista 🎖️",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeuralColors.electric
                        )
                    }
                }
            }
        }
    }
}
