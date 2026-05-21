package com.oltvi.usuario.screens.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.oltvi.core.theme.OltviColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders a user-position [Marker] with a soft orange halo. A separate
 * [com.oltvi.core.ui.effects.PulseRing] overlay above the map can be aligned
 * over this marker for the animated pulse effect.
 */
@Composable
fun UserMarker(
    position: LatLng,
    title: String = "Tu ubicación"
) {
    val descriptor = remember { userMarkerBitmap() }
    Marker(
        state = MarkerState(position = position),
        title = title,
        icon = descriptor,
        anchor = Offset(0.5f, 0.5f),
        flat = true,
        zIndex = 2f
    )
}

/**
 * Renders a driver [Marker] with a car emoji on a glass card background and a
 * small bearing arrow pointing in the heading direction. Pass [selected]=true
 * to emphasize the marker with a neon orange glow ring.
 */
@Composable
fun DriverMarker(
    position: LatLng,
    bearingDegrees: Float = 0f,
    title: String,
    snippet: String,
    selected: Boolean = false
) {
    val descriptor = remember(bearingDegrees, selected) {
        driverMarkerBitmap(bearingDegrees, selected)
    }
    Marker(
        state = MarkerState(position = position),
        title = title,
        snippet = snippet,
        icon = descriptor,
        flat = true,
        anchor = Offset(0.5f, 0.5f),
        zIndex = 1f
    )
}

// ── Bitmap rendering helpers ─────────────────────────────────────────────────

private fun userMarkerBitmap(): BitmapDescriptor {
    val sizePx = 96
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val cx = sizePx / 2f
    val cy = sizePx / 2f

    // Outer halo
    val halo = Paint().apply {
        isAntiAlias = true
        color = OltviColors.Action.copy(alpha = 0.18f).toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx / 2f - 2f, halo)

    // Mid ring
    val ring = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = OltviColors.Action.toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx / 3.2f, ring)

    // Inner white core
    val core = Paint().apply {
        isAntiAlias = true
        color = OltviColors.White.toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx / 6f, core)

    // Center accent dot
    val innerDot = Paint().apply {
        isAntiAlias = true
        color = OltviColors.Action.toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx / 10f, innerDot)

    return BitmapDescriptorFactory.fromBitmap(bmp)
}

private fun driverMarkerBitmap(bearingDegrees: Float, selected: Boolean): BitmapDescriptor {
    val sizePx = 88
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val cx = sizePx / 2f
    val cy = sizePx / 2f

    if (selected) {
        val glow = Paint().apply {
            isAntiAlias = true
            color = OltviColors.ActionLight.copy(alpha = 0.32f).toArgb()
        }
        canvas.drawCircle(cx, cy, sizePx / 2f - 2f, glow)
    }

    val bg = Paint().apply {
        isAntiAlias = true
        color = OltviColors.Surface.toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx / 2.6f, bg)

    val border = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = if (selected) 5f else 3f
        color = if (selected) OltviColors.ActionLight.toArgb() else OltviColors.Action.toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx / 2.6f, border)

    // Bearing-direction arrow dot
    val angleRad = Math.toRadians(bearingDegrees.toDouble())
    val arrowRadius = sizePx / 2.2f
    val ax = cx + arrowRadius * sin(angleRad).toFloat()
    val ay = cy - arrowRadius * cos(angleRad).toFloat()
    val arrow = Paint().apply {
        isAntiAlias = true
        color = OltviColors.Action.toArgb()
    }
    canvas.drawCircle(ax, ay, sizePx / 18f, arrow)

    // Car glyph
    val emojiPaint = Paint().apply {
        isAntiAlias = true
        textSize = sizePx * 0.42f
        textAlign = Paint.Align.CENTER
        color = OltviColors.White.toArgb()
        typeface = Typeface.DEFAULT_BOLD
    }
    val bounds = Rect()
    val text = "🚗" // 🚗
    emojiPaint.getTextBounds(text, 0, text.length, bounds)
    val textY = cy - bounds.exactCenterY()
    canvas.drawText(text, cx, textY, emojiPaint)

    return BitmapDescriptorFactory.fromBitmap(bmp)
}
