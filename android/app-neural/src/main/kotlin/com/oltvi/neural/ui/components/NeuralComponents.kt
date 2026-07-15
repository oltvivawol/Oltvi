package com.oltvi.neural.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.Mision
import com.oltvi.neural.data.NivelNeural
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors

// ---------------------------------------------------------------------------
// NeuralCard — glass morphism card
// ---------------------------------------------------------------------------

@Composable
fun NeuralCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeuralColors.glassBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val nc = LocalNeuralColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(nc.elevated, nc.surface)
                )
            )
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// XP Progress Bar
// ---------------------------------------------------------------------------

@Composable
fun XpProgressBar(
    xpActual: Int,
    xpSiguiente: Int,
    progreso: Float,
    nivel: NivelNeural,
    modifier: Modifier = Modifier
) {
    val nc = LocalNeuralColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = progreso,
        animationSpec = tween(1000),
        label = "xp_progress"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nivel.displayName.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = nc.electric,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "$xpActual / $xpSiguiente XP",
                style = MaterialTheme.typography.labelSmall,
                color = nc.textSecondary
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = nc.xpGold,
            trackColor = nc.glassOverlay,
            strokeCap = StrokeCap.Round
        )
    }
}

// ---------------------------------------------------------------------------
// AvatarBadge — circular avatar with class icon and level ring
// ---------------------------------------------------------------------------

@Composable
fun AvatarBadge(
    inicial: String,
    clase: ClaseRPG,
    numeroNivel: Int,
    size: Dp = 72.dp,
    modifier: Modifier = Modifier
) {
    val nc = LocalNeuralColors.current
    val claseColor = Color(android.graphics.Color.parseColor(clase.colorHex))
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Glow ring
        Box(
            modifier = Modifier
                .size(size + 10.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(claseColor.copy(alpha = 0.15f))
        )
        // Avatar circle
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(claseColor.copy(0.4f), nc.elevated))
                )
                .border(2.dp, claseColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = inicial,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
        // Level badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(22.dp)
                .clip(CircleShape)
                .background(nc.xpGold)
                .border(2.dp, nc.deep, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$numeroNivel",
                style = MaterialTheme.typography.labelSmall,
                color = nc.deep,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
            )
        }
        // Class icon
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(22.dp)
                .clip(CircleShape)
                .background(nc.elevated)
                .border(1.5.dp, claseColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = clase.icon, fontSize = 11.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// MisionCard
// ---------------------------------------------------------------------------

@Composable
fun MisionCard(
    mision: Mision,
    onComplete: (Mision) -> Unit,
    modifier: Modifier = Modifier
) {
    val nc = LocalNeuralColors.current
    val tipoColor = Color(android.graphics.Color.parseColor(mision.tipo.colorHex))
    val animatedProgress by animateFloatAsState(
        targetValue = if (mision.completada) 1f else mision.progreso,
        animationSpec = tween(800),
        label = "mision_progress"
    )

    NeuralCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = if (mision.completada) nc.success.copy(0.4f) else tipoColor.copy(0.3f),
        onClick = if (!mision.completada) ({ onComplete(mision) }) else null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Type indicator
            Box(
                modifier = Modifier
                    .size(4.dp, 56.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (mision.completada) nc.success else tipoColor)
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mision.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (mision.completada) nc.textSecondary else nc.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "+${mision.xpRecompensa} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (mision.completada) nc.textSecondary else nc.xpGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = mision.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = nc.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (mision.completada) nc.success else tipoColor,
                        trackColor = nc.glassOverlay,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (mision.completada) "Completada" else mision.duracionEstimada,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (mision.completada) nc.success else nc.textSecondary,
                        fontWeight = if (mision.completada) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// StatChip — compact stat display
// ---------------------------------------------------------------------------

@Composable
fun StatChip(
    label: String,
    value: String,
    icon: String,
    color: Color = NeuralColors.electric,
    modifier: Modifier = Modifier
) {
    val nc = LocalNeuralColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(0.08f))
            .border(1.dp, color.copy(0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = nc.textSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// NeuralPulse — animated pulsing dot (for map markers / status)
// ---------------------------------------------------------------------------

@Composable
fun NeuralPulse(
    color: Color = NeuralColors.neural,
    size: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse_alpha"
    )
    val scale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse_scale"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size * 1.8f)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.3f))
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}
