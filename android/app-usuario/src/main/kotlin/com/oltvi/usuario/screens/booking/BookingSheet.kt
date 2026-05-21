package com.oltvi.usuario.screens.booking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.data.models.NivelServicio
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.ResultadoPrecio
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.components.OltviButton
import com.oltvi.core.ui.components.OltviButtonVariant
import com.oltvi.core.ui.components.OltviDriverCard
import com.oltvi.core.ui.components.OltviTextField
import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.ui.effects.ConfettiExplosion
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.ShimmerEffect
import com.oltvi.usuario.screens.home.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSheet(
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onDestinoSelected: (PuntoGeo) -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchText by remember { mutableStateOf("") }
    var selectedNivel by remember { mutableStateOf(NivelServicio.ESTANDAR) }
    var showConfetti by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OltviColors.Surface,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .background(OltviColors.Action.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
            ) {
                // Sheet header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Planifica tu viaje",
                        style = OltviTypography.titulo,
                        color = OltviColors.OnSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = OltviColors.OnSurfaceDim)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Destination search bar ──────────────────────────────────
                OltviTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = "¿A dónde vas?",
                    leadingIcon = {
                        Icon(Icons.Filled.Search, null, tint = OltviColors.OnSurfaceDim, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Quick destination chips ─────────────────────────────────
                Text(
                    text = "Destinos frecuentes",
                    style = OltviTypography.etiqueta,
                    color = OltviColors.OnSurfaceDim
                )
                Spacer(modifier = Modifier.height(8.dp))

                val quickDestinations = listOf(
                    Triple("Trabajo", PuntoGeo(-34.5960, -58.3904, "Trabajo", "Av. Corrientes 1234"), Icons.Filled.LocationOn),
                    Triple("Casa", PuntoGeo(-34.6037, -58.3816, "Casa", "Av. de Mayo 567"), Icons.Filled.LocationOn),
                    Triple("Aeropuerto", PuntoGeo(-34.5592, -58.4156, "Aeropuerto", "Aeroparque"), Icons.Filled.LocationOn)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickDestinations.forEach { (label, destino, _) ->
                        val isSelected = uiState.destino?.nombre == label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (isSelected) OltviColors.Action.copy(alpha = 0.15f)
                                    else OltviColors.SurfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) OltviColors.Action else OltviColors.Divider,
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable { onDestinoSelected(destino) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = OltviTypography.etiqueta,
                                color = if (isSelected) OltviColors.Action else OltviColors.OnSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Service level selector ──────────────────────────────────
                Text(
                    text = "Tipo de servicio",
                    style = OltviTypography.etiqueta,
                    color = OltviColors.OnSurfaceDim
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(NivelServicio.ESTANDAR, NivelServicio.EXPRES, NivelServicio.ESPECIAL).forEach { nivel ->
                        val isSelected = selectedNivel == nivel
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) OltviColors.Action
                                    else OltviColors.SurfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) OltviColors.ActionLight else OltviColors.Divider,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedNivel = nivel }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = nivel.displayName,
                                    style = OltviTypography.hud.copy(fontSize = 11.sp),
                                    color = if (isSelected) OltviColors.White else OltviColors.OnSurface
                                )
                                Text(
                                    text = "×${nivel.priceMultiplier}",
                                    style = OltviTypography.etiqueta,
                                    color = if (isSelected) OltviColors.White.copy(0.8f) else OltviColors.OnSurfaceDim
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── AI Price Card ───────────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.destino != null,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                ) {
                    Column {
                        if (uiState.isLoading) {
                            // Shimmer placeholders
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShimmerEffect(modifier = Modifier.fillMaxWidth().height(100.dp))
                                ShimmerEffect(modifier = Modifier.fillMaxWidth().height(120.dp))
                            }
                        } else {
                            uiState.solicitudResultado?.let { resultado ->
                                PriceCard(precio = resultado.precio)
                                Spacer(modifier = Modifier.height(12.dp))
                                DriverResultCard(
                                    conductor = resultado.matchmaking.conductorElegido,
                                    explicacion = resultado.matchmaking.explicacionIA
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Confirm button ──────────────────────────────────────────
                val canConfirm = uiState.destino != null && uiState.solicitudResultado != null && !uiState.isLoading

                OltviButton(
                    text = "Confirmar viaje",
                    onClick = {
                        if (canConfirm) {
                            showConfetti = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canConfirm,
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                OltviButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OltviButtonVariant.Ghost
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Confetti explosion overlay ──────────────────────────────────
            if (showConfetti) {
                ConfettiExplosion(
                    onComplete = {
                        showConfetti = false
                        onConfirm()
                    },
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

// ── Price Card ────────────────────────────────────────────────────────────────

@Composable
private fun PriceCard(precio: ResultadoPrecio) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Precio estimado",
                        style = OltviTypography.etiqueta,
                        color = OltviColors.OnSurfaceDim
                    )
                    Text(
                        text = "$${"%,.0f".format(precio.precioFinal)}",
                        style = OltviTypography.display.copy(fontSize = 32.sp),
                        color = OltviColors.OnSurface
                    )
                }

                // Demand factor chip
                val factorColor = when {
                    precio.factorDemanda < 1.2 -> OltviColors.Success
                    precio.factorDemanda < 1.8 -> OltviColors.Warning
                    else -> OltviColors.Error
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(factorColor.copy(alpha = 0.15f))
                        .border(1.dp, factorColor.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "×${"%.1f".format(precio.factorDemanda)}",
                        style = OltviTypography.hud,
                        color = factorColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price breakdown
            precio.desglose.forEach { (label, amount) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = OltviTypography.pequeno,
                        color = OltviColors.OnSurfaceDim
                    )
                    Text(
                        text = "$${"%,.0f".format(amount)}",
                        style = OltviTypography.pequeno,
                        color = OltviColors.OnSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI explanation
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = OltviColors.Action,
                    modifier = Modifier.size(14.dp).align(Alignment.Top)
                )
                Text(
                    text = precio.explicacionIA,
                    style = OltviTypography.pequeno.copy(fontStyle = FontStyle.Italic),
                    color = OltviColors.OnSurfaceDim
                )
            }
        }
    }
}

// ── Driver Result Card ────────────────────────────────────────────────────────

@Composable
private fun DriverResultCard(
    conductor: ConductorDisponible,
    explicacion: String
) {
    Column {
        Text(
            text = "Conductor seleccionado por IA",
            style = OltviTypography.etiqueta,
            color = OltviColors.OnSurfaceDim
        )
        Spacer(modifier = Modifier.height(6.dp))
        OltviDriverCard(
            conductor = conductor,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Filled.SmartToy,
                contentDescription = null,
                tint = OltviColors.Action,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = explicacion,
                style = OltviTypography.pequeno.copy(fontStyle = FontStyle.Italic),
                color = OltviColors.OnSurfaceDim
            )
        }
    }
}
