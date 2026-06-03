package com.oltvi.neural.ui.world

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.oltvi.neural.theme.EstacionDetector
import com.oltvi.neural.theme.EstacionVawol
import com.oltvi.neural.theme.ParticleShape
import com.oltvi.neural.theme.VawolParticle
import com.oltvi.neural.theme.generateParticles
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ---------------------------------------------------------------------------
// VAWOL — Season particle overlay for the world map
// 25 particles per season, deterministic seed per season, no jitter per frame
// ---------------------------------------------------------------------------

private const val PARTICLE_COUNT = 25

@Composable
fun WorldSeasonOverlay(
    estacion: EstacionVawol = EstacionDetector.actual,
    modifier: Modifier = Modifier
) {
    val particles = remember(estacion) {
        generateParticles(PARTICLE_COUNT, seed = estacion.ordinal * 1337)
    }

    val infinite = rememberInfiniteTransition(label = "season_particles")
    // Single 0→1 clock that drives all particles (each uses its own speed/drift)
    val clock by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing), RepeatMode.Restart),
        label = "clock"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        particles.forEach { p ->
            // Vertical position: wraps 0→1 each period scaled by p.speed
            val t = (p.seedY + clock * p.speed * (12_000f / 1_000f)) % 1f
            val y = t * (h + 40f) - 20f          // -20 → h+20 (enters/exits screen)
            val x = p.seedX * w + sin((t + p.seedX) * 2 * PI.toFloat()) * p.drift * w

            drawParticle(
                shape = estacion.particleShape,
                color = estacion.particleColor.copy(alpha = p.alpha),
                x = x,
                y = y,
                size = p.size * 10f
            )
        }
    }
}

private fun DrawScope.drawParticle(
    shape: ParticleShape,
    color: Color,
    x: Float,
    y: Float,
    size: Float
) {
    when (shape) {
        ParticleShape.PETALO -> drawPetal(color, x, y, size)
        ParticleShape.DESTELLO -> drawDestello(color, x, y, size)
        ParticleShape.HOJA -> drawHoja(color, x, y, size)
        ParticleShape.NIEVE -> drawNieve(color, x, y, size)
    }
}

// ---------------------------------------------------------------------------
// Particle shapes
// ---------------------------------------------------------------------------

private fun DrawScope.drawPetal(color: Color, x: Float, y: Float, size: Float) {
    // Simple teardrop petal: two arcs
    val path = Path().apply {
        moveTo(x, y - size)
        quadraticBezierTo(x + size * 0.8f, y - size * 0.2f, x, y + size * 0.5f)
        quadraticBezierTo(x - size * 0.8f, y - size * 0.2f, x, y - size)
        close()
    }
    drawPath(path = path, color = color)
    drawPath(path = path, color = color.copy(alpha = color.alpha * 0.4f), style = Stroke(size * 0.12f, cap = StrokeCap.Round))
}

private fun DrawScope.drawDestello(color: Color, x: Float, y: Float, size: Float) {
    // 4-point sparkle: cross + × (like the VAWOL eye sparkle, smaller)
    val arm = size * 0.85f
    val arm2 = arm * 0.55f
    val sw = size * 0.18f
    drawLine(color = color, start = Offset(x - arm, y), end = Offset(x + arm, y), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color = color, start = Offset(x, y - arm * 1.3f), end = Offset(x, y + arm * 0.8f), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color = color.copy(alpha = color.alpha * 0.6f), start = Offset(x - arm2, y - arm2), end = Offset(x + arm2, y + arm2), strokeWidth = sw * 0.7f, cap = StrokeCap.Round)
    drawLine(color = color.copy(alpha = color.alpha * 0.6f), start = Offset(x + arm2, y - arm2), end = Offset(x - arm2, y + arm2), strokeWidth = sw * 0.7f, cap = StrokeCap.Round)
    drawCircle(color = color, radius = sw * 0.8f, center = Offset(x, y))
}

private fun DrawScope.drawHoja(color: Color, x: Float, y: Float, size: Float) {
    // Maple-like leaf: oval with a central vein
    val path = Path().apply {
        moveTo(x, y - size)
        quadraticBezierTo(x + size * 0.7f, y - size * 0.3f, x + size * 0.3f, y + size * 0.6f)
        quadraticBezierTo(x, y + size * 0.9f, x - size * 0.3f, y + size * 0.6f)
        quadraticBezierTo(x - size * 0.7f, y - size * 0.3f, x, y - size)
        close()
    }
    drawPath(path = path, color = color)
    // Vein
    drawLine(
        color = color.copy(alpha = color.alpha * 0.55f),
        start = Offset(x, y - size * 0.8f),
        end = Offset(x, y + size * 0.7f),
        strokeWidth = size * 0.1f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawNieve(color: Color, x: Float, y: Float, size: Float) {
    // Snowflake: 6 arms at 60° intervals + small crossbars
    val arms = 6
    val sw = size * 0.15f
    repeat(arms) { i ->
        val angle = i * (2f * PI.toFloat() / arms)
        val ex = x + cos(angle) * size
        val ey = y + sin(angle) * size
        drawLine(color = color, start = Offset(x, y), end = Offset(ex, ey), strokeWidth = sw, cap = StrokeCap.Round)
        // Mini crossbar at 60% along each arm
        val bx = x + cos(angle) * size * 0.58f
        val by = y + sin(angle) * size * 0.58f
        val bAngle = angle + PI.toFloat() / 2
        val bl = size * 0.28f
        drawLine(
            color = color.copy(alpha = color.alpha * 0.7f),
            start = Offset(bx - cos(bAngle) * bl, by - sin(bAngle) * bl),
            end = Offset(bx + cos(bAngle) * bl, by + sin(bAngle) * bl),
            strokeWidth = sw * 0.8f,
            cap = StrokeCap.Round
        )
    }
    drawCircle(color = color, radius = sw * 0.7f, center = Offset(x, y))
}
