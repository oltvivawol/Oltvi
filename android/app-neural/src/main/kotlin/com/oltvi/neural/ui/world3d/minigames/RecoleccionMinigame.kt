package com.oltvi.neural.ui.world3d.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import kotlin.math.sqrt
import kotlin.random.Random

// ---------------------------------------------------------------------------
// VAWOL — Recolección de Neurocréditos · 10 esferas en el mundo
// ---------------------------------------------------------------------------

class RecoleccionState(seed: Long = 77331L) {
    // Posiciones (x, z) en espacio de mundo — distribuidas dentro de 32×32 m del origen
    val posiciones: List<Pair<Float, Float>> = List(10) { i ->
        val rng = Random(seed + i * 13L)
        Pair(rng.nextFloat() * 60f - 30f, rng.nextFloat() * 60f - 30f)
    }

    val recolectados = mutableStateListOf(*Array(10) { false })
    var totalCreditos by mutableIntStateOf(0)
        private set

    // Called every frame from onFrame. Returns credits earned this tick.
    fun update(playerX: Float, playerZ: Float): Int {
        var ganados = 0
        posiciones.forEachIndexed { i, (x, z) ->
            if (!recolectados[i]) {
                val dx = playerX - x
                val dz = playerZ - z
                if (sqrt(dx * dx + dz * dz) < 1.5f) {
                    recolectados[i] = true
                    totalCreditos += 25
                    ganados += 25
                }
            }
        }
        return ganados
    }

    fun resetear() {
        repeat(recolectados.size) { recolectados[it] = false }
        totalCreditos = 0
    }

    val pendientes: Int get() = recolectados.count { !it }
    val todosRecolectados: Boolean get() = pendientes == 0
}

// ---------------------------------------------------------------------------
// HUD badge — top-right corner of the 3D scene
// ---------------------------------------------------------------------------

@Composable
fun RecoleccionHUD(
    state: RecoleccionState,
    modifier: Modifier = Modifier
) {
    val nc = LocalNeuralColors.current
    val done = state.todosRecolectados

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(nc.deep.copy(0.85f))
            .border(1.dp, NeuralColors.electric.copy(0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (done) "✨ ¡Todo recolectado!" else "⚡ Neurocréditos",
            style = MaterialTheme.typography.labelSmall,
            color = if (done) NeuralColors.success else NeuralColors.electric
        )
        Text(
            text = "${state.totalCreditos} CR",
            style = MaterialTheme.typography.titleSmall,
            color = NeuralColors.xpGold,
            fontWeight = FontWeight.Bold
        )
        if (!done) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${state.pendientes} restantes",
                style = MaterialTheme.typography.labelSmall,
                color = nc.textSecondary
            )
        }
    }
}
