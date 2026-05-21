package com.oltvi.usuario.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.data.models.EstadoServicio
import com.oltvi.core.data.models.Servicio
import com.oltvi.core.data.services.MockDataService
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.components.OltviButton
import com.oltvi.core.ui.components.OltviStatusChip
import com.oltvi.core.ui.effects.GlassCard
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PaymentScreen() {
    val mockService = remember { MockDataService() }
    val trips = remember { mockService.getMockTrips() }
    val balance = remember { mockService.getMockBalance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(OltviColors.PrincipalDeep, OltviColors.Surface.copy(alpha = 0.3f))
                )
            )
            .statusBarsPadding()
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CreditCard, contentDescription = null, tint = OltviColors.Action, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pagos", style = OltviTypography.titulo, color = OltviColors.OnSurface)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Balance card ────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(OltviColors.Action, OltviColors.ActionLight, OltviColors.Principal)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Saldo disponible",
                                    style = OltviTypography.etiqueta.copy(color = OltviColors.White.copy(alpha = 0.8f))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$${"%,.0f".format(balance)}",
                                    style = OltviTypography.display.copy(
                                        fontSize = 40.sp,
                                        color = OltviColors.White
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(OltviColors.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.CreditCard,
                                    contentDescription = null,
                                    tint = OltviColors.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Add payment method button
                        OltviButton(
                            text = "Agregar método de pago",
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Filled.Add
                        )
                    }
                }
            }

            // ── Trip history header ─────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Historial de viajes",
                    style = OltviTypography.subtitulo,
                    color = OltviColors.OnSurface
                )
            }

            // ── Trip items ──────────────────────────────────────────────────
            items(trips) { trip ->
                TripHistoryCard(trip = trip)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ── Trip History Card ─────────────────────────────────────────────────────────

@Composable
private fun TripHistoryCard(trip: Servicio) {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        .withZone(ZoneId.systemDefault())
    val dateStr = dateFormatter.format(trip.fechaCreacion)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        Brush.radialGradient(
                            if (trip.estado == EstadoServicio.ENTREGADO)
                                listOf(OltviColors.Success.copy(alpha = 0.2f), OltviColors.Surface)
                            else
                                listOf(OltviColors.Error.copy(alpha = 0.2f), OltviColors.Surface)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = if (trip.estado == EstadoServicio.ENTREGADO) OltviColors.Success else OltviColors.Error,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Trip info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.destino.nombre,
                    style = OltviTypography.cuerpo.copy(fontWeight = FontWeight.Bold),
                    color = OltviColors.OnSurface
                )
                Text(
                    text = "${trip.distanciaKm.let { "%.1f".format(it) }} km · $dateStr",
                    style = OltviTypography.pequeno,
                    color = OltviColors.OnSurfaceDim
                )
                Spacer(modifier = Modifier.height(4.dp))
                OltviStatusChip(estado = trip.estado)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Text(
                text = "$${"%,.0f".format(trip.precio)}",
                style = OltviTypography.subtitulo.copy(
                    fontWeight = FontWeight.Bold,
                    color = OltviColors.OnSurface
                )
            )
        }
    }
}
