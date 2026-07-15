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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oltvi.neural.data.Accesorio
import com.oltvi.neural.data.AvatarConfig
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.EstiloPelo
import com.oltvi.neural.theme.EstacionDetector
import com.oltvi.neural.theme.EstacionVawol
import com.oltvi.neural.theme.VawolStyle

// ---------------------------------------------------------------------------
// VAWOL — Cel-shading · ink outlines · cross-star eye · season ambient
//
// Rendering rules:
//  · Cel-shading: flat base fill + flat shadow zone + flat highlight spot
//  · Outlines: stroke (outline color, double width) drawn before fill so
//    half bleeds outside = ink-line effect; or enlarged shape before fill
//  · Eyes: 9-layer system; cross-star sparkle = VAWOL signature
//  · Hair shine: white radial gradient oval at crown (specular exception)
//  · Season ambient: full-canvas tint drawn last (screen-space, low alpha)
//  · Multi-wave idle: bob 1400ms + sway 2200ms + breathe scale 3000ms
// ---------------------------------------------------------------------------

@Composable
fun AvatarView(
    config: AvatarConfig,
    clase: ClaseRPG,
    size: Dp = 160.dp,
    animated: Boolean = true,
    showGlow: Boolean = false,
    estacion: EstacionVawol = EstacionDetector.actual,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "avatar_idle")
    val bobY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) -5f else 0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val swayX by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 2.2f else 0f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "sway"
    )
    val breathe by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "breathe"
    )
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = if (showGlow) 0.8f else 0.35f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val claseColor = Color(android.graphics.Color.parseColor(clase.colorHex))
    // breathe mapped to scale range [0.97 , 1.02]; 1.0 when not animated
    val breatheScale = if (animated) 0.97f + breathe * 0.05f else 1f

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val sc = h / 160f   // outline / stroke scale factor
            val bs = config.tipoCuerpo.scaleX

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

            translate(top = bobY * sc, left = swayX * sc) {
                scale(scale = breatheScale, pivot = Offset(cx, h * 0.52f)) {
                    drawGroundShadow(cx, h, bs)
                    drawShoes(cx, h, bs, sc, claseColor)
                    drawLegs(cx, h, bs, sc)
                    drawTorso(cx, h, bs, sc, claseColor)
                    drawArms(cx, h, bs, sc, config.tonoPiel.color, config.tonoPiel.shadowColor, claseColor)
                    drawNeck(cx, h, sc, config.tonoPiel.color, config.tonoPiel.shadowColor)
                    drawHead(cx, h, sc, config.tonoPiel.color, config.tonoPiel.shadowColor)
                    drawHairBack(cx, h, sc, config.estiloPelo, config.colorPelo.color)
                    drawEyes(cx, h, sc, config.colorOjos.color)
                    drawEyebrows(cx, h, sc, config.colorPelo.color)
                    drawMouth(cx, h, sc)
                    drawHairFront(cx, h, sc, config.estiloPelo, config.colorPelo.color)
                    drawAccessory(cx, h, sc, config.accesorio, claseColor)
                    drawClassBadge(cx, h, bs, sc, claseColor)
                }
            }

            // Season ambient tint — screen-space, not affected by character transform
            drawRect(color = estacion.ambientColor)
        }
    }
}

// ---------------------------------------------------------------------------
// Layer draw functions
// ---------------------------------------------------------------------------

private fun DrawScope.drawGroundShadow(cx: Float, h: Float, bs: Float) {
    drawOval(color = Color.Black.copy(0.18f), topLeft = Offset(cx - 38 * bs, h * 0.915f), size = Size(76 * bs, 16f))
    drawOval(color = Color.Black.copy(0.08f), topLeft = Offset(cx - 52 * bs, h * 0.918f), size = Size(104 * bs, 12f))
}

