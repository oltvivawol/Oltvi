package com.oltvi.neural.ui.missions

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.oltvi.neural.data.Mision
import com.oltvi.neural.data.NeuralSeedData
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.TipoMision
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import com.oltvi.neural.ui.components.MisionCard
import com.oltvi.neural.ui.components.NeuralCard
import kotlinx.coroutines.delay
import kotlin.math.*

private val ErrorRed = Color(0xFFE74C3C)

@OptIn(ExperimentalMaterial3Api::class)
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
    var misionEnFlujo by remember { mutableStateOf<Mision?>(null) }

    val misionesFiltradas = if (filtroActivo == null) misiones
    else misiones.filter { it.tipo == filtroActivo }

    fun completarMision(mision: Mision, respuesta: String?) {
        val idx = misiones.indexOfFirst { it.id == mision.id }
        if (idx >= 0) {
            misiones[idx] = mision.copy(completada = true, progreso = 1f, respuestaUsuario = respuesta)
            xpTotal += mision.xpRecompensa
            misionesCompletadas++
        }
        misionEnFlujo = null
    }

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

            item {
                AnimatedVisibility(
                    visible = xpTotal > 0,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it }
                ) {
                    Column {
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
            }

            item {
                MisionesSummary(total = misiones.size, completadas = misionesCompletadas)
                Spacer(Modifier.height(20.dp))
            }

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
                    FiltroChip("Todas", filtroActivo == null, nc.electric) { filtroActivo = null }
                    misiones.map { it.tipo }.distinct().forEach { tipo ->
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
                        onComplete = { m -> misionEnFlujo = m }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

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

        misionEnFlujo?.let { mision ->
            MisionFlowSheet(
                mision = mision,
                onCompletar = { respuesta -> completarMision(mision, respuesta) },
                onDismiss = { misionEnFlujo = null }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Flow bottom sheet
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
private fun MisionFlowSheet(
    mision: Mision,
    onCompletar: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val flujo = misionFlujo(mision)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = nc.elevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(nc.glassBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding()
                .navigationBarsPadding()
        ) {
            Text(
                mision.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = nc.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                mision.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = nc.textSecondary
            )
            Spacer(Modifier.height(24.dp))

            when (flujo) {
                is MisionFlujo.Confirmacion -> ConfirmacionFlow(flujo, onCompletar, onDismiss)
                is MisionFlujo.Formulario   -> FormularioFlow(flujo, onCompletar, onDismiss)
                is MisionFlujo.CheckinZona  -> CheckinZonaFlow(flujo, onCompletar, onDismiss)
                is MisionFlujo.Temporizador -> TemporizadorFlow(flujo, onCompletar)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Confirmation flow
// ---------------------------------------------------------------------------

@Composable
private fun ConfirmacionFlow(
    flujo: MisionFlujo.Confirmacion,
    onCompletar: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val nc = LocalNeuralColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(nc.glassOverlay)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            flujo.pregunta,
            style = MaterialTheme.typography.bodyLarge,
            color = nc.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
            Text("No todavía", color = nc.textSecondary)
        }
        Button(
            onClick = { onCompletar(null) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = NeuralColors.success)
        ) {
            Text("✓ Sí, lo hice", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------------------
// Text form flow
// ---------------------------------------------------------------------------

@Composable
private fun FormularioFlow(
    flujo: MisionFlujo.Formulario,
    onCompletar: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val nc = LocalNeuralColors.current
    var texto by remember { mutableStateOf("") }
    val charCount = texto.trim().length
    val habilitado = charCount >= flujo.minChars

    Text(
        flujo.pregunta,
        style = MaterialTheme.typography.bodyLarge,
        color = nc.textPrimary,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = texto,
        onValueChange = { texto = it },
        placeholder = { Text("Escribí tu respuesta aquí...", color = nc.textSecondary) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeuralColors.electric,
            unfocusedBorderColor = nc.glassBorder,
            focusedTextColor = nc.textPrimary,
            unfocusedTextColor = nc.textPrimary,
            cursorColor = NeuralColors.electric
        ),
        minLines = 3
    )
    Spacer(Modifier.height(6.dp))
    Text(
        "$charCount / ${flujo.minChars} caracteres mínimos",
        style = MaterialTheme.typography.labelSmall,
        color = if (habilitado) NeuralColors.success else nc.textSecondary
    )
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
            Text("Cancelar", color = nc.textSecondary)
        }
        Button(
            onClick = { onCompletar(texto.trim()) },
            modifier = Modifier.weight(1f),
            enabled = habilitado,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeuralColors.electric,
                disabledContainerColor = nc.glassBorder
            )
        ) {
            Text(
                "Completar",
                color = if (habilitado) Color.White else nc.textSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// GPS check-in flow
// ---------------------------------------------------------------------------

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CheckinZonaFlow(
    flujo: MisionFlujo.CheckinZona,
    onCompletar: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    var distanciaMetros by remember { mutableStateOf<Double?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val zona = remember(flujo.zonaId) {
        NeuralSeedData.zonasBarrio().find { it.id == flujo.zonaId }
    }

    fun verificarUbicacion() {
        cargando = true
        errorMsg = null
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation
            .addOnSuccessListener { loc ->
                cargando = false
                if (loc != null && zona != null) {
                    distanciaMetros = haversineMetros(
                        loc.latitude, loc.longitude,
                        zona.centro.latitude, zona.centro.longitude
                    )
                } else {
                    errorMsg = "No se pudo obtener tu ubicación. Intentá al aire libre."
                }
            }
            .addOnFailureListener { e ->
                cargando = false
                errorMsg = "Error GPS: ${e.message}"
            }
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) verificarUbicacion()
    }

    val enZona = distanciaMetros != null && zona != null && distanciaMetros!! <= zona.radio

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(nc.glassOverlay)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "📍 Check-in GPS requerido",
                    style = MaterialTheme.typography.titleSmall,
                    color = NeuralColors.electric,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tenés que estar en: ${flujo.zonaNombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = nc.textPrimary
                )
                if (zona != null) {
                    Text(
                        "Radio: ${(zona.radio / 1000).toInt()} km desde el centro",
                        style = MaterialTheme.typography.bodySmall,
                        color = nc.textSecondary
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        when {
            !locationPermission.status.isGranted -> {
                Text(
                    "Esta misión requiere acceso a tu ubicación para verificar que estés en la zona.",
                    style = MaterialTheme.typography.bodySmall,
                    color = nc.textSecondary
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { locationPermission.launchPermissionRequest() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeuralColors.neural)
                ) {
                    Text("Permitir acceso a ubicación", color = Color.White)
                }
            }

            cargando -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📡 Verificando ubicación...", color = nc.textSecondary)
                }
            }

            errorMsg != null -> {
                Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = ErrorRed)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { verificarUbicacion() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeuralColors.neural)
                ) {
                    Text("Reintentar", color = Color.White)
                }
            }

            enZona -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeuralColors.success.copy(0.15f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✅ ¡Estás en ${flujo.zonaNombre}! (${distanciaMetros!!.toInt()} m del centro)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeuralColors.success,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { onCompletar("checkin:${flujo.zonaId}") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeuralColors.success)
                ) {
                    Text("Completar misión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            distanciaMetros != null -> {
                val textoDistancia = if (distanciaMetros!! >= 1000)
                    "%.1f km".format(distanciaMetros!! / 1000)
                else "${distanciaMetros!!.toInt()} m"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorRed.copy(0.12f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "📍 Estás a $textoDistancia de la zona",
                            color = ErrorRed,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (zona != null) {
                            Text(
                                "Necesitás estar dentro de ${(zona.radio / 1000).toInt()} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = nc.textSecondary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { verificarUbicacion() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar ubicación", color = NeuralColors.electric)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Volver", color = nc.textSecondary)
        }
    }
}

// ---------------------------------------------------------------------------
// Countdown timer flow
// ---------------------------------------------------------------------------

@Composable
private fun TemporizadorFlow(
    flujo: MisionFlujo.Temporizador,
    onCompletar: (String?) -> Unit
) {
    val nc = LocalNeuralColors.current
    var restante by remember { mutableIntStateOf(flujo.segundos) }
    val progreso = 1f - restante.toFloat() / flujo.segundos
    val completado = restante == 0

    LaunchedEffect(Unit) {
        while (restante > 0) {
            delay(1000L)
            restante--
        }
        onCompletar(null)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Mantené la actividad durante el tiempo indicado",
            style = MaterialTheme.typography.bodyMedium,
            color = nc.textSecondary
        )
        Spacer(Modifier.height(20.dp))

        val ringColor = if (completado) NeuralColors.success else NeuralColors.electric
        val trackColor = nc.glassOverlay

        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progreso,
                    useCenter = false,
                    style = stroke
                )
            }
            Text(
                text = "%d:%02d".format(restante / 60, restante % 60),
                style = MaterialTheme.typography.headlineMedium,
                color = if (completado) NeuralColors.success else nc.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = if (!completado) "⏳ Quedate presente — no podés cancelar el temporizador"
                   else "✅ ¡Tiempo completado!",
            style = MaterialTheme.typography.bodySmall,
            color = if (!completado) nc.textSecondary else NeuralColors.success
        )
    }
}

// ---------------------------------------------------------------------------
// Utilities
// ---------------------------------------------------------------------------

private fun haversineMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

// ---------------------------------------------------------------------------
// Summary and filter chips
// ---------------------------------------------------------------------------

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
                            Brush.horizontalGradient(listOf(NeuralColors.neural, NeuralColors.electric))
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
