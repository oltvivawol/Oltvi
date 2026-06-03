package com.oltvi.neural.ui.guide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.neural.ai.AgenteGuia
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.MensajeGuia
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun GuiaScreen(
    perfil: PerfilNeural = PerfilNeural(
        id = "demo",
        nombre = "Jugador",
        clase = ClaseRPG.EXPLORADOR,
        objetivo = ObjetivoVida.TRABAJO
    ),
    agenteGuia: AgenteGuia? = null,
    onBack: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    val mensajes = remember { mutableStateListOf<MensajeGuia>() }
    var inputText by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    // Greeting on first load
    LaunchedEffect(Unit) {
        cargando = true
        val saludo = agenteGuia?.saludar(perfil)
            ?: MensajeGuia(
                id = UUID.randomUUID().toString(),
                contenido = "Buenas, ${perfil.nombre}. Seguimos trabajando en tu objetivo de ${perfil.objetivo.displayName.lowercase()}. ¿Querés que armemos un plan para hoy?",
                esIA = true
            )
        mensajes.add(saludo)
        cargando = false
    }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.lastIndex)
        }
    }

    fun enviar() {
        val texto = inputText.trim()
        if (texto.isBlank() || cargando) return
        inputText = ""
        focusManager.clearFocus()

        mensajes.add(MensajeGuia(UUID.randomUUID().toString(), texto, esIA = false))
        cargando = true

        scope.launch {
            val respuesta = agenteGuia?.responder(texto, perfil)
                ?: MensajeGuia(
                    UUID.randomUUID().toString(),
                    generarRespuestaMock(texto, perfil),
                    esIA = true
                )
            mensajes.add(respuesta)
            cargando = false
        }
    }

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
                .imePadding()
        ) {
            // Header
            GuiaHeader(perfil = perfil, onBack = onBack)

            // Messages
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(mensajes) { msg ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
                    ) {
                        ChatBubble(mensaje = msg)
                    }
                }
                if (cargando) {
                    item { TypingIndicator() }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // Quick suggestions
            if (mensajes.size <= 1) {
                SugerenciasRapidas(perfil = perfil, onSelect = { inputText = it; enviar() })
            }

            // Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(nc.surface)
                    .border(
                        width = 1.dp,
                        color = nc.glassBorder,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Preguntale a la IA...", color = nc.textSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { enviar() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = nc.textPrimary,
                        unfocusedTextColor = nc.textPrimary,
                        focusedBorderColor = nc.neural,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = nc.electric,
                        focusedContainerColor = nc.elevated,
                        unfocusedContainerColor = nc.elevated
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) NeuralColors.neural
                            else nc.elevated
                        )
                        .clickable(enabled = inputText.isNotBlank()) { enviar() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("→", color = if (inputText.isNotBlank()) Color.White else nc.textSecondary, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun GuiaHeader(perfil: PerfilNeural, onBack: () -> Unit) {
    val nc = LocalNeuralColors.current
    val infinite = rememberInfiniteTransition(label = "guia_anim")
    val pulse by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "guia_pulse"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(nc.surface, nc.deep))
            )
            .border(
                width = 1.dp,
                color = nc.glassBorder,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(NeuralColors.neural.copy(0.6f), NeuralColors.deep))
                )
                .border(1.5.dp, NeuralColors.electric.copy(0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🧠", fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "IA GUÍA",
                style = MaterialTheme.typography.labelMedium,
                color = NeuralColors.electric,
                letterSpacing = 1.sp
            )
            Text(
                "Tu camino hacia ${perfil.objetivo.displayName.lowercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = nc.textSecondary
            )
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(NeuralColors.success)
        )
    }
}