private fun DrawScope.drawShoes(cx: Float, h: Float, bs: Float, sc: Float, accentColor: Color) {
    val y = h * 0.875f; val sw = 18f * bs; val sh = 14f
    val dark = accentColor.darken(0.5f)
    val ol = VawolStyle.OUTLINE_DETALLE * sc * 0.5f
    val outline = VawolStyle.outlineOf(dark)
    for (lx in listOf(cx - 28 * bs, cx + 10 * bs)) {
        drawRoundRect(color = outline, topLeft = Offset(lx - ol, y - ol), size = Size(sw + ol * 2, sh + ol * 2), cornerRadius = CornerRadius(6f))
        drawRoundRect(color = dark, topLeft = Offset(lx, y), size = Size(sw, sh), cornerRadius = CornerRadius(5f))
        // Cel highlight on toe
        drawOval(color = Color.White.copy(0.2f), topLeft = Offset(lx + sw * 0.1f, y + 2f), size = Size(sw * 0.42f, sh * 0.38f))
    }
}

private fun DrawScope.drawLegs(cx: Float, h: Float, bs: Float, sc: Float) {
    val c = Color(0xFF2A2A4A)
    val lw = 16f * bs; val top = h * 0.64f; val lh = h * 0.885f - top
    val ol = VawolStyle.OUTLINE_DETALLE * sc * 0.5f
    val outline = VawolStyle.outlineOf(c)
    val shadow = c.darken(0.15f).copy(0.4f)
    for ((lx, alpha) in listOf(cx - 28 * bs to 1f, cx + 12 * bs to 0.92f)) {
        drawRoundRect(color = outline, topLeft = Offset(lx - ol, top - ol), size = Size(lw + ol * 2, lh + ol * 2), cornerRadius = CornerRadius(6f))
        drawRoundRect(color = c.copy(alpha), topLeft = Offset(lx, top), size = Size(lw, lh), cornerRadius = CornerRadius(5f))
        // Cel shadow on right half of each leg
        drawRoundRect(color = shadow, topLeft = Offset(lx + lw * 0.5f, top + lh * 0.1f), size = Size(lw * 0.5f, lh * 0.82f), cornerRadius = CornerRadius(5f))
    }
}

private fun DrawScope.drawTorso(cx: Float, h: Float, bs: Float, sc: Float, accentColor: Color) {
    val top = h * 0.39f; val sh = h * 0.265f; val tw = 64f * bs
    val ol = VawolStyle.OUTLINE_SILUETA * sc * 0.5f
    // Outline
    drawRoundRect(color = VawolStyle.outlineOf(accentColor), topLeft = Offset(cx - tw / 2 - ol, top - ol), size = Size(tw + ol * 2, sh + ol * 2), cornerRadius = CornerRadius(11f))
    // Flat base
    drawRoundRect(color = accentColor, topLeft = Offset(cx - tw / 2, top), size = Size(tw, sh), cornerRadius = CornerRadius(10f))
    // Cel shadow — right side + bottom
    val shadowPath = Path().apply {
        addRoundRect(RoundRect(Rect(Offset(cx, top + sh * 0.12f), Size(tw / 2, sh * 0.85f)), CornerRadius(10f)))
    }
    drawPath(path = shadowPath, color = VawolStyle.shadowOf(accentColor).copy(0.35f))
    // Cel highlight — top-left
    drawRoundRect(color = accentColor.lighten(0.22f).copy(0.32f), topLeft = Offset(cx - tw / 2 + 4f, top + 4f), size = Size(tw * 0.4f, sh * 0.28f), cornerRadius = CornerRadius(8f))
    // Collar
    val collar = Path().apply {
        moveTo(cx - 10f, top + 2f)
        quadraticBezierTo(cx, top + 14f, cx + 10f, top + 2f)
    }
    drawPath(path = collar, color = accentColor.lighten(0.45f), style = Stroke(2.5f * sc, cap = StrokeCap.Round))
}

