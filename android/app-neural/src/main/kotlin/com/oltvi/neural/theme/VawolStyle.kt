package com.oltvi.neural.theme

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import kotlin.math.PI
import kotlin.random.Random

// ---------------------------------------------------------------------------
// VAWOL — Estilo visual original. Diseñado para Capa Neural.
//
// Inspiración:
//  · Cel-shading de dos tonos (base + sombra plana, sin gradiente de cuerpo)
//  · Outlines de tinta variables (más gruesos en silueta, más finos en detalle)
//  · Ojos anime con chispa en estrella cruzada — la firma del estilo
//  · Proporciones redondeadas y suaves, cabeza proporcionalmente grande
//  · Pelo con un streak de brillo propio en cada estilo
//  · Paleta por clase RPG: máximo 3 colores derivados del color base
//  · Sistema de estaciones que tiñe el mundo y emite partículas
// ---------------------------------------------------------------------------

object VawolStyle {

    // ── Pesos de outline ─────────────────────────────────────────────────────
    const val OUTLINE_SILUETA = 8f   // cuerpo, cabeza — más grueso
    const val OUTLINE_DETALLE = 4f   // accesorios, ropa interior
    const val OUTLINE_FINO    = 2f   // detalles pequeños

    // ── Derivación de color ───────────────────────────────────────────────────

    /** Tono de outline: oscuro, ligeramente saturado (no negro puro). */
    fun outlineOf(base: Color): Color = Color(
        red   = (base.red   * 0.30f).coerceIn(0f, 1f),
        green = (base.green * 0.28f).coerceIn(0f, 1f),
        blue  = (base.blue  * 0.32f).coerceIn(0f, 1f),
        alpha = 1f
    )

    /** Zona de sombra: más oscura pero no apagada — conserva saturación. */
    fun shadowOf(base: Color): Color = Color(
        red   = (base.red   * 0.68f).coerceIn(0f, 1f),
        green = (base.green * 0.68f).coerceIn(0f, 1f),
        blue  = (base.blue  * 0.75f).coerceIn(0f, 1f),
        alpha = base.alpha
    )

    /** Zona de highlight: más clara pero no lavada. */
    fun highlightOf(base: Color): Color = Color(
        red   = (base.red   + 0.26f).coerceIn(0f, 1f),
        green = (base.green + 0.24f).coerceIn(0f, 1f),
        blue  = (base.blue  + 0.20f).coerceIn(0f, 1f),
        alpha = base.alpha
    )
}

// ---------------------------------------------------------------------------
// VAWOL — Sistema de estaciones
// Hemisferio sur (Argentina): Verano dic-feb, Otoño mar-may, etc.
// ---------------------------------------------------------------------------

enum class EstacionVawol(
    val displayName: String,
    val emoji: String,
    val ambientColor: Color,      // tinte ambiental sobre el canvas
    val particleColor: Color,     // color principal de partículas
    val particleShape: ParticleShape,
    val shopTagline: String
) {
    VERANO(
        "Verano", "☀️",
        ambientColor    = Color(0xFFFF9500).copy(alpha = 0.055f),
        particleColor   = Color(0xFFFFD700),
        particleShape   = ParticleShape.DESTELLO,
        shopTagline     = "Edición Verano"
    ),
    OTONO(
        "Otoño", "🍂",
        ambientColor    = Color(0xFFBF360C).copy(alpha = 0.065f),
        particleColor   = Color(0xFFE65100),
        particleShape   = ParticleShape.HOJA,
        shopTagline     = "Colección Otoño"
    ),
    INVIERNO(
        "Invierno", "❄️",
        ambientColor    = Color(0xFF4FC3F7).copy(alpha = 0.062f),
        particleColor   = Color(0xFFE3F2FD),
        particleShape   = ParticleShape.NIEVE,
        shopTagline     = "Edición Invierno"
    ),
    PRIMAVERA(
        "Primavera", "🌸",
        ambientColor    = Color(0xFFFF6B9D).copy(alpha = 0.055f),
        particleColor   = Color(0xFFFF9EBB),
        particleShape   = ParticleShape.PETALO,
        shopTagline     = "Colección Primavera"
    )
}

enum class ParticleShape { PETALO, DESTELLO, HOJA, NIEVE }

/** Detecta la estación actual según el hemisferio sur. */
object EstacionDetector {
    val actual: EstacionVawol
        get() = when (LocalDate.now().monthValue) {
            12, 1, 2 -> EstacionVawol.VERANO
            3, 4, 5  -> EstacionVawol.OTONO
            6, 7, 8  -> EstacionVawol.INVIERNO
            else     -> EstacionVawol.PRIMAVERA
        }
}

// ---------------------------------------------------------------------------
// Partículas deterministas (seed fija por estación — sin jitter en cada frame)
// ---------------------------------------------------------------------------

data class VawolParticle(
    val seedX: Float,
    val seedY: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float,
    val drift: Float      // amplitud del movimiento horizontal sinusoidal
)

fun generateParticles(count: Int, seed: Int): List<VawolParticle> {
    val rng = Random(seed)
    return List(count) {
        VawolParticle(
            seedX = rng.nextFloat(),
            seedY = rng.nextFloat(),
            size  = 0.25f + rng.nextFloat() * 0.75f,
            speed = 0.035f + rng.nextFloat() * 0.085f,
            alpha = 0.35f + rng.nextFloat() * 0.52f,
            drift = 0.005f + rng.nextFloat() * 0.018f
        )
    }
}
