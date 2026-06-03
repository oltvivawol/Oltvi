package com.oltvi.neural.ui.onboarding

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import com.oltvi.neural.data.NeuralSeedData
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import kotlinx.coroutines.delay
import java.util.UUID

private const val STEP_WELCOME = 0
private const val STEP_NOMBRE = 1
private const val STEP_OBJETIVO = 2
private const val STEP_CLASE = 3
private const val STEP_CONFIRM = 4

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onComplete: (PerfilNeural) -> Unit) {
    val nc = LocalNeuralColors.current
    var step by remember { mutableIntStateOf(STEP_WELCOME) }
    var nombre by remember { mutableStateOf("") }
    var objetivoSeleccionado by remember { mutableStateOf<ObjetivoVida?>(null) }
    var claseSeleccionada by remember { mutableStateOf<ClaseRPG?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(step) {
        visible = false
        delay(100)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(nc.deep, Color(0xFF0A0F22), nc.deep)
                )
            )
    ) {
        // Subtle grid background
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
            ) {
                when (step) {
                    STEP_WELCOME -> StepWelcome(onNext = { step = STEP_NOMBRE })
                    STEP_NOMBRE -> StepNombre(
                        nombre = nombre,
                        onNombreChange = { nombre = it },
                        onNext = { if (nombre.isNotBlank()) step = STEP_OBJETIVO }
                    )
                    STEP_OBJETIVO -> StepObjetivo(
                        selected = objetivoSeleccionado,
                        onSelect = { objetivoSeleccionado = it },
                        onNext = { if (objetivoSeleccionado != null) step = STEP_CLASE }
                    )
                    STEP_CLASE -> StepClase(
                        objetivo = objetivoSeleccionado,
                        selected = claseSeleccionada,
                        onSelect = { claseSeleccionada = it },
                        onNext = { if (claseSeleccionada != null) step = STEP_CONFIRM }
                    )
                    STEP_CONFIRM -> StepConfirm(
                        nombre = nombre,
                        objetivo = objetivoSeleccionado!!,
                        clase = claseSeleccionada!!,
                        onEnterWorld = {
                            onComplete(
                                PerfilNeural(
                                    id = UUID.randomUUID().toString(),
                                    nombre = nombre.trim(),
                                    clase = claseSeleccionada!!,
                                    objetivo = objetivoSeleccionado!!,
                                    logros = NeuralSeedData.logrosIniciales()
                                )
                            )
                        }
                    )
                }
            }
        }

        // Step indicator dots
        if (step > STEP_WELCOME) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..4).forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 20.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (i <= step) nc.neural else nc.elevated)
                    )
                }
            }
        }
    }
}

@Composable
private fun GridBackground() {
    val nc = LocalNeuralColors.current
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 48.dp.toPx()
        val lineColor = nc.glassBorder.copy(alpha = 0.15f)
        var x = 0f
        while (x < size.width) {
            drawLine(lineColor, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), strokeWidth = 0.5f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 0.5f)
            y += step
        }
    }
}

@Composable
private fun StepWelcome(onNext: () -> Unit) {
    val nc = LocalNeuralColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(32.dp))
        // Logo glow
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.radialGradient(listOf(nc.neural.copy(0.4f), nc.deep)))
                .border(1.dp, nc.neural.copy(0.6f), RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🧠", fontSize = 52.sp)
        }
        Spacer(Modifier.height(40.dp))
        Text(
            text = "CAPA NEURAL",
            style = MaterialTheme.typography.displaySmall,
            color = nc.textPrimary,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "La ciudad es el juego.\nVos sos el jugador.\nTu vida real es la misión.",
            style = MaterialTheme.typography.bodyLarge,
            color = nc.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(nc.neural.copy(0.1f))
                .border(1.dp, nc.neural.copy(0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Progresás en el juego progresando en la vida.",
                style = MaterialTheme.typography.bodyMedium,
                color = nc.electric,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(56.dp))
        NeuralButton("Entrar al mundo", onClick = onNext)
    }
}