private fun DrawScope.drawArms(cx: Float, h: Float, bs: Float, sc: Float, skin: Color, skinDark: Color, accent: Color) {
    val top = h * 0.39f; val armH = h * 0.22f; val aw = 14f * bs
    val ol = VawolStyle.OUTLINE_DETALLE * sc * 0.5f
    val outlineSkin = VawolStyle.outlineOf(skin)
    val outlineAccent = VawolStyle.outlineOf(accent)
    for (armX in listOf(cx - 32 * bs - aw / 2, cx + 32 * bs - aw / 2)) {
        // Sleeve (accent)
        drawRoundRect(color = outlineAccent, topLeft = Offset(armX - ol, top - ol), size = Size(aw + ol * 2, armH * 0.57f), cornerRadius = CornerRadius(8f))
        drawRoundRect(color = accent.darken(0.1f), topLeft = Offset(armX, top), size = Size(aw, armH * 0.55f), cornerRadius = CornerRadius(7f))
        // Forearm (skin)
        drawRoundRect(color = outlineSkin, topLeft = Offset(armX + 1 - ol, top + armH * 0.5f), size = Size(aw - 2 + ol * 2, armH * 0.52f), cornerRadius = CornerRadius(8f))
        drawRoundRect(color = skin, topLeft = Offset(armX + 1, top + armH * 0.52f), size = Size(aw - 2, armH * 0.5f), cornerRadius = CornerRadius(7f))
        // Cel shadow on forearm
        drawRoundRect(color = skinDark.copy(0.3f), topLeft = Offset(armX + aw * 0.5f, top + armH * 0.55f), size = Size(aw * 0.46f, armH * 0.42f), cornerRadius = CornerRadius(7f))
    }
}

private fun DrawScope.drawNeck(cx: Float, h: Float, sc: Float, skin: Color, skinDark: Color) {
    val ol = VawolStyle.OUTLINE_FINO * sc * 0.5f
    drawRoundRect(color = VawolStyle.outlineOf(skin), topLeft = Offset(cx - 9f - ol, h * 0.345f - ol), size = Size(18f + ol * 2, h * 0.065f + ol * 2), cornerRadius = CornerRadius(6f))
    drawRoundRect(color = skin, topLeft = Offset(cx - 9f, h * 0.345f), size = Size(18f, h * 0.065f), cornerRadius = CornerRadius(5f))
    drawRoundRect(color = skinDark.copy(0.28f), topLeft = Offset(cx, h * 0.348f), size = Size(9f, h * 0.059f), cornerRadius = CornerRadius(5f))
}

private fun DrawScope.drawHead(cx: Float, h: Float, sc: Float, skin: Color, skinDark: Color) {
    val r = h * 0.19f; val cy = h * 0.195f
    val ol = VawolStyle.OUTLINE_SILUETA * sc * 0.5f
    val outlineColor = VawolStyle.outlineOf(skin)
    // Head outline
    drawOval(color = outlineColor, topLeft = Offset(cx - r - ol, cy - r - ol), size = Size((r + ol) * 2, (r + ol) * 2))
    // Flat fill
    drawOval(color = skin, topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2))
    // Cel shadow — right side and bottom
    val shadowPath = Path().apply {
        addOval(Rect(Offset(cx + r * 0.04f, cy - r * 0.58f), Size(r * 1.08f, r * 1.48f)))
    }
    drawPath(path = shadowPath, color = VawolStyle.shadowOf(skin).copy(0.3f))
    // Cel highlight — upper-left spot
    drawOval(color = skin.lighten(0.22f).copy(0.36f), topLeft = Offset(cx - r * 0.72f, cy - r * 0.76f), size = Size(r * 0.68f, r * 0.48f))
    // Cheeks
    drawOval(color = Color(0xFFFFB0B0).copy(0.28f), topLeft = Offset(cx - r * 0.78f, cy + r * 0.18f), size = Size(r * 0.5f, r * 0.3f))
    drawOval(color = Color(0xFFFFB0B0).copy(0.28f), topLeft = Offset(cx + r * 0.28f, cy + r * 0.18f), size = Size(r * 0.5f, r * 0.3f))
    // Ears with outline
    drawOval(color = outlineColor, topLeft = Offset(cx - r - 6f, cy - 7f), size = Size(14f, 18f))
    drawOval(color = skin, topLeft = Offset(cx - r - 4f, cy - 5f), size = Size(10f, 14f))
    drawOval(color = skinDark.copy(0.28f), topLeft = Offset(cx - r - 2f, cy - 1f), size = Size(6f, 8f))
    drawOval(color = outlineColor, topLeft = Offset(cx + r - 8f, cy - 7f), size = Size(14f, 18f))
    drawOval(color = skin, topLeft = Offset(cx + r - 6f, cy - 5f), size = Size(10f, 14f))
    drawOval(color = skinDark.copy(0.28f), topLeft = Offset(cx + r - 4f, cy - 1f), size = Size(6f, 8f))
}

