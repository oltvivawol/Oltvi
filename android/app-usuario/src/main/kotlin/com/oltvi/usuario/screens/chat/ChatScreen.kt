package com.oltvi.usuario.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oltvi.core.data.models.MensajeChat
import com.oltvi.core.data.models.TipoMensaje
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.components.OltviTextField
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.WaveformAnimation
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiStateFlow
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to latest message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(OltviColors.PrincipalDeep, OltviColors.Surface.copy(alpha = 0.5f))
                )
            )
            .statusBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.radialGradient(listOf(OltviColors.Action, OltviColors.ActionDim)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "O",
                    style = OltviTypography.titulo.copy(color = OltviColors.White, fontWeight = FontWeight.Black)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Olivi",
                    style = OltviTypography.subtitulo.copy(color = OltviColors.OnSurface)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(OltviColors.Success, CircleShape)
                    )
                    Text(
                        "Asistente OLTVI · En línea",
                        style = OltviTypography.etiqueta,
                        color = OltviColors.OnSurfaceDim
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Filled.SmartToy,
                contentDescription = null,
                tint = OltviColors.Action,
                modifier = Modifier.size(24.dp)
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OltviColors.Divider)
        )

        // ── Quick action chips ────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf("Cancelar viaje", "Reporte problema", "Ver factura", "Hablar con humano")
            items(chips) { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(OltviColors.SurfaceVariant)
                        .border(1.dp, OltviColors.Divider, RoundedCornerShape(100.dp))
                        .clickable { viewModel.sendMessage(chip) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(chip, style = OltviTypography.etiqueta, color = OltviColors.OnSurface)
                }
            }
        }

        // ── Message list ──────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.messages, key = { it.id }) { mensaje ->
                ChatBubble(mensaje = mensaje)
            }

            // AI thinking indicator
            if (uiState.isTyping) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(OltviColors.Action, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("O", style = OltviTypography.etiqueta.copy(color = OltviColors.White, fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        GlassCard(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                WaveformAnimation(isActive = true)
                            }
                        }
                    }
                }
            }
        }

        // ── Input bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OltviColors.Surface)
                .border(
                    width = 1.dp,
                    color = OltviColors.Divider,
                    shape = RoundedCornerShape(0.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OltviTextField(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChange,
                placeholder = "Escribí tu consulta...",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (uiState.inputText.isNotBlank())
                            Brush.radialGradient(listOf(OltviColors.Action, OltviColors.ActionLight))
                        else
                            Brush.radialGradient(listOf(OltviColors.SurfaceVariant, OltviColors.SurfaceVariant))
                    )
                    .clickable(enabled = uiState.inputText.isNotBlank()) { viewModel.sendMessage() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (uiState.inputText.isNotBlank()) OltviColors.White else OltviColors.OnSurfaceDim,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Chat Bubble ───────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(mensaje: MensajeChat) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())
    val timeStr = formatter.format(mensaje.timestamp)

    if (mensaje.esIA) {
        // AI message: left-aligned glass card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(OltviColors.Action, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("O", style = OltviTypography.etiqueta.copy(color = OltviColors.White, fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                GlassCard(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = mensaje.contenido,
                            style = OltviTypography.cuerpo,
                            color = OltviColors.OnSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(timeStr, style = OltviTypography.etiqueta, color = OltviColors.OnSurfaceDim)
            }
        }
    } else {
        // User message: right-aligned orange tinted
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(OltviColors.Action.copy(alpha = 0.85f), OltviColors.ActionLight.copy(alpha = 0.9f))
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = mensaje.contenido,
                    style = OltviTypography.cuerpo,
                    color = OltviColors.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(timeStr, style = OltviTypography.etiqueta, color = OltviColors.OnSurfaceDim)
        }
    }
}
