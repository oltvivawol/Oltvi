package com.oltvi.jefe.screens.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.effects.neonGlow
import com.oltvi.jefe.navigation.JefeRoute

/**
 * Glass-styled bottom navigation bar for OLTVI Mando.
 *
 * Animates the selected indicator (a glowing pill underneath the active tab)
 * and the icon scale on selection change for a tactile, gamified feel.
 *
 * @param items Tabs to render — each one corresponds to a top-level route.
 * @param currentRoute Currently active route string; used to highlight the tab.
 * @param onSelect Invoked when a tab is tapped (caller handles navigation).
 */
@Composable
fun BottomNavBar(
    items: List<JefeRoute>,
    currentRoute: String,
    onSelect: (JefeRoute) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        OltviColors.Surface.copy(alpha = 0.92f),
                        OltviColors.SurfaceVariant.copy(alpha = 0.92f),
                        OltviColors.Surface.copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        OltviColors.GlassBorder.copy(alpha = 0.6f),
                        OltviColors.Action.copy(alpha = 0.35f),
                        OltviColors.GlassBorder.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BottomNavTab(
                    item = item,
                    selected = item.route == currentRoute,
                    onClick = { onSelect(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavTab(
    item: JefeRoute,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconColor by animateFloatAsState(
        targetValue = if (selected) 1f else 0.65f,
        animationSpec = tween(300),
        label = "iconAlpha"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 38.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "indicatorWidth"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )
    val tintColor = if (selected) OltviColors.Action else OltviColors.OnSurfaceDim.copy(alpha = iconColor)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 28.dp)
                .then(
                    if (selected) Modifier.neonGlow(
                        color = OltviColors.Action.copy(alpha = 0.45f),
                        blurRadius = 14.dp
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = tintColor,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            color = tintColor,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(3.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(OltviColors.Action, OltviColors.ActionLight)
                    )
                )
        )
    }
}

