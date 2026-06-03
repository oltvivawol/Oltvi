package com.oltvi.neural.ui.avatar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oltvi.neural.data.Accesorio
import com.oltvi.neural.data.AvatarConfig
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.EstiloPelo

/**
 * Renders a stylized chibi character avatar using Compose Canvas.
 *
 * All layer drawing uses the standard DrawScope API with named parameters to
 * avoid any overload ambiguity. No private extension functions are used.
 *
 * Layers (bottom to top):
 *  1. Ground glow / shadow
 *  2. Shoes
 *  3. Legs
 *  4. Torso (class colour)
 *  5. Arms
 *  6. Neck
 *  7. Head + ears (skin gradient)
 *  8. Hair back layer
 *  9. Eyes
 * 10. Eyebrows
 * 11. Mouth
 * 12. Hair front layer
 * 13. Accessory
 * 14. Class badge on chest
 */
@Composable
fun AvatarView(
    config: AvatarConfig,
    clase: ClaseRPG,
    size: Dp = 160.dp,
    animated: Boolean = true,
    showGlow: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "avatar_idle")
    val bobY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) -5f else 0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = if (showGlow) 0.8f else 0.35f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val claseColor = Color(android.graphics.Color.parseColor(clase.colorHex))

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val bs = config.tipoCuerpo.scaleX  // body scale

            // Glow ring (fixed, not bobbing)
            if (showGlow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(claseColor.copy(glowAlpha * 0.35f), Color.Transparent),
                        center = Offset(cx, h * 0.93f),
                        radius = w * 0.42f
                    ),
                    radius = w * 0.42f,
                    center = Offset(cx, h * 0.93f)
                )
            }

            translate(top = bobY * (h / 160f)) {
                drawGroundShadow(cx, h, bs)
                drawShoes(cx, h, bs, claseColor)
                drawLegs(cx, h, bs)
                drawTorso(cx, h, bs, claseColor)
                drawArms(cx, h, bs, config.tonoPiel.color, config.tonoPiel.shadowColor, claseColor)
                drawNeck(cx, h, config.tonoPiel.color, config.tonoPiel.shadowColor)
                drawHead(cx, h, config.tonoPiel.color, config.tonoPiel.shadowColor)
                drawHairBack(cx, h, config.estiloPelo, config.colorPelo.color)
                drawEyes(cx, h, config.colorOjos.color)
                drawEyebrows(cx, h, config.colorPelo.color)
                drawMouth(cx, h)
                drawHairFront(cx, h, config.estiloPelo, config.colorPelo.color)
                drawAccessory(cx, h, config.accesorio, claseColor)
                drawClassBadge(cx, h, bs, claseColor)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Layer draw functions — all use standard Compose DrawScope API
// ---------------------------------------------------------------------------

private fun DrawScope.drawGroundShadow(cx: Float, h: Float, bs: Float) {
    drawOval(color = Color.Black.copy(0.18f), topLeft = Offset(cx - 38 * bs, h * 0.915f), size = Size(76 * bs, 16f))
    drawOval(color = Color.Black.copy(0.08f), topLeft = Offset(cx - 52 * bs, h * 0.918f), size = Size(104 * bs, 12f))
}

private fun DrawScope.drawShoes(cx: Float, h: Float, bs: Float, accentColor: Color) {
    val y = h * 0.875f
    val w = 18f * bs
    val sh = 14f
    val dark = accentColor.darken(0.5f)
    val leftPath = Path().apply { addRoundRect(RoundRect(Rect(Offset(cx - 28 * bs, y), Size(w, sh)), CornerRadius(5f))) }
    val rightPath = Path().apply { addRoundRect(RoundRect(Rect(Offset(cx + (28 - w / bs) * bs, y), Size(w, sh)), CornerRadius(5f))) }
    drawPath(path = leftPath, color = dark)
    drawPath(path = rightPath, color = dark)
}

private fun DrawScope.drawLegs(cx: Float, h: Float, bs: Float) {
    val c = Color(0xFF2A2A4A)
    val lw = 16f * bs
    val top = h * 0.64f; val bot = h * 0.885f
    drawRoundRect(color = c, topLeft = Offset(cx - 28 * bs, top), size = Size(lw, bot - top), cornerRadius = CornerRadius(5f))
    drawRoundRect(color = c.copy(0.72f), topLeft = Offset(cx + 12 * bs, top), size = Size(lw, bot - top), cornerRadius = CornerRadius(5f))
}

private fun DrawScope.drawTorso(cx: Float, h: Float, bs: Float, accentColor: Color) {
    val top = h * 0.39f; val sh = h * 0.265f; val tw = 64f * bs
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(accentColor.lighten(0.25f), accentColor, accentColor.darken(0.2f)),
            startY = top, endY = top + sh
        ),
        topLeft = Offset(cx - tw / 2, top), size = Size(tw, sh), cornerRadius = CornerRadius(10f)
    )
    val collar = Path().apply {
        moveTo(cx - 10f, top + 2); quadraticBezierTo(cx, top + 14f, cx + 10f, top + 2)
    }
    drawPath(path = collar, color = accentColor.lighten(0.45f), style = Stroke(2.5f, cap = StrokeCap.Round))
}