private fun DrawScope.drawHairBack(cx: Float, h: Float, sc: Float, estilo: EstiloPelo, hairColor: Color) {
    val r = h * 0.19f; val cy = h * 0.195f
    val dark = hairColor.darken(0.15f)
    val outline = VawolStyle.outlineOf(hairColor)
    val ol = VawolStyle.OUTLINE_DETALLE * sc * 0.5f
    when (estilo) {
        EstiloPelo.LARGO -> {
            for (lx in listOf(cx - r - 10f, cx + r - 4f)) {
                drawRoundRect(color = outline, topLeft = Offset(lx - ol, cy - r * 0.7f - ol), size = Size(14f + ol * 2, r * 2.8f + ol * 2), cornerRadius = CornerRadius(8f))
                drawRoundRect(color = dark, topLeft = Offset(lx, cy - r * 0.7f), size = Size(14f, r * 2.8f), cornerRadius = CornerRadius(7f))
            }
        }
        EstiloPelo.COLA -> {
            drawRoundRect(color = outline, topLeft = Offset(cx + r * 0.5f - ol, cy - r * 0.2f - ol), size = Size(8f + ol * 2, r * 2.0f + ol * 2), cornerRadius = CornerRadius(5f))
            drawRoundRect(color = dark, topLeft = Offset(cx + r * 0.5f, cy - r * 0.2f), size = Size(8f, r * 2.0f), cornerRadius = CornerRadius(4f))
        }
        EstiloPelo.ONDULADO -> {
            for (lx in listOf(cx - r - 8f, cx + r - 4f)) {
                drawRoundRect(color = outline, topLeft = Offset(lx - ol, cy - r * 0.6f - ol), size = Size(12f + ol * 2, r * 2.2f + ol * 2), cornerRadius = CornerRadius(7f))
                drawRoundRect(color = dark, topLeft = Offset(lx, cy - r * 0.6f), size = Size(12f, r * 2.2f), cornerRadius = CornerRadius(6f))
            }
        }
        else -> Unit
    }
}

// ---------------------------------------------------------------------------
// VAWOL signature eyes — 9 layers + cross-star sparkle
// ---------------------------------------------------------------------------

