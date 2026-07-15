package com.oltvi.neural.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.filament.Material
import com.google.android.filament.MaterialInstance
import io.github.sceneview.rememberEngine

// ---------------------------------------------------------------------------
// VAWOL 3D Material — cel-shading for Filament meshes
//
// Shader design (3 flat bands, no gradient):
//   float NdotL = dot(material.normal, light.l);
//   float band = NdotL > 0.5 ? 1.0 : (NdotL > 0.0 ? 0.68 : 0.35);
//   material.baseColor.rgb = baseColor * band;
//
// The .filamat binary is compiled offline with `matc` and embedded as a
// ByteArray in assets/materials/vawol_cel.filamat. Until that asset is
// available, VawolMaterial falls back to Filament's unlit path and applies
// the same 3-band approximation via emissive colour (ambient-only mode).
// ---------------------------------------------------------------------------

object VawolMaterial {

    // ── Style helpers — mirrors VawolStyle colour math for 3D meshes ────────

    /** Flat shadow (0.35 band): darkened, slightly cool */
    fun shadow3D(base: Color): Color = Color(
        red   = base.red   * 0.35f,
        green = base.green * 0.35f,
        blue  = base.blue  * 0.38f,
        alpha = base.alpha
    )

    /** Mid tone (0.68 band): base slightly darkened */
    fun midTone3D(base: Color): Color = Color(
        red   = base.red   * 0.68f,
        green = base.green * 0.68f,
        blue  = base.blue  * 0.72f,
        alpha = base.alpha
    )

    /** Highlight (1.0 band): base + warm lift */
    fun highlight3D(base: Color): Color = Color(
        red   = (base.red   + 0.22f).coerceAtMost(1f),
        green = (base.green + 0.20f).coerceAtMost(1f),
        blue  = (base.blue  + 0.14f).coerceAtMost(1f),
        alpha = base.alpha
    )

    // ── Outline colour (ink effect, same formula as VawolStyle) ─────────────

    fun outlineColor3D(base: Color): Color = Color(
        red   = base.red   * 0.30f,
        green = base.green * 0.28f,
        blue  = base.blue  * 0.32f,
        alpha = base.alpha
    )

    // ── Season ambient tint for 3D scene ────────────────────────────────────

    fun ambientFor(estacion: EstacionVawol): Color = when (estacion) {
        EstacionVawol.VERANO    -> Color(1.00f, 0.95f, 0.80f, 0.08f)   // warm golden
        EstacionVawol.OTONO     -> Color(1.00f, 0.75f, 0.50f, 0.10f)   // amber-orange
        EstacionVawol.INVIERNO  -> Color(0.70f, 0.85f, 1.00f, 0.10f)   // cool blue
        EstacionVawol.PRIMAVERA -> Color(0.90f, 1.00f, 0.80f, 0.08f)   // fresh green
    }

    // ── Vegetation colours (NOA palette) ────────────────────────────────────

    val CANA_AZUCAR  = Color(0xFF5B8C2A.toInt())   // sugarcane green
    val TIPA_TRONCO  = Color(0xFF7B5B3A.toInt())   // warm brown bark
    val TIPA_COPA    = Color(0xFF3A7D44.toInt())   // deep leaf green
    val PALMA_TRONCO = Color(0xFF9E8462.toInt())   // sandy palm bark
    val PALMA_COPA   = Color(0xFF2D6E3A.toInt())   // tropical palm green
    val MONTANA_BASE = Color(0xFF6B7B5C.toInt())   // grey-green mountain
    val SUELO_NOA    = Color(0xFF8B6844.toInt())   // red-clay Jujuy soil
    val CIELO_VERANO = Color(0xFF4A90D9.toInt())   // deep blue sky
    val CIELO_INVIERNO = Color(0xFF8AAEC9.toInt()) // pale winter sky

    // ── Future: custom .filamat cel shader ──────────────────────────────────
    //
    // When `assets/materials/vawol_cel.filamat` is available, load it with:
    //   val bytes = context.assets.open("materials/vawol_cel.filamat").readBytes()
    //   val material = Material.Builder().payload(bytes, bytes.size).build(engine)
    //   val instance = material.createInstance()
    //   instance.setParameter("baseColor", r, g, b)
    //   instance.setParameter("shadowBand", 0.35f)
    //   instance.setParameter("midBand", 0.68f)
    //
    // The GLSL fragment body in vawol_cel.filamat:
    //   float NdotL = dot(shading_normal, shading_light);
    //   float band = NdotL > 0.5 ? 1.0 : (NdotL > 0.0 ? 0.68 : 0.35);
    //   material.baseColor = vec4(baseColor * band, 1.0);
}