private fun DrawScope.drawArms(cx: Float, h: Float, bs: Float, skin: Color, skinDark: Color, accent: Color) {
    val top = h * 0.39f; val armH = h * 0.22f; val aw = 14f * bs
    // Left
    drawRoundRect(color = accent.darken(0.1f), topLeft = Offset(cx - 32 * bs - aw / 2, top), size = Size(aw, armH * 0.55f), cornerRadius = CornerRadius(7f))
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(skin, skinDark), startY = top + armH * 0.52f, endY = top + armH),
        topLeft = Offset(cx - 32 * bs - aw / 2 + 1, top + armH * 0.52f), size = Size(aw - 2, armH * 0.5f), cornerRadius = CornerRadius(7f)
    )
    // Right
    drawRoundRect(color = accent.darken(0.1f), topLeft = Offset(cx + 32 * bs - aw / 2, top), size = Size(aw, armH * 0.55f), cornerRadius = CornerRadius(7f))
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(skin, skinDark), startY = top + armH * 0.52f, endY = top + armH),
        topLeft = Offset(cx + 32 * bs - aw / 2 + 1, top + armH * 0.52f), size = Size(aw - 2, armH * 0.5f), cornerRadius = CornerRadius(7f)
    )
}

private fun DrawScope.drawNeck(cx: Float, h: Float, skin: Color, skinDark: Color) {
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(skin, skinDark), startY = h * 0.345f, endY = h * 0.41f),
        topLeft = Offset(cx - 9f, h * 0.345f), size = Size(18f, h * 0.065f), cornerRadius = CornerRadius(5f)
    )
}

private fun DrawScope.drawHead(cx: Float, h: Float, skin: Color, skinDark: Color) {
    val r = h * 0.185f; val cy = h * 0.195f
    drawOval(color = skinDark.copy(0.3f), topLeft = Offset(cx - r * 0.9f + 3, cy - r * 0.9f + 3), size = Size(r * 1.8f, r * 1.8f))
    drawOval(
        brush = Brush.radialGradient(
            listOf(skin.lighten(0.15f), skin, skinDark.copy(0.6f)),
            center = Offset(cx - r * 0.15f, cy - r * 0.1f), radius = r
        ),
        topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2)
    )
    // Cheeks
    drawOval(color = Color(0xFFFFB0B0).copy(0.25f), topLeft = Offset(cx - r * 0.78f, cy + r * 0.15f), size = Size(r * 0.48f, r * 0.28f))
    drawOval(color = Color(0xFFFFB0B0).copy(0.25f), topLeft = Offset(cx + r * 0.3f, cy + r * 0.15f), size = Size(r * 0.48f, r * 0.28f))
    // Ears
    drawOval(color = skinDark, topLeft = Offset(cx - r - 5f, cy - 6f), size = Size(12f, 16f))
    drawOval(color = skin, topLeft = Offset(cx - r - 3f, cy - 4f), size = Size(8f, 12f))
    drawOval(color = skinDark, topLeft = Offset(cx + r - 7f, cy - 6f), size = Size(12f, 16f))
    drawOval(color = skin, topLeft = Offset(cx + r - 5f, cy - 4f), size = Size(8f, 12f))
}

