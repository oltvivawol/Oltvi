package com.oltvi.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object OltviColors {
    val Principal = Color(0xFF23374D)
    val PrincipalDark = Color(0xFF172535)
    val PrincipalDeep = Color(0xFF0D1A26)
    val Action = Color(0xFFE67E22)
    val ActionLight = Color(0xFFF39C12)
    val ActionDim = Color(0xFF8B4A10)
    val Success = Color(0xFF27AE60)
    val Warning = Color(0xFFF1C40F)
    val Error = Color(0xFFE74C3C)
    val Surface = Color(0xFF1A2B3C)
    val SurfaceVariant = Color(0xFF243447)
    val OnSurface = Color(0xFFE8F0F8)
    val OnSurfaceDim = Color(0xFF8BA4BB)
    val Divider = Color(0xFF2D4357)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val Transparent = Color(0x00000000)
    val GlassWhite = Color(0x14FFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
}

private val OltviDarkColorScheme = darkColorScheme(
    primary = OltviColors.Action,
    onPrimary = OltviColors.White,
    primaryContainer = OltviColors.ActionDim,
    onPrimaryContainer = OltviColors.ActionLight,
    secondary = OltviColors.ActionLight,
    onSecondary = OltviColors.PrincipalDeep,
    background = OltviColors.PrincipalDeep,
    onBackground = OltviColors.OnSurface,
    surface = OltviColors.Surface,
    onSurface = OltviColors.OnSurface,
    surfaceVariant = OltviColors.SurfaceVariant,
    onSurfaceVariant = OltviColors.OnSurfaceDim,
    error = OltviColors.Error,
    onError = OltviColors.White,
    outline = OltviColors.Divider,
    outlineVariant = OltviColors.GlassBorder,
)

object OltviTypography {
    val display = TextStyle(fontWeight = FontWeight.Black, fontSize = 36.sp, letterSpacing = (-0.5).sp)
    val titulo = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)
    val subtitulo = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
    val cuerpo = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp)
    val pequeno = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp)
    val hud = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
    val etiqueta = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp)
}

val LocalOltviColors = staticCompositionLocalOf { OltviColors }

@Composable
fun OltviTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalOltviColors provides OltviColors) {
        MaterialTheme(
            colorScheme = OltviDarkColorScheme,
            content = content
        )
    }
}
