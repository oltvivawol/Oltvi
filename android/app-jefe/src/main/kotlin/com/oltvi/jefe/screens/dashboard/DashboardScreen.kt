package com.oltvi.jefe.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oltvi.core.data.models.AlertaSeguridad
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.PulseRing
import com.oltvi.core.ui.effects.neonGlow
import com.oltvi.core.ui.effects.shimmer
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * OPERATIONS COMMAND CENTER for OLTVI Mando.
 *
 * Lays out the dashboard as four animated metric tiles at the top, an optional
 * pulsing critical-alert banner, then a scrollable list of AI-generated
 * operations insights from [OltviOrchestrator.generarReporteOperaciones].
 *
 * Auto-refresh is driven entirely by [DashboardViewModel] (every 30 s).
 */
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            currentTime = LocalDateTime.now()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // ── Top glass app bar with brand + clock + sync indicator ────────────
        DashboardHeader(
            time = currentTime,
            isLoading = state.isLoading,
            onRefresh = { viewModel.refresh() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Critical alert banner (red flashing) ─────────────────────────────
        AnimatedVisibility(
            visible = state.alertaCritica != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            state.alertaCritica?.let { CriticalAlertBanner(it) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Four metric cards (2x2 grid for compactness) ─────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Viajes activos",
                value = state.viajesActivos,
                target = state.viajesActivos,
                formatter = { it.toString() },
                icon = Icons.Filled.DirectionsCar,
                accent = OltviColors.Action,
                isLoading = state.isLoading,
                showPulse = true,
                sparkline = listOf(38f, 42f, 41f, 45f, 44f, 46f, state.viajesActivos.toFloat()),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Conductores online",
                value = state.conductoresOnline,
                target = state.conductoresOnline,
                formatter = { it.toString() },
                icon = Icons.Filled.PeopleAlt,
                accent = OltviColors.Success,
                isLoading = state.isLoading,
                showPulse = false,
                sparkline = listOf(118f, 121f, 119f, 122f, 125f, 123f, state.conductoresOnline.toFloat()),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Ingresos hoy",
                value = state.ingresosTotal.toInt(),
                target = state.ingresosTotal.toInt(),
                formatter = { formatMoneyArs(it.toDouble()) },
                icon = Icons.Filled.AttachMoney,
                accent = OltviColors.ActionLight,
                isLoading = state.isLoading,
                showPulse = false,
                sparkline = listOf(180000f, 205000f, 220000f, 240000f, 260000f, 275000f, state.ingresosTotal.toFloat()),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Rating promedio",
                value = (state.ratingPromedio * 10).toInt(),
                target = (state.ratingPromedio * 10).toInt(),
                formatter = { "%.1f".format(it / 10.0) },
                icon = Icons.Filled.Star,
                accent = OltviColors.Warning,
                isLoading = state.isLoading,
                showPulse = false,
                sparkline = listOf(46f, 46f, 47f, 47f, 48f, 47f, (state.ratingPromedio * 10)),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── AI Insights section header ───────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(OltviColors.Action)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Inteligencia operativa",
                style = OltviTypography.subtitulo.copy(
                    color = OltviColors.OnSurface,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${state.insights.size} insights",
                style = OltviTypography.etiqueta.copy(color = OltviColors.OnSurfaceDim)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (state.isLoading && state.insights.isEmpty()) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(OltviColors.Surface.copy(alpha = 0.55f))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(((state.insights.size * 110) + 40).dp.coerceAtMost(900.dp)),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.insights, key = { it.titulo + it.tipo.name }) { insight ->
                    InsightCard(
                        insight = insight,
                        onAccion = { viewModel.tomarAccion(insight) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(
    time: LocalDateTime,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val rotation = rememberInfiniteTransition(label = "syncRot").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1500)),
        label = "rot"
    )
    val fmtDate = remember { DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "AR")) }
    val fmtTime = remember { DateTimeFormatter.ofPattern("HH:mm") }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "OLTVI",
                        style = OltviTypography.titulo.copy(
                            color = OltviColors.Action,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mando",
                        style = OltviTypography.titulo.copy(
                            color = OltviColors.OnSurface,
                            fontWeight = FontWeight.Light
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${fmtDate.format(time).replaceFirstChar { it.uppercase() }} · ${fmtTime.format(time)} hs",
                    style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isLoading) OltviColors.Warning else OltviColors.Success)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLoading) "Sincronizando" else "En línea",
                    style = OltviTypography.etiqueta.copy(
                        color = if (isLoading) OltviColors.Warning else OltviColors.Success
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OltviColors.Action.copy(alpha = 0.15f))
                        .clickable(onClick = onRefresh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refrescar",
                        tint = OltviColors.Action,
                        modifier = Modifier
                            .size(18.dp)
                            .then(if (isLoading) Modifier else Modifier)
                    )
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(
                                    1.5.dp,
                                    Brush.sweepGradient(
                                        listOf(OltviColors.Action, Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )
                        // Intentionally subtle — exact icon rotation handled by the
                        // animated state above to avoid extra layout passes.
                        @Suppress("UNUSED_EXPRESSION") rotation.value
                    }
                }
            }
        }
    }
}

// ── Critical alert banner ─────────────────────────────────────────────────────

@Composable
private fun CriticalAlertBanner(alerta: AlertaSeguridad) {
    val transition = rememberInfiniteTransition(label = "alertPulse")
    val alpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "alertAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .neonGlow(OltviColors.Error.copy(alpha = alpha * 0.75f), blurRadius = 22.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        OltviColors.Error.copy(alpha = 0.30f),
                        OltviColors.Error.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = OltviColors.Error.copy(alpha = alpha),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                PulseRing(
                    modifier = Modifier.size(40.dp),
                    color = OltviColors.Error,
                    maxRadiusDp = 20.dp,
                    ringCount = 2,
                    durationMs = 1100
                )
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = OltviColors.Error,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ALERTA CRÍTICA · ${alerta.tipo}",
                    style = OltviTypography.etiqueta.copy(
                        color = OltviColors.Error,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alerta.descripcion,
                    style = OltviTypography.cuerpo.copy(
                        color = OltviColors.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Acción: ${alerta.accionRecomendada}",
                    style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim),
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Cloud,
                contentDescription = null,
                tint = OltviColors.Error.copy(alpha = alpha),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Utilities ─────────────────────────────────────────────────────────────────

internal fun formatMoneyArs(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "AR"))
    formatter.maximumFractionDigits = 0
    return "$${formatter.format(amount)}"
}
