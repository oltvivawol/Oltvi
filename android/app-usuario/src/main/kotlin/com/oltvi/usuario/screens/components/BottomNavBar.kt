package com.oltvi.usuario.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oltvi.core.theme.OltviColors
import com.oltvi.core.theme.OltviTypography
import com.oltvi.core.ui.effects.neonGlow

/**
 * Glass-style bottom navigation bar with animated orange-glow selection
 * indicator. Designed to overlay the app content (transparent background).
 */
data class OltviBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val DefaultBottomNavItems: List<OltviBottomNavItem> = listOf(
    OltviBottomNavItem("home", "Inicio", Icons.Filled.Map),
    OltviBottomNavItem("trips", "Viajes", Icons.Filled.History),
    OltviBottomNavItem("payment", "Pagos", Icons.Filled.Payment),
    OltviBottomNavItem("profile", "Perfil", Icons.Filled.Person)
)

@Composable
fun OltviBottomNavBar(
    items: List<OltviBottomNavItem>,
    currentRoute: String?,
    onItemSelected: (OltviBottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        OltviColors.Surface.copy(alpha = 0.95f),
                        OltviColors.PrincipalDark.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, OltviColors.GlassBorder, Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                NavBarItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: OltviBottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val animatedSize by animateDpAsState(
        targetValue = if (isSelected) 44.dp else 38.dp,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "navItemSize"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "navIconScale"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(animatedSize)
                .let { if (isSelected) it.neonGlow(OltviColors.Action, 12f) else it }
                .clip(CircleShape)
                .background(
                    if (isSelected)
                        Brush.radialGradient(
                            listOf(OltviColors.Action, OltviColors.ActionDim)
                        )
                    else
                        Brush.radialGradient(listOf(Color.Transparent, Color.Transparent))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isSelected) OltviColors.White else OltviColors.OnSurfaceDim,
                modifier = Modifier.size((22 * iconScale).dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = item.label,
                style = OltviTypography.etiqueta,
                color = if (isSelected) OltviColors.Action else OltviColors.OnSurfaceDim,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
