package com.oltvi.neural.theme

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
 * CAPA NEURAL brand palette.
 *
 *  deep      = #080C18  (near-black navy — main background)
 *  surface   = #0F1628  (cards and panels)
 *  elevated  = #162035  (elevated surfaces)
 *  neural    = #7B2FBE  (primary purple — Neural identity color)
 *  electric  = #00D4FF  (accent cyan — interactive elements)
 *  xp        = #FFB800  (gold — XP, progress)
 *  success   = #00D97E  (green)
 *  danger    = #FF4757  (red — alerts)
 */
object NeuralColors {
    val deep = Color(0xFF080C18)
    val surface = Color(0xFF0F1628)
    val elevated = Color(0xFF162035)
    val neural = Color(0xFF7B2FBE)
    val neuralLight = Color(0xFF9B59D6)
    val electric = Color(0xFF00D4FF)
    val xpGold = Color(0xFFFFB800)
    val success = Color(0xFF00D97E)
    val danger = Color(0xFFFF4757)
    val warning = Color(0xFFFF9500)
    val textPrimary = Color(0xFFE8EEFF)
    val textSecondary = Color(0xFF8892AA)
    val glassOverlay = Color(0x1A7B2FBE)
    val glassBorder = Color(0x337B2FBE)
    val electricBorder = Color(0x3300D4FF)
}

data class NeuralCustomColors(
    val deep: Color,
    val surface: Color,
    val elevated: Color,
    val neural: Color,
    val neuralLight: Color,
    val electric: Color,
    val xpGold: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val glassOverlay: Color,
    val glassBorder: Color,
    val electricBorder: Color
)

val LocalNeuralColors = staticCompositionLocalOf {
    NeuralCustomColors(
        deep = NeuralColors.deep,
        surface = NeuralColors.surface,
        elevated = NeuralColors.elevated,
        neural = NeuralColors.neural,
        neuralLight = NeuralColors.neuralLight,
        electric = NeuralColors.electric,
        xpGold = NeuralColors.xpGold,
        success = NeuralColors.success,
        danger = NeuralColors.danger,
        warning = NeuralColors.warning,
        textPrimary = NeuralColors.textPrimary,
        textSecondary = NeuralColors.textSecondary,
        glassOverlay = NeuralColors.glassOverlay,
        glassBorder = NeuralColors.glassBorder,
        electricBorder = NeuralColors.electricBorder
    )
}

private val NeuralDarkScheme = darkColorScheme(
    primary = NeuralColors.neural,
    onPrimary = Color.White,
    secondary = NeuralColors.electric,
    onSecondary = NeuralColors.deep,
    tertiary = NeuralColors.xpGold,
    onTertiary = NeuralColors.deep,
    background = NeuralColors.deep,
    onBackground = NeuralColors.textPrimary,
    surface = NeuralColors.surface,
    onSurface = NeuralColors.textPrimary,
    surfaceVariant = NeuralColors.elevated,
    onSurfaceVariant = NeuralColors.textSecondary,
    error = NeuralColors.danger,
    onError = Color.White,
    outline = NeuralColors.glassBorder,
    outlineVariant = NeuralColors.glassOverlay
)

val NeuralTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
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
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp
    )
)

@Composable
fun NeuralTheme(content: @Composable () -> Unit) {
    val customColors = NeuralCustomColors(
        deep = NeuralColors.deep,
        surface = NeuralColors.surface,
        elevated = NeuralColors.elevated,
        neural = NeuralColors.neural,
        neuralLight = NeuralColors.neuralLight,
        electric = NeuralColors.electric,
        xpGold = NeuralColors.xpGold,
        success = NeuralColors.success,
        danger = NeuralColors.danger,
        warning = NeuralColors.warning,
        textPrimary = NeuralColors.textPrimary,
        textSecondary = NeuralColors.textSecondary,
        glassOverlay = NeuralColors.glassOverlay,
        glassBorder = NeuralColors.glassBorder,
        electricBorder = NeuralColors.electricBorder
    )
    CompositionLocalProvider(LocalNeuralColors provides customColors) {
        MaterialTheme(
            colorScheme = NeuralDarkScheme,
            typography = NeuralTypography,
            content = content
        )
    }
}