@Composable
private fun StepNombre(nombre: String, onNombreChange: (String) -> Unit, onNext: () -> Unit) {
    val nc = LocalNeuralColors.current
    val focusManager = LocalFocusManager.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader("Tu nombre", "Así te va a conocer la IA y tu comunidad.")
        Spacer(Modifier.height(40.dp))
        OutlinedTextField(
            value = nombre,
            onValueChange = { if (it.length <= 32) onNombreChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("¿Cómo te llamás?", color = nc.textSecondary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onNext()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = nc.textPrimary,
                unfocusedTextColor = nc.textPrimary,
                focusedBorderColor = nc.neural,
                unfocusedBorderColor = nc.glassBorder,
                cursorColor = nc.electric,
                focusedContainerColor = nc.elevated,
                unfocusedContainerColor = nc.surface
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(32.dp))
        NeuralButton("Continuar", onClick = onNext, enabled = nombre.isNotBlank())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepObjetivo(
    selected: ObjetivoVida?,
    onSelect: (ObjetivoVida) -> Unit,
    onNext: () -> Unit
) {
    val nc = LocalNeuralColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader("¿Qué querés lograr?", "Tu objetivo define tu camino.\nLa IA va a construir el plan.")
        Spacer(Modifier.height(32.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ObjetivoVida.entries.forEach { obj ->
                val isSelected = selected == obj
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) nc.neural.copy(0.25f) else nc.elevated)
                        .border(
                            1.5.dp,
                            if (isSelected) nc.neural else nc.glassBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(obj) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(obj.icon, fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            obj.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) nc.textPrimary else nc.textSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        NeuralButton("Continuar", onClick = onNext, enabled = selected != null)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepClase(
    objetivo: ObjetivoVida?,
    selected: ClaseRPG?,
    onSelect: (ClaseRPG) -> Unit,
    onNext: () -> Unit
) {
    val nc = LocalNeuralColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader("Tu clase", "¿Cómo querés contribuir?\nEsta es tu identidad en la ciudad.")
        Spacer(Modifier.height(32.dp))
        ClaseRPG.entries.forEach { clase ->
            val isSelected = selected == clase
            val claseColor = Color(android.graphics.Color.parseColor(clase.colorHex))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) claseColor.copy(0.15f) else nc.elevated)
                    .border(
                        1.5.dp,
                        if (isSelected) claseColor else nc.glassBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelect(clase) }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(clase.icon, fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            clase.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) claseColor else nc.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            clase.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = nc.textSecondary
                        )
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(claseColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        NeuralButton("Continuar", onClick = onNext, enabled = selected != null)
    }
}

@Composable
private fun StepConfirm(
    nombre: String,
    objetivo: ObjetivoVida,
    clase: ClaseRPG,
    onEnterWorld: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val claseColor = Color(android.graphics.Color.parseColor(clase.colorHex))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Todo listo,", style = MaterialTheme.typography.headlineSmall, color = nc.textSecondary)
        Text(
            nombre,
            style = MaterialTheme.typography.headlineLarge,
            color = nc.textPrimary,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(nc.elevated, nc.surface)))
                .border(1.dp, nc.glassBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(claseColor.copy(0.2f))
                        .border(1.5.dp, claseColor, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) { Text(clase.icon, fontSize = 36.sp) }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Clase ${clase.displayName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = claseColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    clase.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = nc.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(nc.neural.copy(0.1f))
                        .border(1.dp, nc.neural.copy(0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(objetivo.icon, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Objetivo: ${objetivo.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = nc.electric
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "La IA ya conoce tu objetivo.\nTu primera misión te espera en el mundo.",
            style = MaterialTheme.typography.bodyMedium,
            color = nc.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        NeuralButton("Entrar al mundo", onClick = onEnterWorld, glowing = true)
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    val nc = LocalNeuralColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = nc.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = nc.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun NeuralButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    glowing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val nc = LocalNeuralColors.current
    val bgBrush = if (glowing && enabled) {
        Brush.horizontalGradient(listOf(nc.neural, nc.electric.copy(0.8f)))
    } else if (enabled) {
        Brush.horizontalGradient(listOf(nc.neural, nc.neuralLight))
    } else {
        Brush.horizontalGradient(listOf(nc.elevated, nc.elevated))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) Color.White else nc.textSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
