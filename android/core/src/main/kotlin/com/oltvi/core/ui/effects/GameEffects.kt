package com.oltvi.core.ui.effects

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as GSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oltvi.core.theme.OltviColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// =============================================================================
// 1. PulseRing — 3 staggered rings expanding outward with fading alpha
// =============================================================================

@Composable
fun PulseRing(
    modifier: Modifier = Modifier,
    color: Color = OltviColors.action,
    maxRadiusDp: Dp = 60.dp,
    ringCount: Int = 3,
    durationMs: Int = 2000
) {
    val transition = rememberInfiniteTransition(label = "pulse-ring")
    val progresses = (0 until ringCount).map { idx ->
        transition.animateFloat(
            initialValue = idx.toFloat() / ringCount,
            targetValue = 1f + idx.toFloat() / ringCount,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ring-$idx"
        )
    }

    Canvas(modifier = modifier) {
        val maxRadiusPx = maxRadiusDp.toPx()
        val strokePx = 2.dp.toPx()
        progresses.forEach { state ->
            val raw = state.value % 1f
            val radius = maxRadiusPx * raw
            val alpha = (1f - raw).coerceIn(0f, 1f) * 0.7f
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                style = Stroke(width = strokePx)
            )
        }
    }
}

// =============================================================================
// 2. ParticleField — drifting upward particles, wrap to bottom when off-screen
// =============================================================================

private data class Particle(
    val x: Float,        // 0..1
    val y: Float,        // 0..1
    val size: Float,     // px
    val drift: Float,    // normalized per ms
    val alpha: Float
)

@Composable
fun ParticleField(
    modifier: Modifier = Modifier,
    particleCount: Int = 80,
    color: Color = OltviColors.action,
    speed: Float = 0.5f
) {
    val random = remember { Random(42) }
    var particles by remember {
        mutableStateOf(
            List(particleCount) {
                Particle(
                    x = random.nextFloat(),
                    y = random.nextFloat(),
                    size = 1.5f + random.nextFloat() * 3.5f,
                    drift = 0.0003f + random.nextFloat() * 0.0009f,
                    alpha = 0.15f + random.nextFloat() * 0.4f
                )
            }
        )
    }

    LaunchedEffect(particleCount, speed) {
        var last = 0L
        while (true) {
            withFrameMillis { now ->
                val delta = if (last == 0L) 16L else (now - last).coerceAtMost(64L)
                last = now
                particles = particles.map { p ->
                    val newY = p.y - p.drift * delta * speed
                    p.copy(y = if (newY < 0f) 1f else newY)
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            drawCircle(
                color = color.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(p.x * size.width, p.y * size.height)
            )
        }
    }
}

// =============================================================================
// 3. neonGlow — paints a blurred coloured rounded rect behind content
// =============================================================================

fun Modifier.neonGlow(
    color: Color,
    blurRadius: Dp = 20.dp,
    cornerRadius: Dp = 16.dp
): Modifier = this.drawBehind {
    val blurPx = blurRadius.toPx()
    val cornerPx = cornerRadius.toPx()
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = color.toArgb()
        maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
    }
    drawContext.canvas.nativeCanvas.drawRoundRect(
        -blurPx / 2f,
        -blurPx / 2f,
        size.width + blurPx / 2f,
        size.height + blurPx / 2f,
        cornerPx,
        cornerPx,
        paint
    )
}

// =============================================================================
// 4. GlassCard — translucent gradient surface with a thin accent border
// =============================================================================

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = OltviColors.glassBorder,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        OltviColors.glassOverlay,
                        Color(0x08FFFFFF)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.15f))
                ),
                shape = shape
            )
    ) {
        content()
    }
}

// =============================================================================
// 5. WaveformAnimation — animated audio-style bars
// =============================================================================

@Composable
fun WaveformAnimation(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    color: Color = OltviColors.action,
    barCount: Int = 12
) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val heights = (0 until barCount).map { idx ->
        transition.animateFloat(
            initialValue = 6f + (idx % 3) * 4f,
            targetValue = 22f + (idx % 4) * 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 380 + idx * 60,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar-$idx"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { state ->
            val h = if (isActive) state.value else 4f
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.6f), color)
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

// =============================================================================
// 6. shimmer — sweeping highlight brush, useful for skeleton placeholders
// =============================================================================

fun Modifier.shimmer(durationMs: Int = 1500): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer-phase"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.0f),
            Color.White.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.0f)
        ),
        start = Offset(-200f + 1400f * phase, 0f),
        end = Offset(0f + 1400f * phase, 400f)
    )
    this.background(brush)
}

// =============================================================================
// 7. ConfettiExplosion — particles flying outward from a centre point
// =============================================================================

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotation: Float
)

@Composable
fun ConfettiExplosion(
    modifier: Modifier = Modifier,
    center: Offset,
    onDone: () -> Unit,
    particleCount: Int = 40
) {
    val random = remember { Random(System.currentTimeMillis()) }
    val palette = listOf(
        OltviColors.action,
        OltviColors.actionLight,
        OltviColors.success,
        OltviColors.warning
    )

    val particles = remember(particleCount) {
        List(particleCount) {
            ConfettiParticle(
                angle = random.nextFloat() * 360f,
                speed = 280f + random.nextFloat() * 420f,
                size = 6f + random.nextFloat() * 6f,
                color = palette[random.nextInt(palette.size)],
                rotation = random.nextFloat() * 360f
            )
        }
    }

    var elapsed by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        var start = 0L
        while (true) {
            withFrameMillis { now ->
                if (start == 0L) start = now
                elapsed = now - start
            }
            if (elapsed >= 2000L) {
                onDone()
                break
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = (elapsed / 2000f).coerceIn(0f, 1f)
        val gravity = 380f * t * t
        val alpha = (1f - t).coerceIn(0f, 1f)
        particles.forEach { p ->
            val rad = p.angle * PI.toFloat() / 180f
            val travel = p.speed * t
            val px = center.x + cos(rad) * travel
            val py = center.y + sin(rad) * travel + gravity
            drawRect(
                color = p.color.copy(alpha = alpha),
                topLeft = Offset(px - p.size / 2f, py - p.size / 2f),
                size = GSize(p.size, p.size * 0.55f)
            )
        }
    }
}
