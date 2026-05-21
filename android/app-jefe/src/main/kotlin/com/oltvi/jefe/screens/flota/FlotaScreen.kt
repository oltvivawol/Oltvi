package com.oltvi.jefe.screens.flota

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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.oltvi.core.data.models.*
import com.oltvi.core.data.services.MockDataService
import com.oltvi.core.theme.*
import com.oltvi.core.ui.components.*

private val DARK_MAP_STYLE = """[{"elementType":"geometry","stylers":[{"color":"#1a2b3c"}]},{"elementType":"labels.text.fill","stylers":[{"color":"#8ba4bb"}]},{"featureType":"road","elementType":"geometry","stylers":[{"color":"#23374d"}]},{"featureType":"water","elementType":"geometry","stylers":[{"color":"#0d1a26"}]},{"featureType":"poi","stylers":[{"visibility":"off"}]}]""".trimIndent()

@Composable
fun FlotaScreen() {
    val mockService = remember { MockDataService() }
    val conductores = remember { mockService.getDriversNear(-34.6037, -58.3816) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showMap by remember { mutableStateOf(false) }

    val filtered = when (selectedFilter) {
        "Disponibles" -> conductores.take(5)
        "En viaje" -> conductores.drop(5)
        else -> conductores
    }

    Column(Modifier.fillMaxSize().background(OltviColors.PrincipalDeep)) {
        Spacer(Modifier.height(32.dp))
        // Header
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Flota", style = OltviTypography.titulo, color = OltviColors.OnSurface, fontWeight = FontWeight.Black)
                Text("${conductores.size} conductores activos", style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
            }
            // Map/List toggle
            Row {
                IconButton(onClick = { showMap = false }) {
                    Icon(Icons.Filled.List, null, tint = if (!showMap) OltviColors.Action else OltviColors.OnSurfaceDim)
                }
                IconButton(onClick = { showMap = true }) {
                    Icon(Icons.Filled.Map, null, tint = if (showMap) OltviColors.Action else OltviColors.OnSurfaceDim)
                }
            }
        }

        // Summary chips
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FlotaSummaryChip("${conductores.size} total", OltviColors.OnSurface)
            FlotaSummaryChip("${conductores.take(5).size} disponibles", OltviColors.Success)
            FlotaSummaryChip("${conductores.drop(5).size} en viaje", OltviColors.Action)
        }

        // Filters
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Todos", "Disponibles", "En viaje").forEach { filter ->
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

        if (showMap) {
            // Map view with all driver markers
            val cameraState = rememberCameraPositionState {
                position = CameraPosition.Builder().target(LatLng(-34.6037, -58.3816)).zoom(13f).build()
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties = MapProperties(mapStyleOptions = MapStyleOptions(DARK_MAP_STYLE)),
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                filtered.forEachIndexed { idx, conductor ->
                    val color = if (idx < 5) OltviColors.Success else OltviColors.Action
                    MarkerComposable(state = MarkerState(position = LatLng(conductor.posicion.latitud, conductor.posicion.longitud)), title = conductor.nombre) {
                        Box(Modifier.size(32.dp).background(color.copy(0.9f), CircleShape).border(2.dp, OltviColors.White, CircleShape), contentAlignment = Alignment.Center) {
                            Text("🚗", fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            // List view
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filtered) { conductor ->
                    ConductorFlotaRow(conductor, filtered.indexOf(conductor) < 5)
                }
            }
        }
    }
}

@Composable
private fun ConductorFlotaRow(conductor: ConductorDisponible, disponible: Boolean) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).background(OltviColors.Action, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(conductor.nombre.first().toString(), style = OltviTypography.subtitulo, color = OltviColors.White, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(conductor.nombre, style = OltviTypography.cuerpo, color = OltviColors.OnSurface, fontWeight = FontWeight.Bold)
                Text(conductor.vehiculo.descripcionCorta, style = OltviTypography.pequeno, color = OltviColors.OnSurfaceDim)
                Text("⭐ ${conductor.rating}", style = OltviTypography.pequeno, color = OltviColors.Warning)
            }
            OltviStatusChip(estado = if (disponible) EstadoServicio.ASIGNADO else EstadoServicio.EN_RUTA)
        }
    }
}

@Composable
private fun FlotaSummaryChip(label: String, color: Color) {
    Surface(color = color.copy(0.12f), shape = RoundedCornerShape(100.dp)) {
        Text(label, style = OltviTypography.etiqueta, color = color, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    }
}
