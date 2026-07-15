package com.oltvi.neural.ui.minigames

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.neural.data.NeuralSeedData
import com.oltvi.neural.data.TriviaQ
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import kotlinx.coroutines.delay

private const val SEGUNDOS_POR_PREGUNTA = 15

// ---------------------------------------------------------------------------
// VAWOL — Trivia del Barrio · La Esperanza / Jujuy / NOA
// ---------------------------------------------------------------------------

@Composable
fun TriviaBarrioScreen(onBack: () -> Unit) {
    val nc = LocalNeuralColors.current
    val preguntas = remember { NeuralSeedData.triviaLaEsperanza() }
    var indice by remember { mutableIntStateOf(0) }
    var respuestaElegida by remember { mutableStateOf<Int?>(null) }
    var puntajeTotal by remember { mutableIntStateOf(0) }
    var rachaActual by remember { mutableIntStateOf(0) }
    var rachaMaxima by remember { mutableIntStateOf(0) }
    var correctasTotal by remember { mutableIntStateOf(0) }
    var timerTicks by remember { mutableIntStateOf(SEGUNDOS_POR_PREGUNTA) }
    var terminado by remember { mutableStateOf(false) }

    fun avanzarPregunta() {
        if (indice < preguntas.lastIndex) {
            indice++
            respuestaElegida = null
            timerTicks = SEGUNDOS_POR_PREGUNTA
        } else {
            terminado = true
        }
    }

    fun responder(opcion: Int) {
        if (respuestaElegida != null) return
        respuestaElegida = opcion
        val correcta = preguntas[indice].correcta
        if (opcion == correcta) {
            rachaActual++
            if (rachaActual > rachaMaxima) rachaMaxima = rachaActual
            correctasTotal++
            val xpGanado = preguntas[indice].xp * rachaActual.coerceAtMost(5)
            puntajeTotal += xpGanado
        } else {
            rachaActual = 0
        }
    }

    // Auto-advance after choosing an answer
    LaunchedEffect(respuestaElegida) {
        if (respuestaElegida != null) {
            delay(1800L)
            avanzarPregunta()
        }
    }

    // Per-question countdown timer
    LaunchedEffect(indice) {
        timerTicks = SEGUNDOS_POR_PREGUNTA
        while (timerTicks > 0 && respuestaElegida == null && !terminado) {
            delay(1000L)
            timerTicks--
        }
        if (timerTicks == 0 && respuestaElegida == null && !terminado) {
            responder(-1)  // -1 = time out, wrong by default
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(nc.deep)
    ) {
        if (terminado) {
            TriviaResultados(
                preguntas = preguntas,
                correctas = correctasTotal,
                xpTotal = puntajeTotal,
                rachaMaxima = rachaMaxima,
                onBack = onBack
            )
        } else {
            AnimatedContent(
                targetState = indice,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "trivia_q"
            ) { idx ->
                val pregunta = preguntas[idx]
                TriviaQuestion(
                    pregunta = pregunta,
                    numeroPregunta = idx + 1,
                    totalPreguntas = preguntas.size,
                    timerTicks = timerTicks,
                    respuestaElegida = respuestaElegida,
                    rachaActual = rachaActual,
                    onResponder = { op -> responder(op) },
                    onBack = onBack
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Single question screen
// ---------------------------------------------------------------------------

@Composable
private fun TriviaQuestion(
    pregunta: TriviaQ,
    numeroPregunta: Int,
    totalPreguntas: Int,
    timerTicks: Int,
    respuestaElegida: Int?,
    rachaActual: Int,
    onResponder: (Int) -> Unit,
    onBack: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val progreso = timerTicks.toFloat() / SEGUNDOS_POR_PREGUNTA
    val timerColor = when {
        timerTicks <= 5  -> Color(0xFFE74C3C)
        timerTicks <= 10 -> NeuralColors.xpGold
        else             -> NeuralColors.electric
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
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
                "TRIVIA DEL BARRIO",
                style = MaterialTheme.typography.labelLarge,
                color = NeuralColors.xpGold,
                letterSpacing = 2.sp
            )
            if (rachaActual > 1) {
                Text(
                    "🔥 ×$rachaActual",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeuralColors.xpGold,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Spacer(Modifier.size(40.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Progress bar (questions)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(nc.glassOverlay)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(numeroPregunta.toFloat() / totalPreguntas)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(NeuralColors.neural)
            )
        }
        Text(
            "$numeroPregunta / $totalPreguntas",
            style = MaterialTheme.typography.labelSmall,
            color = nc.textSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Timer ring
        Box(contentAlignment = Alignment.Center, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Canvas(modifier = Modifier.size(64.dp)) {
                val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                drawArc(color = nc.glassOverlay, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
                drawArc(color = timerColor, startAngle = -90f, sweepAngle = 360f * progreso, useCenter = false, style = stroke)
            }
            Text(
                "$timerTicks",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = timerColor
            )
        }

        Spacer(Modifier.height(20.dp))

        // Question text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(nc.elevated)
                .border(1.dp, nc.glassBorder, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                pregunta.pregunta,
                style = MaterialTheme.typography.titleMedium,
                color = nc.textPrimary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        // Options
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            pregunta.opciones.forEachIndexed { i, opcion ->
                val answered = respuestaElegida != null
                val isChosen = respuestaElegida == i
                val isCorrect = i == pregunta.correcta

                val borderColor = when {
                    !answered -> nc.glassBorder
                    isCorrect -> NeuralColors.success
                    isChosen  -> Color(0xFFE74C3C)
                    else      -> nc.glassBorder
                }
                val bgColor = when {
                    !answered -> nc.elevated
                    isCorrect -> NeuralColors.success.copy(0.15f)
                    isChosen  -> Color(0xFFE74C3C).copy(0.12f)
                    else      -> nc.elevated
                }
                val textColor = when {
                    !answered -> nc.textPrimary
                    isCorrect -> NeuralColors.success
                    isChosen  -> Color(0xFFE74C3C)
                    else      -> nc.textSecondary
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = !answered) { onResponder(i) }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (!answered) nc.glassOverlay else bgColor)
                                .border(1.dp, borderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                listOf("A", "B", "C", "D")[i],
                                style = MaterialTheme.typography.labelMedium,
                                color = borderColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            opcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            fontWeight = if (isChosen || isCorrect) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (answered && isCorrect) {
                            Spacer(Modifier.weight(1f))
                            Text("✓", color = NeuralColors.success, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // XP badge for this question if answered correctly
        if (respuestaElegida == pregunta.correcta) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeuralColors.xpGold.copy(0.15f))
                    .border(1.dp, NeuralColors.xpGold.copy(0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    "+${pregunta.xp} XP ⚡",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeuralColors.xpGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Results screen
// ---------------------------------------------------------------------------

@Composable
private fun TriviaResultados(
    preguntas: List<TriviaQ>,
    correctas: Int,
    xpTotal: Int,
    rachaMaxima: Int,
    onBack: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val pct = (correctas.toFloat() / preguntas.size * 100).toInt()
    val medallaEmoji = when {
        pct == 100 -> "🏆"
        pct >= 80  -> "🥇"
        pct >= 60  -> "🥈"
        pct >= 40  -> "🥉"
        else       -> "📚"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(medallaEmoji, fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Trivia del Barrio",
            style = MaterialTheme.typography.headlineSmall,
            color = nc.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "$correctas / ${preguntas.size} respuestas correctas",
            style = MaterialTheme.typography.titleMedium,
            color = NeuralColors.electric
        )
        Spacer(Modifier.height(24.dp))

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(modifier = Modifier.weight(1f), label = "XP ganados", value = "+$xpTotal", color = NeuralColors.xpGold)
            StatBox(modifier = Modifier.weight(1f), label = "Precisión", value = "$pct%", color = NeuralColors.electric)
            StatBox(modifier = Modifier.weight(1f), label = "Racha max.", value = "×$rachaMaxima", color = NeuralColors.success)
        }

        Spacer(Modifier.height(32.dp))

        if (pct == 100) {
            Text(
                "🌟 ¡Conocés tu barrio como nadie!",
                style = MaterialTheme.typography.bodyMedium,
                color = NeuralColors.xpGold,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NeuralColors.neural)
        ) {
            Text("Volver al mapa", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    val nc = LocalNeuralColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(nc.elevated)
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = nc.textSecondary)
    }
}