private fun DrawScope.drawHairBack(cx: Float, h: Float, estilo: EstiloPelo, hairColor: Color) {
    val r = h * 0.185f; val cy = h * 0.195f; val dark = hairColor.darken(0.15f)
    when (estilo) {
        EstiloPelo.LARGO -> {
            val grad = Brush.verticalGradient(listOf(hairColor, dark), startY = cy - r, endY = cy + r * 2.2f)
            drawRoundRect(brush = grad, topLeft = Offset(cx - r - 10, cy - r * 0.7f), size = Size(14f, r * 2.8f), cornerRadius = CornerRadius(7f))
            drawRoundRect(brush = grad, topLeft = Offset(cx + r - 4f, cy - r * 0.7f), size = Size(14f, r * 2.8f), cornerRadius = CornerRadius(7f))
        }
        EstiloPelo.COLA -> {
            drawRoundRect(color = dark, topLeft = Offset(cx + r * 0.5f, cy - r * 0.2f), size = Size(8f, r * 2.0f), cornerRadius = CornerRadius(4f))
        }
        EstiloPelo.ONDULADO -> {
            val grad = Brush.verticalGradient(listOf(hairColor, dark), startY = cy - r, endY = cy + r * 1.5f)
            drawRoundRect(brush = grad, topLeft = Offset(cx - r - 8, cy - r * 0.6f), size = Size(12f, r * 2.2f), cornerRadius = CornerRadius(6f))
            drawRoundRect(brush = grad, topLeft = Offset(cx + r - 4f, cy - r * 0.6f), size = Size(12f, r * 2.2f), cornerRadius = CornerRadius(6f))
        }
        else -> Unit
    }
}

private fun DrawScope.drawEyes(cx: Float, h: Float, eyeColor: Color) {
    val r = h * 0.185f; val cy = h * 0.195f
    val ey = cy + r * 0.05f; val esp = r * 0.42f; val rx = r * 0.22f; val ry = r * 0.26f
    listOf(-1f, 1f).forEach { side ->
        val ex = cx + side * esp
        drawOval(color = Color.White, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2))
        drawOval(color = eyeColor, topLeft = Offset(ex - rx * 0.65f, ey - ry * 0.75f), size = Size(rx * 1.3f, ry * 1.3f))
        drawOval(color = Color(0xFF080808), topLeft = Offset(ex - rx * 0.32f, ey - ry * 0.42f), size = Size(rx * 0.64f, ry * 0.72f))
        drawOval(color = Color.White.copy(0.9f), topLeft = Offset(ex - rx * 0.05f, ey - ry * 0.35f), size = Size(rx * 0.4f, ry * 0.4f))
        drawOval(color = Color(0xFF2A1508).copy(0.5f), topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2), style = Stroke(1.5f))
    }
}