@Composable
private fun ChatBubble(mensaje: MensajeGuia) {
    val nc = LocalNeuralColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensaje.esIA) Arrangement.Start else Arrangement.End
    ) {
        if (mensaje.esIA) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NeuralColors.neural.copy(0.3f))
                    .border(1.dp, NeuralColors.neural.copy(0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("🧠", fontSize = 13.sp) }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(
                    RoundedCornerShape(
                        topStart = if (mensaje.esIA) 4.dp else 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = if (mensaje.esIA) 18.dp else 4.dp
                    )
                )
                .background(
                    if (mensaje.esIA) {
                        Brush.verticalGradient(listOf(nc.elevated, nc.surface))
                    } else {
                        Brush.verticalGradient(
                            listOf(NeuralColors.neural, NeuralColors.neural.copy(0.7f))
                        )
                    }
                )
                .border(
                    1.dp,
                    if (mensaje.esIA) nc.glassBorder else NeuralColors.neuralLight.copy(0.3f),
                    RoundedCornerShape(
                        topStart = if (mensaje.esIA) 4.dp else 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = if (mensaje.esIA) 18.dp else 4.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                mensaje.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = if (mensaje.esIA) nc.textPrimary else Color.White,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val nc = LocalNeuralColors.current
    val infinite = rememberInfiniteTransition(label = "typing")
    val offset1 by infinite.animateFloat(0f, -6f, infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse), label = "d1")
    val offset2 by infinite.animateFloat(0f, -6f, infiniteRepeatable(tween(400, 100, LinearEasing), RepeatMode.Reverse), label = "d2")
    val offset3 by infinite.animateFloat(0f, -6f, infiniteRepeatable(tween(400, 200, LinearEasing), RepeatMode.Reverse), label = "d3")

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
            .background(nc.elevated)
            .border(1.dp, nc.glassBorder, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(offset1, offset2, offset3).forEach { off ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(NeuralColors.neural)
                    .padding(bottom = off.dp.coerceAtLeast(0.dp))
            )
        }
    }
}

@Composable
private fun SugerenciasRapidas(perfil: PerfilNeural, onSelect: (String) -> Unit) {
    val nc = LocalNeuralColors.current
    val sugerencias = when (perfil.objetivo) {
        ObjetivoVida.TRABAJO -> listOf("¿Qué puedo hacer hoy?", "¿Cómo mejoro mi CV?", "¿Dónde busco trabajo?")
        ObjetivoVida.NEGOCIO -> listOf("¿Por dónde empiezo mi negocio?", "¿Cómo valido mi idea?", "¿Qué cursos necesito?")
        ObjetivoVida.EDUCACION -> listOf("¿Qué debería aprender?", "¿Cuánto tiempo me lleva?", "¿Hay cursos gratuitos?")
        else -> listOf("¿Cuál es mi próxima misión?", "¿Cómo subo de nivel?", "¿Qué hay de nuevo en mi barrio?")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(nc.deep)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            "Preguntas rápidas:",
            style = MaterialTheme.typography.labelSmall,
            color = nc.textSecondary
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sugerencias.take(3).forEach { sug ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NeuralColors.neural.copy(0.1f))
                        .border(1.dp, NeuralColors.neural.copy(0.3f), RoundedCornerShape(20.dp))
                        .clickable { onSelect(sug) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        sug,
                        style = MaterialTheme.typography.labelSmall,
                        color = nc.textPrimary,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

private fun generarRespuestaMock(input: String, perfil: PerfilNeural): String {
    val lower = input.lowercase()
    return when {
        "hoy" in lower || "hacer" in lower ->
            "Para avanzar hoy: completá una misión de las que tenés pendientes. Cada misión completada te acerca a ${perfil.objetivo.displayName.lowercase()}. Arrancá por la más corta."
        "cv" in lower || "curriculum" in lower ->
            "Un buen CV no tiene que ser perfecto, tiene que ser honesto y claro. Tres cosas que no pueden faltar: lo que sabés hacer, lo que hiciste antes y cómo contactarte. ¿Querés que revisemos tu estructura?"
        "nivel" in lower || "xp" in lower ->
            "Para subir de nivel necesitás completar misiones y ganar XP. Cada acción real cuenta: un curso, un evento, una conexión nueva. El progreso acá refleja el progreso afuera."
        "barrio" in lower || "zona" in lower ->
            "Tu barrio tiene misiones activas esperándote en el mapa. Las zonas con más actividad suelen tener mejores oportunidades. ¿Querés explorar las zonas cercanas?"
        "misión" in lower || "mision" in lower ->
            "Tus misiones pendientes están ordenadas por impacto. La primera que aparece es la que más te conviene completar ahora. ¿Querés detalles de alguna en particular?"
        else ->
            "Buena pregunta. Lo que puedo decirte es que cada paso que das en el mundo real se refleja acá. ¿Querés que armemos un plan concreto para esta semana?"
    }
}
