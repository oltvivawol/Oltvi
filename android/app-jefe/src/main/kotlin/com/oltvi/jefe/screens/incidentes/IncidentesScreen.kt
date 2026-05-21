package com.oltvi.jefe.screens.incidentes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.oltvi.core.data.models.*
import com.oltvi.core.theme.*
import com.oltvi.core.ui.components.*
import java.time.Instant

private val mockIncidentes = listOf(
    EventoVial("i1", "Accidente", "Colisión leve en intersección. Tráfico parcialmente cortado.", PuntoGeo(-34.608, -58.374, "Av. Corrientes y Callao"), "conductor-001", true, Instant.now().minusSeconds(1200)),
    EventoVial("i2", "Obra vial", "Trabajos de pavimentación. Carril derecho cortado.", PuntoGeo(-34.618, -58.381, "Av. Santa Fe 3200"), "usuario-002", true, Instant.now().minusSeconds(3600)),
    EventoVial("i3", "Bache peligroso", "Bache de 40cm de diámetro en carril derecho.", PuntoGeo(-34.601, -58.395, "Av. Cabildo 1800"), "usuario-003", true, Instant.now().minusSeconds(7200)),
    EventoVial("i4", "Corte de calle", "Manifestación. Calle cortada completamente.", PuntoGeo(-34.613, -58.362, "Av. 9 de Julio y Belgrano"), "conductor-004", false, Instant.now().minusSeconds(18000)),
    EventoVial("i5", "Semáforo roto", "Semáforo sin funcionar. Tráfico lento.", PuntoGeo(-34.622, -58.408, "Palermo Soho - Thames y Gorriti"), "usuario-005", false, Instant.now().minusSeconds(28800)),
    EventoVial("i6", "Inundación", "Acumulación de agua por lluvias. Baja velocidad.", PuntoGeo(-34.595, -58.367, "Belgrano - Cabildo y Juramento"), "conductor-006", true, Instant.now().minusSeconds(900)),
    EventoVial("i7", "Objeto en calzada", "Escombros en carril izquierdo.", PuntoGeo(-34.632, -58.388, "Flores - Rivadavia 6500"), "usuario-007", false, Instant.now().minusSeconds(43200)),
    EventoVial("i8", "Desvío activo", "Desvío por obra de subte. Seguir señalización.", PuntoGeo(-34.608, -58.400, "Once - Av. Pueyrredón"), "conductor-008", true, Instant.now().minusSeconds(5400)),
)

@Composable
fun IncidentesScreen() {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Activos", "Resueltos")

    val filtered = when (selectedFilter) {
        "Activos" -> mockIncidentes.filter { it.activo }
        "Resueltos" -> mockIncidentes.filter { !it.activo }
        else -> mockIncidentes
    }

    Column(Modifier.fillMaxSize().background(OltviColors.PrincipalDeep)) {
        Spacer(Modifier.height(32.dp))
        // Header
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("Incidentes Viales", style = OltviTypography.titulo, color = OltviColors.OnSurface, fontWeight = FontWeight.Black)
            Text("${mockIncidentes.count { it.activo }} activos · ${mockIncidentes.count { !it.activo }} resueltos", style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
        }
        Spacer(Modifier.height(12.dp))

        // Filter chips
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, style = OltviTypography.etiqueta) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OltviColors.Action.copy(0.2f),
                        selectedLabelColor = OltviColors.Action,
                        containerColor = OltviColors.SurfaceVariant,
                        labelColor = OltviColors.OnSurfaceDim
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filtered) { incidente ->
                IncidenteCard(incidente)
            }
        }
    }
}

@Composable
private fun IncidenteCard(incidente: EventoVial) {
    val severityColor = when {
        incidente.tipo.contains("Accidente") || incidente.tipo.contains("Corte") -> OltviColors.Error
        incidente.tipo.contains("Inundación") || incidente.tipo.contains("Obra") -> OltviColors.Warning
        else -> OltviColors.Action
    }

    val minutesAgo = ((Instant.now().epochSecond - incidente.fechaReporte.epochSecond) / 60).toInt()
    val timeAgo = when {
        minutesAgo < 60 -> "Hace $minutesAgo min"
        minutesAgo < 1440 -> "Hace ${minutesAgo / 60}h"
        else -> "Hace ${minutesAgo / 1440}d"
    }

    GlassCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp)) {
            // Severity dot
            Box(
                Modifier.size(10.dp).background(severityColor, CircleShape).align(Alignment.Top).offset(y = 4.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(incidente.tipo, style = OltviTypography.cuerpo, color = OltviColors.OnSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    OltviStatusChip(estado = if (incidente.activo) EstadoServicio.EN_CAMINO else EstadoServicio.ENTREGADO)
                }
                Spacer(Modifier.height(4.dp))
                Text(incidente.descripcion, style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, null, tint = OltviColors.OnSurfaceDim, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(incidente.ubicacion.nombre, style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(4.dp))
                Text(timeAgo, style = OltviTypography.etiqueta, color = OltviColors.OnSurfaceDim)
                if (incidente.activo) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {},
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OltviColors.Success),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) { Text("Resolver", style = OltviTypography.etiqueta) }
                        OutlinedButton(
                            onClick = {},
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OltviColors.OnSurfaceDim),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) { Text("Descartar", style = OltviTypography.etiqueta) }
                    }
                }
            }
        }
    }
}