private fun DrawScope.drawEyebrows(cx: Float, h: Float, hairColor: Color) {
    val r = h * 0.185f; val cy = h * 0.195f
    val browY = cy - r * 0.28f; val bc = hairColor.darken(0.1f).copy(0.85f)
    listOf(-1f, 1f).forEach { side ->
        val bx = cx + side * r * 0.42f
        val path = Path().apply {
            moveTo(bx - r * 0.22f, browY + 1)
            quadraticBezierTo(bx, browY - 3f, bx + r * 0.22f, browY)
        }
        drawPath(path = path, color = bc, style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawMouth(cx: Float, h: Float) {
    val r = h * 0.185f; val cy = h * 0.195f
    val path = Path().apply {
        moveTo(cx - r * 0.18f, cy + r * 0.32f)
        quadraticBezierTo(cx, cy + r * 0.45f, cx + r * 0.18f, cy + r * 0.32f)
    }
    drawPath(path = path, color = Color(0xFFD4606A), style = Stroke(2.8f, cap = StrokeCap.Round))
}

private fun DrawScope.drawHairFront(cx: Float, h: Float, estilo: EstiloPelo, hairColor: Color) {
    val r = h * 0.185f; val cy = h * 0.195f
    val light = hairColor.lighten(0.15f); val dark = hairColor.darken(0.1f)

    when (estilo) {
        EstiloPelo.RAPADO -> {
            drawOval(
                brush = Brush.radialGradient(listOf(hairColor.copy(0.5f), hairColor.copy(0.2f)), center = Offset(cx, cy - r * 0.7f), radius = r),
                topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 0.5f)
            )
        }
        EstiloPelo.CORTO -> {
            val p = Path().apply {
                moveTo(cx - r * 1.05f, cy - r * 0.05f)
                quadraticBezierTo(cx - r * 0.8f, cy - r * 1.3f, cx, cy - r * 1.25f)
                quadraticBezierTo(cx + r * 0.8f, cy - r * 1.3f, cx + r * 1.05f, cy - r * 0.05f)
                quadraticBezierTo(cx + r * 0.9f, cy - r * 0.62f, cx, cy - r * 0.55f)
                quadraticBezierTo(cx - r * 0.9f, cy - r * 0.62f, cx - r * 1.05f, cy - r * 0.05f)
                close()
            }
            drawPath(path = p, brush = Brush.radialGradient(listOf(light, hairColor, dark), center = Offset(cx, cy - r * 0.8f), radius = r * 1.2f))
        }
        EstiloPelo.MEDIO -> {
            val p = Path().apply {
                moveTo(cx - r * 1.1f, cy + r * 0.4f)
                lineTo(cx - r * 1.1f, cy - r * 0.15f)
                quadraticBezierTo(cx - r * 0.9f, cy - r * 1.4f, cx, cy - r * 1.35f)
                quadraticBezierTo(cx + r * 0.9f, cy - r * 1.4f, cx + r * 1.1f, cy - r * 0.15f)
                lineTo(cx + r * 1.1f, cy + r * 0.4f)
                quadraticBezierTo(cx + r * 0.9f, cy - r * 0.5f, cx, cy - r * 0.45f)
                quadraticBezierTo(cx - r * 0.9f, cy - r * 0.5f, cx - r * 1.1f, cy + r * 0.4f)
                close()
            }
            drawPath(path = p, brush = Brush.radialGradient(listOf(light, hairColor, dark), center = Offset(cx - r * 0.1f, cy - r * 0.8f), radius = r * 1.4f))
        }
        EstiloPelo.LARGO -> {
            val p = Path().apply {
                moveTo(cx - r * 1.05f, cy + r * 0.5f)
                lineTo(cx - r * 1.1f, cy - r * 0.1f)
                quadraticBezierTo(cx - r * 0.85f, cy - r * 1.35f, cx, cy - r * 1.3f)
                quadraticBezierTo(cx + r * 0.85f, cy - r * 1.35f, cx + r * 1.1f, cy - r * 0.1f)
                lineTo(cx + r * 1.05f, cy + r * 0.5f)
                quadraticBezierTo(cx + r * 0.85f, cy - r * 0.4f, cx, cy - r * 0.4f)
                quadraticBezierTo(cx - r * 0.85f, cy - r * 0.4f, cx - r * 1.05f, cy + r * 0.5f)
                close()
            }
            drawPath(path = p, brush = Brush.radialGradient(listOf(light, hairColor, dark), center = Offset(cx, cy - r), radius = r * 1.4f))
        }
        EstiloPelo.RIZADO -> {
            listOf(-0.8f, -0.4f, 0f, 0.4f, 0.8f).forEachIndexed { i, xOff ->
                val yOff = if (i % 2 == 0) -0.18f else -0.06f
                drawOval(color = hairColor, topLeft = Offset(cx + xOff * r - r * 0.35f, cy - r * (1.05f - yOff) - r * 0.3f), size = Size(r * 0.7f, r * 0.7f))
            }
            // Front bang
            drawOval(color = hairColor.lighten(0.05f), topLeft = Offset(cx - r * 0.3f, cy - r * 1.05f), size = Size(r * 0.6f, r * 0.5f))
        }
        EstiloPelo.MOHAWK -> {
            val p = Path().apply {
                moveTo(cx - 8f, cy - r * 0.9f)
                lineTo(cx - 4f, cy - r * 1.8f); lineTo(cx, cy - r * 2.05f)
                lineTo(cx + 4f, cy - r * 1.8f); lineTo(cx + 8f, cy - r * 0.9f)
                quadraticBezierTo(cx, cy - r * 1.1f, cx - 8f, cy - r * 0.9f)
                close()
            }
            drawPath(path = p, brush = Brush.verticalGradient(listOf(light, hairColor, dark), startY = cy - r * 2.05f, endY = cy - r * 0.9f))
            // Side shaved
            drawRoundRect(color = Color(0xFF1A1A1A).copy(0.15f), topLeft = Offset(cx - r * 0.98f, cy - r * 0.92f), size = Size(r * 0.45f, r * 0.7f), cornerRadius = CornerRadius(4f))
            drawRoundRect(color = Color(0xFF1A1A1A).copy(0.15f), topLeft = Offset(cx + r * 0.52f, cy - r * 0.92f), size = Size(r * 0.45f, r * 0.7f), cornerRadius = CornerRadius(4f))
        }
        EstiloPelo.COLA -> {
            val p = Path().apply {
                moveTo(cx - r * 1.05f, cy + r * 0.3f)
                lineTo(cx - r * 1.05f, cy - r * 0.05f)
                quadraticBezierTo(cx - r * 0.85f, cy - r * 1.3f, cx, cy - r * 1.25f)
                quadraticBezierTo(cx + r * 0.85f, cy - r * 1.3f, cx + r * 1.05f, cy - r * 0.05f)
                lineTo(cx + r * 1.05f, cy + r * 0.3f)
                quadraticBezierTo(cx + r * 0.85f, cy - r * 0.5f, cx, cy - r * 0.45f)
                quadraticBezierTo(cx - r * 0.85f, cy - r * 0.5f, cx - r * 1.05f, cy + r * 0.3f)
                close()
            }
            drawPath(path = p, brush = Brush.radialGradient(listOf(light, hairColor, dark), center = Offset(cx, cy - r), radius = r * 1.3f))
        }
        EstiloPelo.ONDULADO -> {
            val p = Path().apply {
                moveTo(cx - r * 1.1f, cy + r * 0.38f)
                lineTo(cx - r * 1.1f, cy - r * 0.15f)
                quadraticBezierTo(cx - r * 0.6f, cy - r * 1.4f, cx, cy - r * 1.3f)
                quadraticBezierTo(cx + r * 0.6f, cy - r * 1.4f, cx + r * 1.1f, cy - r * 0.15f)
                lineTo(cx + r * 1.1f, cy + r * 0.38f)
                quadraticBezierTo(cx + r * 0.8f, cy + r * 0.1f, cx + r * 0.5f, cy + r * 0.25f)
                quadraticBezierTo(cx + r * 0.2f, cy + r * 0.4f, cx, cy + r * 0.22f)
                quadraticBezierTo(cx - r * 0.2f, cy, cx - r * 0.5f, cy + r * 0.15f)
                quadraticBezierTo(cx - r * 0.8f, cy + r * 0.35f, cx - r * 1.1f, cy + r * 0.38f)
                close()
            }
            drawPath(path = p, brush = Brush.radialGradient(listOf(light, hairColor, dark), center = Offset(cx, cy - r), radius = r * 1.4f))
        }
    }
}

private fun DrawScope.drawAccessory(cx: Float, h: Float, acc: Accesorio, accent: Color) {
    val r = h * 0.185f; val cy = h * 0.195f
    when (acc) {
        Accesorio.NINGUNO -> Unit

        Accesorio.GAFAS -> {
            val ey = cy + r * 0.05f; val esp = r * 0.42f; val rx = r * 0.26f; val ry = r * 0.22f
            val frame = Color(0xFF2A2A3A); val lens = Color(0xAAB0D4F0)
            listOf(-1f, 1f).forEach { s ->
                val ex = cx + s * esp
                drawOval(color = lens, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2))
                drawOval(color = frame, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2), style = Stroke(2.5f))
            }
            drawLine(frame, Offset(cx - esp + rx, ey), Offset(cx + esp - rx, ey), 2f)
            drawLine(frame, Offset(cx - esp - rx, ey - ry / 2), Offset(cx - esp - rx - r * 0.28f, ey - ry / 3), 2f, StrokeCap.Round)
            drawLine(frame, Offset(cx + esp + rx, ey - ry / 2), Offset(cx + esp + rx + r * 0.28f, ey - ry / 3), 2f, StrokeCap.Round)
        }

        Accesorio.GAFAS_SOL -> {
            val ey = cy + r * 0.05f; val esp = r * 0.42f; val rx = r * 0.28f; val ry = r * 0.22f
            val frame = Color(0xFF1A1A1A); val lens = Color(0xCC1A1A2A)
            listOf(-1f, 1f).forEach { s ->
                val ex = cx + s * esp
                drawOval(color = lens, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2))
                drawOval(color = frame, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2), style = Stroke(3f))
            }
            drawLine(frame, Offset(cx - esp + rx, ey), Offset(cx + esp - rx, ey), 2.5f)
        }

        Accesorio.GORRA -> {
            val capTop = cy - r * 1.35f; val capColor = accent.darken(0.3f)
            drawOval(color = capColor, topLeft = Offset(cx - r * 1.25f, cy - r * 0.85f), size = Size(r * 2.5f, r * 0.35f))
            drawArc(color = capColor, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(cx - r * 0.98f, capTop), size = Size(r * 1.96f, r * 1.2f))
            drawLine(accent.lighten(0.15f), Offset(cx - r, capTop + r * 1.1f), Offset(cx + r, capTop + r * 1.1f), 3f)
            drawOval(color = accent.lighten(0.3f), topLeft = Offset(cx - 4f, capTop + r * 0.45f), size = Size(8f, 8f))
        }

        Accesorio.AURICULARES -> {
            val hpColor = Color(0xFF252535); val pad = accent.darken(0.2f)
            drawArc(color = hpColor, startAngle = 195f, sweepAngle = 150f, useCenter = false,
                topLeft = Offset(cx - r * 1.05f, cy - r * 1.25f), size = Size(r * 2.1f, r * 1.1f),
                style = Stroke(5f, cap = StrokeCap.Round))
            drawOval(color = pad, topLeft = Offset(cx - r * 1.12f, cy - r * 0.32f), size = Size(14f, 18f))
            drawOval(color = hpColor, topLeft = Offset(cx - r * 1.1f, cy - r * 0.28f), size = Size(10f, 14f))
            drawOval(color = pad, topLeft = Offset(cx + r * 1.0f, cy - r * 0.32f), size = Size(14f, 18f))
            drawOval(color = hpColor, topLeft = Offset(cx + r * 1.02f, cy - r * 0.28f), size = Size(10f, 14f))
        }

        Accesorio.SOMBRERO -> {
            val hc = Color(0xFF2A1810); val brimY = cy - r * 0.85f
            drawRoundRect(color = hc, topLeft = Offset(cx - r * 1.3f, brimY), size = Size(r * 2.6f, r * 0.25f), cornerRadius = CornerRadius(4f))
            drawRoundRect(color = hc, topLeft = Offset(cx - r * 0.72f, cy - r * 1.65f), size = Size(r * 1.44f, r * 0.85f), cornerRadius = CornerRadius(5f))
            drawRoundRect(color = accent, topLeft = Offset(cx - r * 0.72f, brimY - r * 0.18f), size = Size(r * 1.44f, r * 0.2f), cornerRadius = CornerRadius(3f))
        }

        Accesorio.CAPUCHA -> {
            val hoodColor = accent.darken(0.35f)
            val p = Path().apply {
                moveTo(cx - r * 1.3f, cy + r * 0.5f)
                quadraticBezierTo(cx - r * 1.5f, cy - r * 0.6f, cx - r * 0.7f, cy - r * 1.55f)
                quadraticBezierTo(cx, cy - r * 1.7f, cx + r * 0.7f, cy - r * 1.55f)
                quadraticBezierTo(cx + r * 1.5f, cy - r * 0.6f, cx + r * 1.3f, cy + r * 0.5f)
                quadraticBezierTo(cx + r, cy - r * 0.15f, cx, cy - r * 0.25f)
                quadraticBezierTo(cx - r, cy - r * 0.15f, cx - r * 1.3f, cy + r * 0.5f)
                close()
            }
            drawPath(path = p, brush = Brush.radialGradient(
                listOf(hoodColor.lighten(0.1f), hoodColor, hoodColor.darken(0.2f)),
                center = Offset(cx, cy - r), radius = r * 1.5f
            ))
        }

        Accesorio.CORONITA -> {
            val gold = Color(0xFFFFD700); val topY = cy - r * 1.25f
            val p = Path().apply {
                moveTo(cx - r * 0.6f, topY); lineTo(cx - r * 0.6f, topY - r * 0.35f)
                lineTo(cx - r * 0.2f, topY - r * 0.15f); lineTo(cx, topY - r * 0.5f)
                lineTo(cx + r * 0.2f, topY - r * 0.15f); lineTo(cx + r * 0.6f, topY - r * 0.35f)
                lineTo(cx + r * 0.6f, topY); close()
            }
            drawPath(path = p, brush = Brush.verticalGradient(
                listOf(Color(0xFFFFF0A0), gold, Color(0xFFB8860B)),
                startY = topY - r * 0.5f, endY = topY
            ))
            listOf(-0.4f, 0f, 0.4f).forEach { xOff ->
                drawOval(color = Color(0xFFE74C3C), topLeft = Offset(cx + xOff * r - 3f, topY - 5f), size = Size(6f, 6f))
            }
        }
    }
}

private fun DrawScope.drawClassBadge(cx: Float, h: Float, bs: Float, accentColor: Color) {
    val bcy = h * 0.49f; val br = 8f * bs
    drawCircle(color = accentColor.copy(0.25f), radius = br + 3f, center = Offset(cx, bcy))
    drawCircle(color = accentColor, radius = br, center = Offset(cx, bcy))
    drawCircle(color = Color.White.copy(0.22f), radius = br * 0.58f, center = Offset(cx - br * 0.18f, bcy - br * 0.18f))
}

// ---------------------------------------------------------------------------
// Color utilities
// ---------------------------------------------------------------------------

internal fun Color.lighten(amount: Float) = Color(
    red = (red + amount).coerceIn(0f, 1f),
    green = (green + amount).coerceIn(0f, 1f),
    blue = (blue + amount).coerceIn(0f, 1f),
    alpha = alpha
)

internal fun Color.darken(amount: Float) = Color(
    red = (red - amount).coerceIn(0f, 1f),
    green = (green - amount).coerceIn(0f, 1f),
    blue = (blue - amount).coerceIn(0f, 1f),
    alpha = alpha
)
