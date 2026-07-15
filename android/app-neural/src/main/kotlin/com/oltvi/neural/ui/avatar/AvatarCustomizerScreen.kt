package com.oltvi.neural.ui.avatar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.neural.data.Accesorio
import com.oltvi.neural.data.AvatarConfig
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.ColorOjos
import com.oltvi.neural.data.ColorPelo
import com.oltvi.neural.data.EstiloPelo
import com.oltvi.neural.data.TipoCuerpo
import com.oltvi.neural.data.TonoPiel
import com.oltvi.neural.theme.LocalNeuralColors
import com.oltvi.neural.theme.NeuralColors
import com.oltvi.neural.ui.onboarding.NeuralButton

@Composable
fun AvatarCustomizerScreen(
    clase: ClaseRPG,
    initialConfig: AvatarConfig = AvatarConfig.defaultParaClase(clase),
    onConfirm: (AvatarConfig) -> Unit,
    onBack: () -> Unit
) {
    val nc = LocalNeuralColors.current
    var config by remember { mutableStateOf(initialConfig) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(nc.deep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "←",
                    color = nc.textSecondary, fontSize = 22.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                        .padding(8.dp)
                )
                Text(
                    "TU AVATAR",
                    style = MaterialTheme.typography.labelLarge,
                    color = nc.electric, letterSpacing = 2.sp
                )
                Spacer(Modifier.width(40.dp))
            }

            // Avatar preview — large, centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(android.graphics.Color.parseColor(clase.colorHex)).copy(0.12f),
                                nc.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glow platform
                Box(
                    modifier = Modifier
                        .size(160.dp, 24.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(android.graphics.Color.parseColor(clase.colorHex)).copy(0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp)
                )

                AvatarView(
                    config = config,
                    clase = clase,
                    size = 190.dp,
                    animated = true,
                    showGlow = true
                )
            }

            // Customizer options
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // --- Tono de piel ---
                CustomizerSection(title = "TONO DE PIEL") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(TonoPiel.entries) { tono ->
                            ColorCircle(
                                color = tono.color,
                                borderColor = tono.shadowColor,
                                selected = config.tonoPiel == tono,
                                label = tono.displayName,
                                onClick = { config = config.copy(tonoPiel = tono) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- Estilo de pelo ---
                CustomizerSection(title = "ESTILO DE PELO") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(EstiloPelo.entries) { estilo ->
                            StyleChip(
                                label = estilo.displayName,
                                selected = config.estiloPelo == estilo,
                                color = NeuralColors.neural,
                                onClick = { config = config.copy(estiloPelo = estilo) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- Color de pelo ---
                CustomizerSection(title = "COLOR DE PELO") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(ColorPelo.entries) { colorPelo ->
                            ColorCircle(
                                color = colorPelo.color,
                                selected = config.colorPelo == colorPelo,
                                label = colorPelo.displayName,
                                onClick = { config = config.copy(colorPelo = colorPelo) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- Color de ojos ---
                CustomizerSection(title = "COLOR DE OJOS") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(ColorOjos.entries) { ojos ->
                            ColorCircle(
                                color = ojos.color,
                                selected = config.colorOjos == ojos,
                                label = ojos.displayName,
                                onClick = { config = config.copy(colorOjos = ojos) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- Tipo de cuerpo ---
                CustomizerSection(title = "COMPLEXIÓN") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TipoCuerpo.entries.forEach { tipo ->
                            StyleChip(
                                label = tipo.displayName,
                                selected = config.tipoCuerpo == tipo,
                                color = NeuralColors.electric,
                                onClick = { config = config.copy(tipoCuerpo = tipo) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- Accesorios ---
                CustomizerSection(title = "ACCESORIO") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(Accesorio.entries) { acc ->
                            AccesorioChip(
                                accesorio = acc,
                                selected = config.accesorio == acc,
                                accentColor = Color(android.graphics.Color.parseColor(clase.colorHex)),
                                onClick = { config = config.copy(accesorio = acc) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                NeuralButton(
                    text = "Confirmar personaje",
                    onClick = { onConfirm(config) },
                    glowing = true
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

@Composable
private fun CustomizerSection(title: String, content: @Composable () -> Unit) {
    val nc = LocalNeuralColors.current
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = nc.electric,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    borderColor: Color = Color.White.copy(0.6f),
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val borderAnim by animateColorAsState(
        if (selected) NeuralColors.electric else nc.glassBorder,
        tween(200),
        label = "border"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(52.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color)
                .border(if (selected) 3.dp else 1.dp, borderAnim, CircleShape)
                .clickable(onClick = onClick)
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(14.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.9f))
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) nc.electric else nc.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 8.sp
        )
    }
}

@Composable
private fun StyleChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val nc = LocalNeuralColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) color.copy(0.2f) else nc.elevated)
            .border(1.5.dp, if (selected) color else nc.glassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else nc.textSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AccesorioChip(
    accesorio: Accesorio,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val nc = LocalNeuralColors.current
    val displayName = if (accesorio == Accesorio.NINGUNO) "Ninguno" else accesorio.displayName

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) accentColor.copy(0.2f) else nc.elevated)
            .border(1.5.dp, if (selected) accentColor else nc.glassBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (accesorio.icon.isNotEmpty()) {
                Text(accesorio.icon, fontSize = 20.sp)
                Spacer(Modifier.height(2.dp))
            }
            Text(
                displayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) accentColor else nc.textSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
