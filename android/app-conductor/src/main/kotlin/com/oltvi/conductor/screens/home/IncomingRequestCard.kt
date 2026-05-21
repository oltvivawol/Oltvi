package com.oltvi.conductor.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.data.models.Servicio
import com.oltvi.core.theme.LocalOltviColors
import com.oltvi.core.ui.effects.neonGlow
import kotlinx.coroutines.delay

/**
 * Total countdown (milliseconds) the driver has to accept/reject. Mirrors the
 * 10-second window required by the spec.
 */
private const val COUNTDOWN_MS = 10_000L

/**
 * Slides in from the right as a modal HUD card whenever a new ride request
 * arrives. Shows passenger info, distance to pickup, estimated earnings, and
 * accept / reject CTAs flanked by a countdown ring.
 *
 * When the countdown reaches zero, [onAutoReject] fires so the ViewModel can
 * clear the request and notify the driver.
 *
 * @param servicio       The incoming service request to display.
 * @param distanciaKm    Distance from the driver to pickup, in km.
 * @param ratingPasajero Passenger rating (display only, 1.0–5.0).
 * @param onAccept       Driver accepted — navigate to OperacionScreen.
 * @param onReject       Driver manually rejected.
 * @param onAutoReject   Countdown elapsed — auto-reject.
 */
@Composable
fun IncomingRequestCard(
    servicio: Servicio,
    distanciaKm: Double,
    ratingPasajero: Float,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onAutoReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalOltviColors.current

    // Countdown progress: 1f -> 0f over COUNTDOWN_MS, driven by frame ticks.
    var remaining by remember(servicio.id) { mutableFloatStateOf(1f) }
    var startMs by remember(servicio.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(servicio.id) {
        startMs = 0f
        var triggered = false
        while (remaining > 0f && !triggered) {
            withFrameMillis { now ->
                if (startMs == 0f) startMs = now.toFloat()
                val elapsed = now.toFloat() - startMs
                remaining = (1f - elapsed / COUNTDOWN_MS.toFloat()).coerceIn(0f, 1f)
                if (remaining <= 0f) {
                    triggered = true
                }
            }
        }
        if (triggered) {
            // Yield one frame so the ring renders empty before we dismiss.
            delay(80)
            onAutoReject()
        }
    }

    val secondsLeft = (remaining * (COUNTDOWN_MS / 1000)).toInt()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .neonGlow(color = colors.action.copy(alpha = 0.55f), blurRadius = 28.dp, cornerRadius = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.surfaceMid.copy(alpha = 0.98f),
                        colors.surfaceDark.copy(alpha = 0.99f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(colors.action, colors.actionLight, colors.action)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        // ── Header: countdown ring + "Nueva solicitud" + price badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            CountdownRing(
                progress = remaining,
                secondsLeft = secondsLeft,
                ringColor = colors.action,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NUEVA SOLICITUD",
                    color = colors.action,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = servicio.tipo.displayName + "  ·  " + servicio.nivel.displayName,
                    color = Color(0xFFE8F0F8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            PriceBadge(precio = servicio.precio)
        }

        Spacer(Modifier.height(16.dp))

        // ── Origin row
        OriginDestRow(
            iconColor = colors.success,
            icon = Icons.Filled.LocationOn,
            label = "Recogida",
            place = servicio.origen.nombre.ifBlank { "Origen" },
            detail = servicio.origen.direccion.ifBlank { "${"%.4f".format(servicio.origen.lat)}, ${"%.4f".format(servicio.origen.lng)}" }
        )
        Spacer(Modifier.height(10.dp))
        // ── Destination row
        OriginDestRow(
            iconColor = colors.actionLight,
            icon = Icons.Filled.Place,
            label = "Destino",
            place = servicio.destino.nombre.ifBlank { "Destino" },
            detail = servicio.destino.direccion.ifBlank { "${"%.4f".format(servicio.destino.lat)}, ${"%.4f".format(servicio.destino.lng)}" }
        )

        Spacer(Modifier.height(16.dp))

        // ── Stats row (distance pickup, distance trip, rating)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatPill(
                label = "Al origen",
                value = "${"%.1f".format(distanciaKm)} km"
            )
            StatPill(
                label = "Viaje",
                value = "${"%.1f".format(servicio.distanciaKm)} km"
            )
            StatPill(
                label = "Tiempo",
                value = "${servicio.tiempoEstimadoMin} min"
            )
            StatPill(
                label = "Pasajero",
                value = "%.1f★".format(ratingPasajero)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RejectButton(
                onClick = onReject,
                modifier = Modifier.weight(1f)
            )
            AcceptButton(
                onClick = onAccept,
                modifier = Modifier.weight(1.5f)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Sub-components
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun CountdownRing(
    progress: Float,
    secondsLeft: Int,
    ringColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalOltviColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "ring-progress"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.size(56.dp)) {
            val stroke = 5.dp.toPx()
            // Track
            drawArc(
                color = colors.glassBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = androidx.compose.ui.geometry.Size(
                    size.width - stroke,
                    size.height - stroke
                ),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // Progress arc (counter-clockwise as the timer drains).
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = androidx.compose.ui.geometry.Size(
                    size.width - stroke,
                    size.height - stroke
                ),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = secondsLeft.coerceAtLeast(0).toString(),
            color = ringColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun PriceBadge(precio: Double) {
    val colors = LocalOltviColors.current
    Column(
        horizontalAlignment = Alignment.End
    ) {
        Text(
            "Ganarás",
            color = Color(0xFF8BA4BB),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            "$" + "%.0f".format(precio),
            color = colors.success,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun OriginDestRow(
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    place: String,
    detail: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconColor.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color(0xFF8BA4BB), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(place, color = Color(0xFFE8F0F8), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(detail, color = Color(0xFF8BA4BB), fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFF8BA4BB), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Text(value, color = Color(0xFFE8F0F8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AcceptButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalOltviColors.current
    var pressed by remember { mutableFloatStateOf(1f) }
    val scale by animateFloatAsState(targetValue = pressed, label = "accept-press")
    Box(
        modifier = modifier
            .scale(scale)
            .height(54.dp)
            .neonGlow(colors.success, blurRadius = 18.dp, cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(listOf(colors.success, Color(0xFF2ECC71)))
            )
            .clickable {
                pressed = 0.96f
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("ACEPTAR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun RejectButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalOltviColors.current
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceDark.copy(alpha = 0.85f))
            .border(1.dp, colors.error.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Close, contentDescription = null, tint = colors.error)
            Spacer(Modifier.width(6.dp))
            Text("Rechazar", color = colors.error, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

/** Tiny passenger badge shown above the card on the HUD when needed. */
@Composable
fun PassengerBadge(nombre: String, rating: Float) {
    val colors = LocalOltviColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceMid.copy(alpha = 0.85f))
            .border(1.dp, colors.glassBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(Icons.Filled.Person, contentDescription = null, tint = colors.action, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(nombre, color = Color(0xFFE8F0F8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Filled.Star, contentDescription = null, tint = colors.warning, modifier = Modifier.size(12.dp))
        Text("%.1f".format(rating), color = colors.warning, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
