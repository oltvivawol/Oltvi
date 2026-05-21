package com.oltvi.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.data.models.EstadoServicio
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.neonGlow

// =============================================================================
// 1. OltviButton — primary CTA with neon glow, gradient and press scale
// =============================================================================

@Composable
fun OltviButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !loading) 0.96f else 1f,
        label = "press-scale"
    )
    val isInteractive = enabled && !loading
    val gradient = Brush.horizontalGradient(
        colors = if (isInteractive) {
            listOf(OltviColors.action, OltviColors.actionLight)
        } else {
            listOf(Color(0xFF555555), Color(0xFF6B6B6B))
        }
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isInteractive) Modifier.neonGlow(
                    color = OltviColors.action.copy(alpha = 0.5f),
                    blurRadius = 24.dp,
                    cornerRadius = 16.dp
                ) else Modifier
            )
            .background(gradient, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = isInteractive,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// =============================================================================
// 2. OltviGlassAppBar — frosted glass top app bar with back + actions
// =============================================================================

@Composable
fun OltviGlassAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val statusBars: PaddingValues = WindowInsets.statusBars.asPaddingValues()
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBars.calculateTopPadding())
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

// =============================================================================
// 3. OltviStatusChip — pill-shaped chip showing service status colour
// =============================================================================

@Composable
fun OltviStatusChip(
    estado: EstadoServicio,
    modifier: Modifier = Modifier
) {
    val color = Color(estado.colorHex.toColorInt())
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = estado.displayName.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// =============================================================================
// 4. OltviRatingStars — animated row of star icons
// =============================================================================

@Composable
fun OltviRatingStars(
    rating: Float,
    modifier: Modifier = Modifier,
    max: Int = 5,
    size: Dp = 16.dp
) {
    val animated by animateFloatAsState(
        targetValue = rating.coerceIn(0f, max.toFloat()),
        label = "rating-anim"
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(max) { idx ->
            val pos = idx + 1f
            val icon = when {
                animated >= pos -> Icons.Filled.Star
                animated >= pos - 0.5f -> Icons.Filled.StarHalf
                else -> Icons.Outlined.StarOutline
            }
            val tint = if (animated >= pos - 0.5f) OltviColors.warning else Color.White.copy(alpha = 0.35f)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size)
            )
        }
    }
}

// =============================================================================
// 5. OltviDriverCard — driver summary card with avatar, rating and ETA
// =============================================================================

@Composable
fun OltviDriverCard(
    driver: ConductorDisponible,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(OltviColors.action, OltviColors.actionLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driver.nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = driver.nombre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(2.dp))
                OltviRatingStars(rating = driver.rating, size = 14.dp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${driver.vehiculo.marca} ${driver.vehiculo.modelo} · ${driver.vehiculo.patente}",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Box(
                modifier = Modifier
                    .background(OltviColors.action.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                    .border(1.dp, OltviColors.action.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${driver.tiempoEstimadoMin} min",
                    color = OltviColors.action,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// =============================================================================
// 6. OltviBottomSheet — translucent modal bottom sheet
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OltviBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = Color.Transparent,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OltviColors.surfaceMid.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .padding(20.dp),
                content = content
            )
        }
    }
}

// =============================================================================
// 7. OltviTextField — dark themed outlined text field
// =============================================================================

@Composable
fun OltviTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailingIcon?.let { { Icon(it, contentDescription = null) } },
        singleLine = singleLine,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = OltviColors.surfaceMid,
            unfocusedContainerColor = OltviColors.surfaceMid.copy(alpha = 0.7f),
            cursorColor = OltviColors.action,
            focusedBorderColor = OltviColors.action,
            unfocusedBorderColor = OltviColors.glassBorder,
            focusedLabelColor = OltviColors.actionLight,
            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
            focusedLeadingIconColor = OltviColors.action,
            unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f),
            focusedTrailingIconColor = OltviColors.action,
            unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f)
        )
    )
}

// =============================================================================
// 8. OltviLogo — branded text logo "OLTVI" with orange "O" gradient
// =============================================================================

@Composable
fun OltviLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp
) {
    val annotated = buildAnnotatedString {
        withStyle(
            SpanStyle(
                brush = Brush.horizontalGradient(
                    colors = listOf(OltviColors.action, OltviColors.actionLight)
                ),
                fontWeight = FontWeight.Black
            )
        ) { append("O") }
        withStyle(
            SpanStyle(
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        ) { append("LTVI") }
    }
    Text(
        text = annotated,
        modifier = modifier,
        fontSize = fontSize,
        style = MaterialTheme.typography.displaySmall
    )
}

// =============================================================================
// 9. MapOverlayButton — round glass action button for floating map UI
// =============================================================================

@Composable
fun MapOverlayButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(OltviColors.surfaceMid.copy(alpha = 0.7f))
            .border(1.dp, OltviColors.glassBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}
