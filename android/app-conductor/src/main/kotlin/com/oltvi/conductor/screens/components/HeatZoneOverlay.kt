package com.oltvi.conductor.screens.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.oltvi.core.data.models.PuntoGeo

/**
 * Heat-intensity tiers — used to size & saturate the overlay circles.
 * Tier order mirrors typical demand bands: ALTA > MEDIA > BAJA.
 */
enum class IntensidadDemanda(val radiusMeters: Double, val baseAlpha: Float) {
    BAJA(400.0, 0.18f),
    MEDIA(700.0, 0.30f),
    ALTA(1100.0, 0.45f)
}

/**
 * A single hot-zone payload — pairs a geographic point with its demand level.
 */
data class ZonaCaliente(
    val centro: PuntoGeo,
    val intensidad: IntensidadDemanda = IntensidadDemanda.MEDIA
)

/**
 * Renders a list of demand "hot zones" as pulsing translucent orange circles
 * on the map. Each circle slowly breathes between [baseAlpha] and a slightly
 * brighter value to keep the visual alive even when zoomed out.
 *
 * Must be called from inside a [com.google.maps.android.compose.GoogleMap]
 * content lambda.
 */
@Composable
fun HeatZoneOverlay(
    zonas: List<ZonaCaliente>
) {
    val transition = rememberInfiniteTransition(label = "heatzone-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    zonas.forEach { zona ->
        val center = LatLng(zona.centro.lat, zona.centro.lng)
        val baseAlpha = zona.intensidad.baseAlpha
        val animatedAlpha = (baseAlpha + 0.08f * pulse).coerceIn(0f, 1f)

        // Outer halo — wider, softer.
        Circle(
            center = center,
            radius = zona.intensidad.radiusMeters * 1.4,
            fillColor = Color(0xFFE67E22).copy(alpha = animatedAlpha * 0.35f),
            strokeColor = Color.Transparent,
            strokeWidth = 0f
        )
        // Mid ring — main heat circle.
        Circle(
            center = center,
            radius = zona.intensidad.radiusMeters,
            fillColor = Color(0xFFE67E22).copy(alpha = animatedAlpha),
            strokeColor = Color(0xFFF39C12).copy(alpha = 0.6f),
            strokeWidth = 2f
        )
        // Inner core — brighter for ALTA tier.
        Circle(
            center = center,
            radius = zona.intensidad.radiusMeters * 0.45,
            fillColor = Color(0xFFF1C40F).copy(
                alpha = (animatedAlpha * 0.55f).coerceIn(0f, 1f)
            ),
            strokeColor = Color.Transparent,
            strokeWidth = 0f
        )
    }
}
