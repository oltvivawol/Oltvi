package com.oltvi.neural.ui.studio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oltvi.neural.data.CategoriaPrenda
import com.oltvi.neural.data.GeneracionStatus
import com.oltvi.neural.theme.EstacionDetector
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import com.oltvi.neural.theme.VawolMaterial
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// VAWOL — Estudio de Creación · Generación de prendas con Vertex AI
// ---------------------------------------------------------------------------

private val COLORES_PALETA = listOf(
    "#FFFFFF", "#000000", "#7B2FBE", "#00D4FF", "#FFB800",
    "#FF6B35", "#27AE60", "#E74C3C", "#1565C0", "#616161"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EstudioCreacionScreen(
    onBack: () -> Unit,
    onModeloAgregado: () -> Unit = {}
) {
    val nc = LocalNeuralColors.current
    val estacion = remember { EstacionDetector.actual }
    val ambientColor = remember { VawolMaterial.ambientFor(estacion) }
    val scope = rememberCoroutineScope()

    var categoriaSeleccionada by remember { mutableStateOf<CategoriaPrenda?>(null) }
    var colorSeleccionado by remember { mutableStateOf(COLORES_PALETA[2]) }  // default: VAWOL purple
    var descripcion by remember { mutableStateOf("") }

    var estado by remember { mutableStateOf<GeneracionStatus?>(null) }

    val puedeGenerar = categoriaSeleccionada != null && descripcion.trim().length >= 5
    val generando = estado?.estado == GeneracionStatus.Estado.GENERANDO
    val listo = estado?.estado == GeneracionStatus.Estado.LISTO

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(nc.deep)
    ) {
        // Season ambient tint overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ambientColor.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "←",
                    color = nc.textSecondary, fontSize = 22.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                        .padding(8.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🔬 LABORATORIO VAWOL",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeuralColors.electric,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "Creá tu prenda con Vertex AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = nc.textSecondary
                    )
                }
                Spacer(Modifier.size(40.dp))
            }
            Spacer(Modifier.height(24.dp))

            AnimatedContent(
                targetState = listo,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "studio_state"
            ) { isListo ->
                if (isListo && estado?.glbUrl != null) {
                    ResultadoPanel(
                        status = estado!!,
                        onAgregarInventario = {
                            onModeloAgregado()
                            // Reset for next generation
                            estado = null
                            categoriaSeleccionada = null
                            descripcion = ""
                        },
                        onNuevaCreacion = { estado = null }
                    )
                } else {
                    CreacionForm(
                        categoriaSeleccionada = categoriaSeleccionada,
                        colorSeleccionado = colorSeleccionado,
                        descripcion = descripcion,
                        estado = estado,
                        generando = generando,
                        puedeGenerar = puedeGenerar,
                        onCategoriaChange = { categoriaSeleccionada = it },
                        onColorChange = { colorSeleccionado = it },
                        onDescripcionChange = { descripcion = it }
                    ) {
                        // TODO: inject repository via ViewModel and call generarModelo3D
                        // For now, show mock generation flow
                        scope.launch {
                            estado = GeneracionStatus(GeneracionStatus.Estado.GENERANDO, 0.2f)
                            kotlinx.coroutines.delay(2000)
                            estado = GeneracionStatus(GeneracionStatus.Estado.GENERANDO, 0.6f)
                            kotlinx.coroutines.delay(2000)
                            estado = GeneracionStatus(
                                GeneracionStatus.Estado.LISTO,
                                progreso = 1f,
                                modelId = "demo_${System.currentTimeMillis()}",
                                glbUrl = "" // placeholder — real URL from Cloud Function
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Creation form
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreacionForm(
    categoriaSeleccionada: CategoriaPrenda?,
    colorSeleccionado: String,
    descripcion: String,
    estado: GeneracionStatus?,
    generando: Boolean,
    puedeGenerar: Boolean,
    onCategoriaChange: (CategoriaPrenda) -> Unit,
    onColorChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onGenerar: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val estacion = remember { EstacionDetector.actual }

    // ── Step 1: Category ──────────────────────────────────────────────────
    Text("1. Elegí la categoría", style = MaterialTheme.typography.titleSmall, color = nc.textPrimary, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CategoriaPrenda.entries.forEach { cat ->
            val selected = categoriaSeleccionada == cat
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) NeuralColors.electric.copy(0.2f) else nc.elevated)
                    .border(1.dp, if (selected) NeuralColors.electric else nc.glassBorder, RoundedCornerShape(10.dp))
                    .clickable { onCategoriaChange(cat) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(cat.icon, fontSize = 22.sp)
                    Text(cat.displayName, style = MaterialTheme.typography.labelSmall, color = if (selected) NeuralColors.electric else nc.textSecondary)
                }
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    // ── Step 2: Color ─────────────────────────────────────────────────────
    Text("2. Elegí el color principal", style = MaterialTheme.typography.titleSmall, color = nc.textPrimary, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        COLORES_PALETA.forEach { hex ->
            val color = Color(android.graphics.Color.parseColor(hex))
            val selected = colorSeleccionado == hex
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (selected) 3.dp else 1.dp,
                        color = if (selected) NeuralColors.electric else color.copy(0.4f),
                        shape = CircleShape
                    )
                    .clickable { onColorChange(hex) }
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    // ── Step 3: Description ───────────────────────────────────────────────
    Text("3. Describí tu prenda", style = MaterialTheme.typography.titleSmall, color = nc.textPrimary, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = descripcion,
        onValueChange = onDescripcionChange,
        placeholder = { Text("Ej: remera con estampado de caña de azúcar y colores del NOA", color = nc.textSecondary) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeuralColors.electric,
            unfocusedBorderColor = nc.glassBorder,
            focusedTextColor = nc.textPrimary,
            unfocusedTextColor = nc.textPrimary,
            cursorColor = NeuralColors.electric
        ),
        minLines = 2,
        maxLines = 4
    )

    Spacer(Modifier.height(20.dp))

    // ── Progress bar (when generating) ────────────────────────────────────
    if (generando && estado != null) {
        GenerandoIndicador(progreso = estado.progreso, estacion = estacion.name)
        Spacer(Modifier.height(16.dp))
    } else if (estado?.estado == GeneracionStatus.Estado.ERROR) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE74C3C).copy(0.12f))
                .padding(12.dp)
        ) {
            Text(
                "⚠️ ${estado.errorMensaje ?: "Error al generar. Intentá nuevamente."}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE74C3C)
            )
        }
        Spacer(Modifier.height(12.dp))
    }

    // ── Generate button ────────────────────────────────────────────────────
    Button(
        onClick = onGenerar,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        enabled = puedeGenerar && !generando,
        colors = ButtonDefaults.buttonColors(
            containerColor = NeuralColors.electric,
            disabledContainerColor = nc.glassBorder
        )
    ) {
        Text(
            if (generando) "⏳ Generando..." else "✨ Crear con IA",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (puedeGenerar && !generando) Color.White else nc.textSecondary
        )
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "Podés generar 1 prenda por hora",
        style = MaterialTheme.typography.labelSmall,
        color = nc.textSecondary,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))
}

// ---------------------------------------------------------------------------
// Generating animation
// ---------------------------------------------------------------------------

@Composable
private fun GenerandoIndicador(progreso: Float, estacion: String) {
    val nc = LocalNeuralColors.current
    val infinite = rememberInfiniteTransition(label = "gen_spin")
    val spin by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "spin"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("✨", fontSize = 22.sp, modifier = Modifier.rotate(spin))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Vertex AI está creando tu prenda…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = nc.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    when ((progreso * 3).toInt()) {
                        0 -> "Refinando descripción con Gemini…"
                        1 -> "Generando vistas con Imagen 3D…"
                        else -> "Convirtiendo a modelo 3D…"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = nc.textSecondary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(nc.glassOverlay)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progreso)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(NeuralColors.electric)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Result panel
// ---------------------------------------------------------------------------

@Composable
private fun ResultadoPanel(
    status: GeneracionStatus,
    onAgregarInventario: () -> Unit,
    onNuevaCreacion: () -> Unit
) {
    val nc = LocalNeuralColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("✅", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "¡Prenda creada con IA!",
            style = MaterialTheme.typography.headlineSmall,
            color = NeuralColors.success,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        // Preview placeholder (SceneView mini-viewer would go here with status.glbUrl)
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(nc.elevated)
                .border(2.dp, NeuralColors.electric.copy(0.4f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎨", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Vista 3D",
                    style = MaterialTheme.typography.labelMedium,
                    color = nc.textSecondary
                )
                Text(
                    "disponible en inventario",
                    style = MaterialTheme.typography.labelSmall,
                    color = nc.textSecondary
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "ID: ${status.modelId?.takeLast(8) ?: "—"}",
            style = MaterialTheme.typography.labelSmall,
            color = nc.textSecondary
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onAgregarInventario,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeuralColors.success)
        ) {
            Text("+ Agregar al inventario", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNuevaCreacion, modifier = Modifier.fillMaxWidth()) {
            Text("Crear otra prenda", color = NeuralColors.electric)
        }
        Spacer(Modifier.height(32.dp))
    }
}
