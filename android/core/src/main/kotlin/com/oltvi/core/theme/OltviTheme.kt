package com.oltvi.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * OLTVI brand palette + UI surfaces used across all client apps.
 *
 *  principal   = #23374D
 *  action      = #E67E22
 *  actionLight = #F39C12
 *  success     = #27AE60
 *  warning     = #F1C40F
 *  error       = #E74C3C
 */
object OltviColors {
    val principal = Color(0xFF23374D)
    val action = Color(0xFFE67E22)
    val actionLight = Color(0xFFF39C12)
    val success = Color(0xFF27AE60)
    val warning = Color(0xFFF1C40F)
    val error = Color(0xFFE74C3C)

    val surfaceDark = Color(0xFF0F1820)
    val surfaceMid = Color(0xFF1A2733)
    val glassOverlay = Color(0x14FFFFFF)
    val glassBorder = Color(0x33E67E22)
}

/**
 * Brand colour tokens exposed as a CompositionLocal so screens can resolve
 * accents (action / success / warning / etc.) without touching MaterialTheme.
 */
data class OltviCustomColors(
    val principal: Color,
    val action: Color,
    val actionLight: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val surfaceDark: Color,
    val surfaceMid: Color,
    val glassOverlay: Color,
    val glassBorder: Color
)

val LocalOltviColors = staticCompositionLocalOf {
    OltviCustomColors(
        principal = OltviColors.principal,
        action = OltviColors.action,
        actionLight = OltviColors.actionLight,
        success = OltviColors.success,
        warning = OltviColors.warning,
        error = OltviColors.error,
        surfaceDark = OltviColors.surfaceDark,
        surfaceMid = OltviColors.surfaceMid,
        glassOverlay = OltviColors.glassOverlay,
        glassBorder = OltviColors.glassBorder
    )
}

private val OltviDarkScheme = darkColorScheme(
    primary = OltviColors.action,
    onPrimary = Color.White,
    secondary = OltviColors.actionLight,
    onSecondary = Color.White,
    tertiary = OltviColors.warning,
    onTertiary = Color.Black,
    background = OltviColors.surfaceDark,
    onBackground = Color.White,
    surface = OltviColors.surfaceMid,
    onSurface = Color.White,
    surfaceVariant = OltviColors.surfaceMid,
    onSurfaceVariant = Color(0xFFB8C4D0),
    error = OltviColors.error,
    onError = Color.White,
    outline = OltviColors.glassBorder,
    outlineVariant = OltviColors.glassOverlay
)

val OltviTypography: Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun OltviTheme(content: @Composable () -> Unit) {
    val customColors = OltviCustomColors(
        principal = OltviColors.principal,
        action = OltviColors.action,
        actionLight = OltviColors.actionLight,
        success = OltviColors.success,
        warning = OltviColors.warning,
        error = OltviColors.error,
        surfaceDark = OltviColors.surfaceDark,
        surfaceMid = OltviColors.surfaceMid,
        glassOverlay = OltviColors.glassOverlay,
        glassBorder = OltviColors.glassBorder
    )
    CompositionLocalProvider(LocalOltviColors provides customColors) {
        MaterialTheme(
            colorScheme = OltviDarkScheme,
            typography = OltviTypography,
            content = content
        )
    }
}
