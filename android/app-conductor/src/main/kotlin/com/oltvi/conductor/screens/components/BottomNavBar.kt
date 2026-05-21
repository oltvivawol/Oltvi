package com.oltvi.conductor.screens.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltvi.conductor.navigation.ConductorRoute
import com.oltvi.core.theme.LocalOltviColors

/**
 * A single bottom-nav entry.
 *
 * @param route       Route ID this tab points to (matches NavGraph composables).
 * @param icon        Filled icon shown when selected/unselected.
 * @param label       User-facing label (Spanish).
 */
data class ConductorTab(
    val route: ConductorRoute,
    val icon: ImageVector,
    val label: String
)

private val tabs: List<ConductorTab> = listOf(
    ConductorTab(ConductorRoute.Home,      Icons.Filled.Speed,         "Inicio"),
    ConductorTab(ConductorRoute.Operacion, Icons.Filled.LocalShipping, "Operación"),
    ConductorTab(ConductorRoute.Ganancias, Icons.Filled.TrendingUp,    "Ganancias"),
    ConductorTab(ConductorRoute.Perfil,    Icons.Filled.AccountCircle, "Perfil")
)

/**
 * The driver app's glass-styled bottom navigation. Uses a darker base than the
 * usuario variant and an orange accent indicator for the active tab to
 * reinforce the "tools / cockpit" feel.
 */
@Composable
fun ConductorBottomNavBar(
    currentRoute: String?,
    onNavigate: (ConductorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalOltviColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.surfaceMid.copy(alpha = 0.92f),
                        colors.surfaceDark.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        colors.glassBorder,
                        Color.Transparent,
                        colors.glassBorder
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .height(64.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEach { tab ->
            ConductorNavItem(
                tab = tab,
                isSelected = currentRoute == tab.route.route,
                onClick = { onNavigate(tab.route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ConductorNavItem(
    tab: ConductorTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalOltviColors.current
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "nav-item-scale"
    )

    val tint = if (isSelected) colors.action else Color(0xFF8BA4BB)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(scale)
                .size(32.dp)
                .then(
                    if (isSelected) Modifier
                        .background(colors.action.copy(alpha = 0.15f), CircleShape)
                    else Modifier
                )
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = tab.label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
