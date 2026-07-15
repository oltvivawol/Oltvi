package com.oltvi.neural.ui.shop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oltvi.neural.data.CategoriaPrenda
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.EquipamientoAvatar
import com.oltvi.neural.data.NeuralRepository
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import com.oltvi.neural.data.PrendaRopa
import com.oltvi.neural.data.Rareza
import com.oltvi.neural.data.WardrobeSeedData
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import com.oltvi.neural.ui.avatar.AvatarView
import com.oltvi.neural.ui.components.NeuralCard
import kotlinx.coroutines.launch

@Composable
fun TiendaScreen(
    perfil: PerfilNeural = PerfilNeural(
        id = "demo", nombre = "Jugador",
        clase = ClaseRPG.EXPLORADOR, objetivo = ObjetivoVida.TRABAJO,
        monedas = 1500
    ),
    onBack: () -> Unit,
    onPerfilUpdate: (PerfilNeural) -> Unit = {},
    repository: NeuralRepository? = null
) {
    val nc = LocalNeuralColors.current
    val scope = rememberCoroutineScope()

    var selectedCategoria by remember { mutableStateOf<CategoriaPrenda?>(null) }
    var perfilActual by remember { mutableStateOf(perfil) }
    val catalogo = remember { WardrobeSeedData.catalogoInicial() }
    var feedbackMsg by remember { mutableStateOf<String?>(null) }

    val prendasFiltradas = remember(selectedCategoria, catalogo) {
        if (selectedCategoria == null) catalogo else catalogo.filter { it.categoria == selectedCategoria }
    }

    Box(modifier = Modifier.fillMaxSize().background(nc.deep)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "←", color = nc.textSecondary, fontSize = 22.sp,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onBack() }.padding(8.dp)
                )
                Text(
                    "TIENDA NEURAL",
                    style = MaterialTheme.typography.labelLarge,
                    color = nc.electric, letterSpacing = 2.sp
                )
                // Saldo
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeuralColors.xpGold.copy(0.12f))
                        .border(1.dp, NeuralColors.xpGold.copy(0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${perfilActual.monedas}",
                        style = MaterialTheme.typography.titleSmall,
                        color = NeuralColors.xpGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Preview del avatar con ropa actual
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                AvatarView(
                    config = perfilActual.avatar,
                    clase = perfilActual.clase,
                    size = 120.dp,
                    animated = true,
                    showGlow = false
                )
            }

            // Feedback mensaje
            feedbackMsg?.let { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(msg, style = MaterialTheme.typography.bodySmall,
                        color = if (msg.startsWith("✓")) NeuralColors.success else nc.electric)
                }
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2000)
                    feedbackMsg = null
                }
            }

            Spacer(Modifier.height(8.dp))

            // Filtro por categoría
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoriaChip(
                        label = "Todo",
                        icon = "🛍️",
                        selected = selectedCategoria == null,
                        onClick = { selectedCategoria = null }
                    )
                }
                items(CategoriaPrenda.entries) { cat ->
                    CategoriaChip(
                        label = cat.displayName,
                        icon = cat.icon,
                        selected = selectedCategoria == cat,
                        onClick = { selectedCategoria = if (selectedCategoria == cat) null else cat }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Grilla de prendas
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(prendasFiltradas) { prenda ->
                    val poseida = prenda.id in perfilActual.inventario
                    val equipada = perfilActual.equipamiento.prendaEn(prenda.categoria) == prenda.id
                    PrendaCard(
                        prenda = prenda,
                        poseida = poseida,
                        equipada = equipada,
                        saldo = perfilActual.monedas,
                        onComprar = {
                            if (!poseida && perfilActual.monedas >= prenda.precio) {
                                val nuevoSaldo = perfilActual.monedas - prenda.precio
                                val nuevoInventario = perfilActual.inventario + prenda.id
                                perfilActual = perfilActual.copy(monedas = nuevoSaldo, inventario = nuevoInventario)
                                onPerfilUpdate(perfilActual)
                                feedbackMsg = "✓ ${prenda.nombre} comprada"
                                scope.launch {
                                    repository?.comprarPrenda(perfilActual.id, prenda, nuevoSaldo + prenda.precio)
                                }
                            } else if (perfilActual.monedas < prenda.precio) {
                                feedbackMsg = "Sin Neurocréditos suficientes"
                            }
                        },
                        onEquipar = {
                            if (poseida) {
                                val nuevoEquip = if (equipada) {
                                    perfilActual.equipamiento.desequipar(prenda.categoria)
                                } else {
                                    perfilActual.equipamiento.equipar(prenda.id, prenda.categoria)
                                }
                                perfilActual = perfilActual.copy(equipamiento = nuevoEquip)
                                onPerfilUpdate(perfilActual)
                                feedbackMsg = if (equipada) "Prenda desequipada" else "✓ ${prenda.nombre} equipada"
                                scope.launch {
                                    repository?.equipar(perfilActual.id, nuevoEquip)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Componentes internos
// ---------------------------------------------------------------------------

@Composable
private fun CategoriaChip(label: String, icon: String, selected: Boolean, onClick: () -> Unit) {
    val nc = LocalNeuralColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) nc.electric.copy(0.2f) else nc.elevated)
            .border(1.dp, if (selected) nc.electric else nc.glassBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) nc.electric else nc.textSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PrendaCard(
    prenda: PrendaRopa,
    poseida: Boolean,
    equipada: Boolean,
    saldo: Int,
    onComprar: () -> Unit,
    onEquipar: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val rarezaColor = Color(android.graphics.Color.parseColor(prenda.rareza.colorHex))
    val prendaColor = Color(android.graphics.Color.parseColor(prenda.colorHex))
    val puedeComprar = !poseida && saldo >= prenda.precio

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(nc.surface)
            .border(1.dp, rarezaColor.copy(if (equipada) 0.9f else 0.25f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ícono de prenda con color
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(prendaColor.copy(0.15f))
                .border(1.dp, prendaColor.copy(0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(prenda.categoria.icon, fontSize = 28.sp)
        }

        Spacer(Modifier.height(8.dp))

        // Badge rareza
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(rarezaColor.copy(0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                prenda.rareza.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = rarezaColor, fontWeight = FontWeight.Bold, fontSize = 8.sp
            )
        }

        Spacer(Modifier.height(4.dp))
        Text(
            prenda.nombre, style = MaterialTheme.typography.bodySmall,
            color = nc.textPrimary, fontWeight = FontWeight.SemiBold,
            maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        if (equipada) {
            // Botón "Equipada" — tap para desequipar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(rarezaColor.copy(0.2f))
                    .border(1.dp, rarezaColor, RoundedCornerShape(8.dp))
                    .clickable(onClick = onEquipar)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("✓ Equipada", style = MaterialTheme.typography.labelSmall,
                    color = rarezaColor, fontWeight = FontWeight.Bold)
            }
        } else if (poseida) {
            // Botón "Equipar"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(nc.elevated)
                    .border(1.dp, nc.glassBorder, RoundedCornerShape(8.dp))
                    .clickable(onClick = onEquipar)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Equipar", style = MaterialTheme.typography.labelSmall,
                    color = nc.textSecondary)
            }
        } else {
            // Botón comprar con precio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (puedeComprar) NeuralColors.xpGold.copy(0.15f) else nc.elevated)
                    .border(1.dp, if (puedeComprar) NeuralColors.xpGold.copy(0.5f) else nc.glassBorder, RoundedCornerShape(8.dp))
                    .clickable(enabled = puedeComprar, onClick = onComprar)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 11.sp)
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "${prenda.precio}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (puedeComprar) NeuralColors.xpGold else nc.textSecondary.copy(0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
