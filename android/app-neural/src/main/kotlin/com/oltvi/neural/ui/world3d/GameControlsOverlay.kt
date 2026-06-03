package com.oltvi.neural.ui.world3d

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import kotlin.math.roundToInt
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// Overlay de controles sobre la escena 3D
// ---------------------------------------------------------------------------

@Composable
fun GameControlsOverlay(
    controller: CharacterController,
    accionActual: AccionPersonaje,
    onBack: () -> Unit
) {
    val nc = LocalNeuralColors.current

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Botón volver ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(nc.deep.copy(0.85f))
                .border(1.dp, nc.glassBorder, CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = nc.textSecondary, fontSize = 18.sp)
        }

        // ── Indicador de acción (debug) ────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(nc.deep.copy(0.75f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                accionActual.name,
                style = MaterialTheme.typography.labelSmall,
                color = nc.electric, fontWeight = FontWeight.Bold
            )
        }

        // ── Joystick (izquierda abajo) ────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 28.dp, bottom = 28.dp)
        ) {
            VirtualJoystick(
                radius = 56f,
                onMove = { x, y -> controller.onJoystickMove(x, y) },
                onRelease = { controller.onJoystickMove(0f, 0f) }
            )
        }

        // ── Botones de acción (derecha abajo) ─────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("👊", NeuralColors.electric) { controller.onGolpear() }
                ActionButton("🤜", NeuralColors.neural) { controller.onAgarrar() }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("🦆", nc.textSecondary) { controller.onAgacharse() }
                ActionButton("⬆", NeuralColors.xpGold) { controller.onSaltar() }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Joystick virtual
// ---------------------------------------------------------------------------

@Composable
private fun VirtualJoystick(
    radius: Float,
    onMove: (x: Float, y: Float) -> Unit,
    onRelease: () -> Unit
) {
    val nc = LocalNeuralColors.current
    var thumbX by remember { mutableFloatStateOf(0f) }
    var thumbY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .size((radius * 2).dp)
            .clip(CircleShape)
            .background(nc.deep.copy(0.65f))
            .border(1.dp, nc.glassBorder, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        thumbX = 0f; thumbY = 0f
                        onRelease()
                    },
                    onDragCancel = {
                        thumbX = 0f; thumbY = 0f
                        onRelease()
                    }
                ) { change, dragAmount ->
                    change.consume()
                    thumbX = (thumbX + dragAmount.x).coerceIn(-radius, radius)
                    thumbY = (thumbY + dragAmount.y).coerceIn(-radius, radius)
                    val dist = sqrt(thumbX * thumbX + thumbY * thumbY)
                    if (dist > radius) {
                        val scale = radius / dist
                        thumbX *= scale
                        thumbY *= scale
                    }
                    onMove(thumbX / radius, thumbY / radius)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbX.roundToInt(), thumbY.roundToInt()) }
                .size(32.dp)
                .clip(CircleShape)
                .background(NeuralColors.electric.copy(0.7f))
                .border(1.dp, NeuralColors.electric, CircleShape)
        )
    }
}

// ---------------------------------------------------------------------------
// Botón de acción
// ---------------------------------------------------------------------------

@Composable
private fun ActionButton(icon: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(color.copy(0.2f))
            .border(1.dp, color.copy(0.6f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 20.sp)
    }
}