private fun DrawScope.drawEyes(cx: Float, h: Float, sc: Float, eyeColor: Color) {
    val r = h * 0.19f; val cy = h * 0.195f
    val ey = cy + r * 0.05f; val esp = r * 0.42f
    val rx = r * 0.23f; val ry = r * 0.285f
    val ol = VawolStyle.OUTLINE_DETALLE * sc * 0.5f

    listOf(-1f, 1f).forEach { side ->
        val ex = cx + side * esp

        // 1 — outer lower lash arc
        val lashPath = Path().apply {
            moveTo(ex - rx * 1.08f, ey + ry * 0.68f)
            quadraticBezierTo(ex, ey + ry * 1.22f, ex + rx * 1.08f, ey + ry * 0.68f)
        }
        drawPath(path = lashPath, color = Color(0xFF1A1008).copy(0.8f), style = Stroke(2.4f * sc, cap = StrokeCap.Round))

        // 2 — sclera outline (larger oval)
        drawOval(color = Color(0xFF1A1008), topLeft = Offset(ex - rx - ol, ey - ry - ol), size = Size((rx + ol) * 2, (ry + ol) * 2))

        // 3 — sclera white fill
        drawOval(color = Color.White, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2))

        // 4 — upper eyelid shadow in sclera
        val upperShadow = Path().apply {
            addOval(Rect(Offset(ex - rx, ey - ry), Size(rx * 2, ry * 0.62f)))
        }
        drawPath(path = upperShadow, color = Color(0xFF2A1508).copy(0.16f))

        // 5 — iris flat color
        drawOval(color = eyeColor, topLeft = Offset(ex - rx * 0.72f, ey - ry * 0.8f), size = Size(rx * 1.44f, ry * 1.48f))

        // 6 — iris edge darkening ring
        drawOval(color = eyeColor.darken(0.28f), topLeft = Offset(ex - rx * 0.72f, ey - ry * 0.8f), size = Size(rx * 1.44f, ry * 1.48f), style = Stroke(1.6f * sc))

        // 7 — pupil
        drawOval(color = Color(0xFF080808), topLeft = Offset(ex - rx * 0.36f, ey - ry * 0.46f), size = Size(rx * 0.72f, ry * 0.8f))

        // 8 — iris bottom highlight crescent
        val irisHigh = Path().apply {
            moveTo(ex - rx * 0.54f, ey + ry * 0.54f)
            quadraticBezierTo(ex, ey + ry * 0.7f, ex + rx * 0.54f, ey + ry * 0.54f)
        }
        drawPath(path = irisHigh, color = eyeColor.lighten(0.45f).copy(0.55f), style = Stroke(1.8f * sc, cap = StrokeCap.Round))

        // 9 — VAWOL cross-star sparkle (signature)
        val sx = ex - rx * 0.04f
        val sy = ey - ry * 0.32f
        val arm = ry * 0.42f
        val arm2 = arm * 0.62f
        val sw = sc * 1.5f
        // Main + cross
        drawLine(color = Color.White, start = Offset(sx - arm, sy), end = Offset(sx + arm, sy), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color = Color.White, start = Offset(sx, sy - arm * 1.38f), end = Offset(sx, sy + arm * 0.7f), strokeWidth = sw, cap = StrokeCap.Round)
        // Diagonal × (shorter, semi-transparent)
        drawLine(color = Color.White.copy(0.6f), start = Offset(sx - arm2, sy - arm2), end = Offset(sx + arm2, sy + arm2), strokeWidth = sw * 0.75f, cap = StrokeCap.Round)
        drawLine(color = Color.White.copy(0.6f), start = Offset(sx + arm2, sy - arm2), end = Offset(sx - arm2, sy + arm2), strokeWidth = sw * 0.75f, cap = StrokeCap.Round)
        // Center bright oval
        drawOval(color = Color.White, topLeft = Offset(sx - arm * 0.22f, sy - arm * 0.3f), size = Size(arm * 0.44f, arm * 0.58f))
    }

    // Upper eyelash arcs (drawn last so they sit on top)
    listOf(-1f, 1f).forEach { side ->
        val ex = cx + side * esp
        val topLash = Path().apply {
            moveTo(ex - rx * 1.05f, ey - ry * 0.7f)
            quadraticBezierTo(ex, ey - ry * 1.2f, ex + rx * 1.05f, ey - ry * 0.7f)
        }
        drawPath(path = topLash, color = Color(0xFF1A1008), style = Stroke(3.2f * sc, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawEyebrows(cx: Float, h: Float, sc: Float, hairColor: Color) {
    val r = h * 0.19f; val cy = h * 0.195f
    val browY = cy - r * 0.3f
    val bc = hairColor.darken(0.1f).copy(0.9f)
    listOf(-1f, 1f).forEach { side ->
        val bx = cx + side * r * 0.42f
        val path = Path().apply {
            moveTo(bx - r * 0.22f, browY + 1f)
            quadraticBezierTo(bx, browY - 3f, bx + r * 0.22f, browY)
        }
        drawPath(path = path, color = bc, style = Stroke(3.5f * sc, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawMouth(cx: Float, h: Float, sc: Float) {
    val r = h * 0.19f; val cy = h * 0.195f
    val path = Path().apply {
        moveTo(cx - r * 0.18f, cy + r * 0.32f)
        quadraticBezierTo(cx, cy + r * 0.46f, cx + r * 0.18f, cy + r * 0.32f)
    }
    drawPath(path = path, color = Color(0xFFD4606A), style = Stroke(3.0f * sc, cap = StrokeCap.Round))
}

// ---------------------------------------------------------------------------
// Hair front — outline-then-fill + VAWOL shine streak
// ---------------------------------------------------------------------------

private fun DrawScope.drawHairFront(cx: Float, h: Float, sc: Float, estilo: EstiloPelo, hairColor: Color) {
    val r = h * 0.19f; val cy = h * 0.195f
    val outlineStroke = Stroke(VawolStyle.OUTLINE_SILUETA * sc * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val outlineColor = VawolStyle.outlineOf(hairColor)

    val drawHairShape = { p: Path ->
        drawPath(path = p, color = outlineColor, style = outlineStroke)
        drawPath(path = p, color = hairColor)
    }

    when (estilo) {
        EstiloPelo.RAPADO -> {
            drawOval(
                brush = Brush.radialGradient(
                    listOf(hairColor.copy(0.5f), hairColor.copy(0.2f)),
                    center = Offset(cx, cy - r * 0.7f), radius = r
                ),
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
            drawHairShape(p)
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
            drawHairShape(p)
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
            drawHairShape(p)
        }
        EstiloPelo.RIZADO -> {
            listOf(-0.8f, -0.4f, 0f, 0.4f, 0.8f).forEachIndexed { i, xOff ->
                val yOff = if (i % 2 == 0) -0.18f else -0.06f
                val curl = Path().apply {
                    addOval(Rect(Offset(cx + xOff * r - r * 0.35f, cy - r * (1.05f - yOff) - r * 0.3f), Size(r * 0.7f, r * 0.7f)))
                }
                drawHairShape(curl)
            }
            val bang = Path().apply {
                addOval(Rect(Offset(cx - r * 0.3f, cy - r * 1.05f), Size(r * 0.6f, r * 0.5f)))
            }
            drawPath(path = bang, color = outlineColor, style = outlineStroke)
            drawPath(path = bang, color = hairColor.lighten(0.05f))
        }
        EstiloPelo.MOHAWK -> {
            val p = Path().apply {
                moveTo(cx - 8f, cy - r * 0.9f)
                lineTo(cx - 4f, cy - r * 1.8f)
                lineTo(cx, cy - r * 2.05f)
                lineTo(cx + 4f, cy - r * 1.8f)
                lineTo(cx + 8f, cy - r * 0.9f)
                quadraticBezierTo(cx, cy - r * 1.1f, cx - 8f, cy - r * 0.9f)
                close()
            }
            drawHairShape(p)
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
            drawHairShape(p)
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
            drawHairShape(p)
        }
    }

    // VAWOL hair shine streak — white radial oval at crown (all styles except rapado)
    if (estilo != EstiloPelo.RAPADO) {
        drawHairShine(cx, cy, r)
    }
}

private fun DrawScope.drawHairShine(cx: Float, cy: Float, r: Float) {
    drawOval(
        brush = Brush.radialGradient(
            listOf(Color.White.copy(0.55f), Color.White.copy(0.0f)),
            center = Offset(cx - r * 0.14f, cy - r * 0.84f),
            radius = r * 0.42f
        ),
        topLeft = Offset(cx - r * 0.58f, cy - r * 1.08f),
        size = Size(r * 0.88f, r * 0.38f)
    )
}

// ---------------------------------------------------------------------------
// Accessories
// ---------------------------------------------------------------------------

private fun DrawScope.drawAccessory(cx: Float, h: Float, sc: Float, acc: Accesorio, accent: Color) {
    val r = h * 0.19f; val cy = h * 0.195f
    when (acc) {
        Accesorio.NINGUNO -> Unit

        Accesorio.GAFAS -> {
            val ey = cy + r * 0.05f; val esp = r * 0.42f; val rx = r * 0.26f; val ry = r * 0.22f
            val frame = Color(0xFF2A2A3A); val lens = Color(0xAAB0D4F0)
            listOf(-1f, 1f).forEach { s ->
                val ex = cx + s * esp
                drawOval(color = lens, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2))
                drawOval(color = frame, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2), style = Stroke(2.5f * sc))
            }
            drawLine(color = frame, start = Offset(cx - esp + rx, ey), end = Offset(cx + esp - rx, ey), strokeWidth = 2f * sc)
            drawLine(color = frame, start = Offset(cx - esp - rx, ey - ry / 2), end = Offset(cx - esp - rx - r * 0.28f, ey - ry / 3), strokeWidth = 2f * sc, cap = StrokeCap.Round)
            drawLine(color = frame, start = Offset(cx + esp + rx, ey - ry / 2), end = Offset(cx + esp + rx + r * 0.28f, ey - ry / 3), strokeWidth = 2f * sc, cap = StrokeCap.Round)
        }

        Accesorio.GAFAS_SOL -> {
            val ey = cy + r * 0.05f; val esp = r * 0.42f; val rx = r * 0.28f; val ry = r * 0.22f
            val frame = Color(0xFF1A1A1A); val lens = Color(0xCC1A1A2A)
            listOf(-1f, 1f).forEach { s ->
                val ex = cx + s * esp
                drawOval(color = lens, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2))
                drawOval(color = frame, topLeft = Offset(ex - rx, ey - ry), size = Size(rx * 2, ry * 2), style = Stroke(3f * sc))
            }
            drawLine(color = frame, start = Offset(cx - esp + rx, ey), end = Offset(cx + esp - rx, ey), strokeWidth = 2.5f * sc)
        }

        Accesorio.GORRA -> {
            val capTop = cy - r * 1.35f; val capColor = accent.darken(0.3f)
            drawOval(color = capColor, topLeft = Offset(cx - r * 1.25f, cy - r * 0.85f), size = Size(r * 2.5f, r * 0.35f))
            drawArc(color = capColor, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(cx - r * 0.98f, capTop), size = Size(r * 1.96f, r * 1.2f))
            drawLine(color = accent.lighten(0.15f), start = Offset(cx - r, capTop + r * 1.1f), end = Offset(cx + r, capTop + r * 1.1f), strokeWidth = 3f * sc)
            drawOval(color = accent.lighten(0.3f), topLeft = Offset(cx - 4f, capTop + r * 0.45f), size = Size(8f, 8f))
        }

        Accesorio.AURICULARES -> {
            val hpColor = Color(0xFF252535); val pad = accent.darken(0.2f)
            drawArc(
                color = hpColor, startAngle = 195f, sweepAngle = 150f, useCenter = false,
                topLeft = Offset(cx - r * 1.05f, cy - r * 1.25f), size = Size(r * 2.1f, r * 1.1f),
                style = Stroke(5f * sc, cap = StrokeCap.Round)
            )
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
            drawPath(path = p, color = VawolStyle.outlineOf(hoodColor), style = Stroke(VawolStyle.OUTLINE_SILUETA * sc * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path = p, color = hoodColor)
            drawPath(path = p, color = hoodColor.lighten(0.1f).copy(0.22f))
        }

        Accesorio.CORONITA -> {
            val gold = Color(0xFFFFD700); val topY = cy - r * 1.25f
            val p = Path().apply {
                moveTo(cx - r * 0.6f, topY)
                lineTo(cx - r * 0.6f, topY - r * 0.35f)
                lineTo(cx - r * 0.2f, topY - r * 0.15f)
                lineTo(cx, topY - r * 0.5f)
                lineTo(cx + r * 0.2f, topY - r * 0.15f)
                lineTo(cx + r * 0.6f, topY - r * 0.35f)
                lineTo(cx + r * 0.6f, topY)
                close()
            }
            drawPath(path = p, color = VawolStyle.outlineOf(gold), style = Stroke(VawolStyle.OUTLINE_DETALLE * sc * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path = p, brush = Brush.verticalGradient(listOf(Color(0xFFFFF0A0), gold, Color(0xFFB8860B)), startY = topY - r * 0.5f, endY = topY))
            listOf(-0.4f, 0f, 0.4f).forEach { xOff ->
                drawOval(color = Color(0xFFE74C3C), topLeft = Offset(cx + xOff * r - 3f, topY - 5f), size = Size(6f, 6f))
            }
        }
    }
}

private fun DrawScope.drawClassBadge(cx: Float, h: Float, bs: Float, sc: Float, accentColor: Color) {
    val bcy = h * 0.49f; val br = 8f * bs
    drawCircle(color = accentColor.copy(0.25f), radius = br + 3f * sc, center = Offset(cx, bcy))
    drawCircle(color = VawolStyle.outlineOf(accentColor), radius = br + sc, center = Offset(cx, bcy))
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
