package com.oltvi.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.data.models.EstadoServicio
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.effects.GlassCard
import com.oltvi.core.ui.effects.neonGlow

// ── Utility ───────────────────────────────────────────────────────────────

private fun parseHexColor(hex: String): Color {
    return try {
        val cleaned = hex.trimStart('#')
        val value = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color(0xFF000000 or value)
            8 -> Color(value)
            else -> OltviColors.Surface
        }
    } catch (e: Exception) {
        OltviColors.Surface
    }
}

// ── 0. OltviButtonVariant ─────────────────────────────────────────────────

enum class OltviButtonVariant { Primary, Ghost, Danger }

// ── 1. OltviButton ────────────────────────────────────────────────────────

@Composable
fun OltviButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    variant: OltviButtonVariant = OltviButtonVariant.Primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )

    val gradient = when (variant) {
        OltviButtonVariant.Primary -> Brush.horizontalGradient(listOf(OltviColors.Action, OltviColors.ActionLight))
        OltviButtonVariant.Ghost -> Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
        OltviButtonVariant.Danger -> Brush.horizontalGradient(listOf(OltviColors.Error, OltviColors.Error.copy(alpha = 0.8f)))
    }
    val disabledGradient = Brush.horizontalGradient(listOf(OltviColors.ActionDim, OltviColors.ActionDim))
    val textColor = when (variant) {
        OltviButtonVariant.Primary -> OltviColors.White
        OltviButtonVariant.Ghost -> OltviColors.Action
        OltviButtonVariant.Danger -> OltviColors.White
    }
    val glowColor = when (variant) {
        OltviButtonVariant.Primary -> if (enabled) OltviColors.Action else Color.Transparent
        OltviButtonVariant.Ghost -> Color.Transparent
        OltviButtonVariant.Danger -> if (enabled) OltviColors.Error else Color.Transparent
    }
    val borderModifier = if (variant == OltviButtonVariant.Ghost && enabled) {
        Modifier.border(1.dp, OltviColors.Action, RoundedCornerShape(12.dp))
    } else Modifier

    Box(
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .fillMaxWidth()
            .neonGlow(glowColor, 15f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) gradient else disabledGradient)
            .then(borderModifier)
            .then(
                if (enabled && !isLoading) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = textColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = OltviTypography.hud.copy(color = textColor)
                )
            }
        }
    }
}

// ── 2. OltviGlassAppBar ───────────────────────────────────────────────────

@Composable
fun OltviGlassAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .border(
                width = 0.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, OltviColors.GlassBorder, Color.Transparent)
                ),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = OltviColors.OnSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            Text(
                text = title,
                style = OltviTypography.subtitulo.copy(color = OltviColors.OnSurface),
                modifier = Modifier.weight(1f)
            )

            if (actions != null) {
                actions()
            }
        }
    }
}

// ── 3. OltviStatusChip ────────────────────────────────────────────────────

@Composable
fun OltviStatusChip(estado: EstadoServicio) {
    val parsedColor = parseHexColor(estado.colorHex)

    Surface(
        color = parsedColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(100.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(parsedColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = estado.displayName,
                style = OltviTypography.etiqueta.copy(color = parsedColor),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── 4. OltviRatingStars ───────────────────────────────────────────────────

@Composable
fun OltviRatingStars(
    rating: Double,
    maxStars: Int = 5,
    modifier: Modifier = Modifier
) {
    val animatedRating by animateFloatAsState(
        targetValue = rating.toFloat(),
        animationSpec = tween(durationMillis = 400),
        label = "ratingAnim"
    )

    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            val icon = when {
                animatedRating >= i.toFloat() -> Icons.Filled.Star
                animatedRating >= i.toFloat() - 0.5f -> Icons.Filled.StarHalf
                else -> Icons.Filled.StarOutline
            }
            val tint = if (animatedRating >= i.toFloat() - 0.5f) OltviColors.Warning
            else OltviColors.OnSurfaceDim

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ── 5. OltviDriverCard ────────────────────────────────────────────────────

@Composable
fun OltviDriverCard(
    conductor: ConductorDisponible,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(OltviColors.Action, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conductor.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = OltviTypography.subtitulo.copy(
                        color = OltviColors.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + vehicle + rating
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conductor.nombre,
                    style = OltviTypography.cuerpo.copy(
                        color = OltviColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = conductor.vehiculo.descripcionCorta,
                    style = OltviTypography.pequeno.copy(color = OltviColors.OnSurfaceDim)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OltviRatingStars(rating = conductor.rating)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ETA badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .background(OltviColors.Action.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${conductor.tiempoEstimadoMin}",
                            style = OltviTypography.subtitulo.copy(
                                color = OltviColors.Action,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "min",
                            style = OltviTypography.etiqueta.copy(color = OltviColors.OnSurfaceDim)
                        )
                    }
                }
            }
        }
    }
}

// ── 6. OltviBottomSheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OltviBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OltviColors.Surface,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(OltviColors.Action.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        content()
    }
}

// ── 7. OltviTextField ─────────────────────────────────────────────────────

@Composable
fun OltviTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = OltviTypography.cuerpo.copy(color = OltviColors.OnSurfaceDim)
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = OltviColors.PrincipalDark,
            unfocusedContainerColor = OltviColors.PrincipalDark,
            disabledContainerColor = OltviColors.PrincipalDark,
            focusedBorderColor = OltviColors.Action,
            unfocusedBorderColor = OltviColors.Divider,
            focusedTextColor = OltviColors.OnSurface,
            unfocusedTextColor = OltviColors.OnSurface,
            cursorColor = OltviColors.Action,
            focusedLabelColor = OltviColors.Action,
            unfocusedLabelColor = OltviColors.OnSurfaceDim
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

// ── 8. OltviLogo ──────────────────────────────────────────────────────────

@Composable
fun OltviLogo(
    modifier: Modifier = Modifier,
    size: TextUnit = 28.sp
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(OltviColors.Action, OltviColors.ActionLight)
    )

    val annotated = buildAnnotatedString {
        withStyle(
            SpanStyle(
                brush = gradient,
                fontWeight = FontWeight.Black,
                fontSize = size
            )
        ) {
            append("O")
        }
        withStyle(
            SpanStyle(
                color = OltviColors.White,
                fontWeight = FontWeight.Black,
                fontSize = size
            )
        ) {
            append("LTVI")
        }
    }

    BasicText(
        text = annotated,
        modifier = modifier
    )
}

// ── 9. MapOverlayButton ───────────────────────────────────────────────────

@Composable
fun MapOverlayButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(elevation = 4.dp, shape = CircleShape)
    ) {
        GlassCard(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OltviColors.OnSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
