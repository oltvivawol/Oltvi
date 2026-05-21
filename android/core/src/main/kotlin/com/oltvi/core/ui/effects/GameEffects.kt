package com.oltvi.core.ui.effects

import android.graphics.BlurMaskFilter
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oltvi.core.theme.OltviColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── 1. PulseRing ──────────────────────────────────────────────────────────

@Composable
fun PulseRing(
    modifier: Modifier = Modifier,
    color: Color = OltviColors.Action,
    radiusDp: Dp = 60.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")

    val progress0 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring0"
    )
    val progress1 by infiniteTransition.animateFloat(
        initialValue = 0.33f,
        targetValue = 1.33f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    val progress2 by infiniteTransition.animateFloat(
        initialValue = 0.66f,
        targetValue = 1.66f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )

    Canvas(modifier = modifier) {
        val maxRadius = radiusDp.toPx()

        fun drawRing(progress: Float) {
            val p = progress % 1f
            val radius = maxRadius * p
            val alpha = (0.6f * (1f - p)).coerceIn(0f, 0.6f)
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        drawRing(progress0)
        drawRing(progress1)
        drawRing(progress2)
    }
}

// ── 2. ParticleField ──────────────────────────────────────────────────────

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

@Composable
fun ParticleField(modifier: Modifier = Modifier) {
    val random = remember { kotlin.random.Random(42) }

    var particles by remember {
        mutableStateOf(
            List(80) {
                Particle(
                    x = random.nextFloat(),
                    y = random.nextFloat(),
                    size = random.nextFloat() * 4f + 2f,   // 2..6
                    speed = random.nextFloat() * 0.0006f + 0.0002f, // 0.0002..0.0008
                    alpha = random.nextFloat() * 0.3f + 0.1f         // 0.1..0.4
                )
            }
        )
    }

    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (true) {
            withFrameMillis { frameTime ->
                val delta = if (lastFrame == 0L) 16L else frameTime - lastFrame
                lastFrame = frameTime
                particles = particles.map { p ->
                    val newY = p.y - p.speed * delta
                    p.copy(y = if (newY < 0f) 1f else newY)
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            drawCircle(
                color = OltviColors.Action.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(p.x * size.width, p.y * size.height)
            )
        }
    }
}

// ── 3. NeonGlow ───────────────────────────────────────────────────────────

fun Modifier.neonGlow(color: Color, blurRadius: Float = 20f): Modifier = this.drawBehind {
    val paint = Paint().apply {
        asFrameworkPaint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.TRANSPARENT
            setShadowLayer(blurRadius, 0f, 0f, color.copy(alpha = 0.7f).toArgb())
            maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        }
        this.color = color.copy(alpha = 0.5f)
    }
    drawContext.canvas.drawRect(
        left = -blurRadius,
        top = -blurRadius,
        right = size.width + blurRadius,
        bottom = size.height + blurRadius,
        paint = paint
    )
}

// ── 4. GlassCard ──────────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .background(OltviColors.GlassWhite, shape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(OltviColors.GlassBorder, Color.Transparent)
                ),
                shape = shape
            )
    ) {
        content()
    }
}

// ── 5. WaveformAnimation ──────────────────────────────────────────────────

@Composable
fun WaveformAnimation(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val barCount = 12

    val heights = List(barCount) { index ->
        val animatedHeight by infiniteTransition.animateFloat(
            initialValue = 4f + (index % 3) * 6f,
            targetValue = 24f + (index % 4) * 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 400 + index * 80,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$index"
        )
        if (isActive) animatedHeight else 4f
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(OltviColors.ActionLight, OltviColors.Action)
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

// ── 6. ShimmerEffect ──────────────────────────────────────────────────────

@Composable
fun ShimmerEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    val surface = OltviColors.Surface
    val shimmerBrush = Brush.linearGradient(
        colorStops = arrayOf(
            (shimmer - 0.3f).coerceIn(0f, 1f) to surface.copy(alpha = 0.6f),
            shimmer.coerceIn(0f, 1f) to surface.copy(alpha = 0.9f),
            (shimmer + 0.3f).coerceIn(0f, 1f) to surface.copy(alpha = 0.6f)
        )
    )

    Box(
        modifier = modifier.background(shimmerBrush, RoundedCornerShape(8.dp))
    )
}

// ── 7. ConfettiExplosion ──────────────────────────────────────────────────

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotation: Float
)

@Composable
fun ConfettiExplosion(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val random = remember { kotlin.random.Random(System.currentTimeMillis()) }
    val confettiColors = listOf(
        OltviColors.Action,
        OltviColors.ActionLight,
        OltviColors.Success,
        OltviColors.Warning
    )

    val particles = remember {
        List(40) {
            ConfettiParticle(
                angle = random.nextFloat() * 360f,
                speed = random.nextFloat() * 400f + 200f,
                size = random.nextFloat() * 6f + 6f,
                color = confettiColors[random.nextInt(confettiColors.size)],
                rotation = random.nextFloat() * 360f
            )
        }
    }

    var elapsed by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        val startTime = withFrameMillis { it }
        while (true) {
            val frameTime = withFrameMillis { it }
            elapsed = frameTime - startTime
            if (elapsed >= 2000L) {
                onComplete()
                break
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val t = (elapsed / 2000f).coerceIn(0f, 1f)
        val alpha = 1f - t

        particles.forEach { p ->
            val angleRad = p.angle * PI.toFloat() / 180f
            val distance = p.speed * t
            val px = centerX + cos(angleRad) * distance
            val py = centerY + sin(angleRad) * distance

            withTransform({
                rotate(degrees = p.rotation + t * 360f, pivot = Offset(px, py))
            }) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(px - p.size / 2f, py - p.size / 2f),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.5f)
                )
            }
        }
    }
}
