package com.oltvi.conductor.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.ui.components.OltviRatingStars
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.neonGlow

@Composable
fun PerfilScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1820))
            .navigationBarsPadding(),
        contentPadding = PaddingValues(top = 56.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { AvatarSection() }
        item { StatsRow() }
        item { VehicleCard() }
        item { DocumentsCard() }
        item { EarningsSummary() }
        item { SettingsList() }
    }
}

@Composable
private fun AvatarSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(OltviColors.action)
                .border(3.dp, OltviColors.action, CircleShape)
                .neonGlow(OltviColors.action, blurRadius = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("MG", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(12.dp))
        Text("Martín González", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Surface(
            color = OltviColors.action.copy(0.2f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Colaborador", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = OltviColors.action, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        OltviRatingStars(rating = 4.85f)
    }
}

@Composable
private fun StatsRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatItem(Icons.Filled.Star, "4.85", "Rating", Modifier.weight(1f))
        StatItem(Icons.Filled.DirectionsCar, "1,247", "Viajes", Modifier.weight(1f))
        StatItem(Icons.Filled.CheckCircle, "94%", "Aceptación", Modifier.weight(1f))
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String, modifier: Modifier) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = OltviColors.action, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(0.6f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun VehicleCard() {
    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DirectionsCar, null, tint = OltviColors.action, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Vehículo asignado", color = OltviColors.action, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Toyota Corolla 2022", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Blanco · Sedán", color = Color.White.copy(0.6f), fontSize = 13.sp)
                }
                Surface(color = OltviColors.action.copy(0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text("AB123CD", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = OltviColors.action, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DocumentsCard() {
    GlassCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Description, null, tint = OltviColors.action, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Documentos", color = OltviColors.action, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            DocumentRow("VTV", "Vence 15/08/2025", isOk = true)
            DocumentRow("Seguro", "Vence 30/11/2025", isOk = true)
            DocumentRow("Licencia A2", "Vigente", isOk = true)
        }
    }
}

@Composable
private fun DocumentRow(nombre: String, detalle: String, isOk: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(detalle, color = Color.White.copy(0.5f), fontSize = 12.sp)
        }
        Surface(
            color = if (isOk) Color(0xFF27AE60).copy(0.15f) else Color(0xFFE74C3C).copy(0.15f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                if (isOk) "OK" else "Vencido",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                color = if (isOk) Color(0xFF27AE60) else Color(0xFFE74C3C),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EarningsSummary() {
    GlassCard {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Este mes", color = Color.White.copy(0.6f), fontSize = 12.sp)
                Text("$62,000", color = OltviColors.action, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(0.15f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Este año", color = Color.White.copy(0.6f), fontSize = 12.sp)
                Text("$1,200,000", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SettingsList() {
    GlassCard {
        Column {
            listOf(
                Triple(Icons.Filled.Notifications, "Notificaciones", true),
                Triple(Icons.Filled.Security, "Privacidad", true),
                Triple(Icons.Filled.Help, "Ayuda", true),
                Triple(Icons.Filled.Logout, "Cerrar sesión", false),
            ).forEach { (icon, label, hasArrow) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = if (label == "Cerrar sesión") Color(0xFFE74C3C) else OltviColors.action, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(label, color = if (label == "Cerrar sesión") Color(0xFFE74C3C) else Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    if (hasArrow) Icon(Icons.Filled.ChevronRight, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(18.dp))
                }
                if (label != "Cerrar sesión") HorizontalDivider(color = Color.White.copy(0.05f))
            }
        }
    }
}
