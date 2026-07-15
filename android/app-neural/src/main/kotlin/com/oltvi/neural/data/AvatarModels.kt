package com.oltvi.neural.data

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Skin tones
// ---------------------------------------------------------------------------

enum class TonoPiel(val displayName: String, val color: Color, val shadowColor: Color) {
    PORCELANA("Porcelana", Color(0xFFF5E6D3), Color(0xFFD4B896)),
    CLARO("Claro", Color(0xFFEDC9A0), Color(0xFFC99B6B)),
    MEDIO_CLARO("Medio Claro", Color(0xFFD4945A), Color(0xFFAA6A30)),
    MEDIO("Medio", Color(0xFFBB7D45), Color(0xFF8A5520)),
    MEDIO_OSCURO("Medio Oscuro", Color(0xFF8B5E3C), Color(0xFF5C3519)),
    OSCURO("Oscuro", Color(0xFF4A2E1A), Color(0xFF2A1508))
}

// ---------------------------------------------------------------------------
// Hair
// ---------------------------------------------------------------------------

enum class EstiloPelo(val displayName: String) {
    RAPADO("Rapado"),
    CORTO("Corto"),
    MEDIO("Medio"),
    LARGO("Largo"),
    RIZADO("Rizado"),
    COLA("Cola de caballo"),
    MOHAWK("Mohawk"),
    ONDULADO("Ondulado")
}

enum class ColorPelo(val displayName: String, val color: Color) {
    NEGRO("Negro", Color(0xFF1A1008)),
    CASTANO_OSCURO("Castaño oscuro", Color(0xFF3D1F0D)),
    CASTANO("Castaño", Color(0xFF6B3A2A)),
    RUBIO("Rubio", Color(0xFFD4A84B)),
    RUBIO_CLARO("Rubio claro", Color(0xFFEDD680)),
    PELIRROJO("Pelirrojo", Color(0xFFB83A1A)),
    GRIS("Gris", Color(0xFF8A8A8A)),
    BLANCO("Blanco", Color(0xFFE8E8E8)),
    AZUL("Azul", Color(0xFF1A5BC4)),
    VIOLETA("Violeta", Color(0xFF7B2FBE)),
    VERDE("Verde", Color(0xFF1A8A4A)),
    ROSA("Rosa", Color(0xFFE84A7A))
}

// ---------------------------------------------------------------------------
// Eyes
// ---------------------------------------------------------------------------

enum class ColorOjos(val displayName: String, val color: Color) {
    MARRON("Marrón", Color(0xFF5C3319)),
    MARRON_CLARO("Marrón claro", Color(0xFF8B5E3C)),
    VERDE("Verde", Color(0xFF2D6A3A)),
    AZUL_OSCURO("Azul oscuro", Color(0xFF1A3B8A)),
    AZUL_CLARO("Azul claro", Color(0xFF4A9ECA)),
    GRIS("Gris", Color(0xFF5A6A7A)),
    NEGRO("Negro", Color(0xFF1A1A1A)),
    VIOLETA("Violeta", Color(0xFF7B2FBE))
}

// ---------------------------------------------------------------------------
// Body type
// ---------------------------------------------------------------------------

enum class TipoCuerpo(val displayName: String, val scaleX: Float) {
    DELGADO("Delgado", 0.82f),
    ESTANDAR("Estándar", 1.0f),
    ATLETICO("Atlético", 1.12f),
    ROBUSTO("Robusto", 1.25f)
}

// ---------------------------------------------------------------------------
// Accessories
// ---------------------------------------------------------------------------

enum class Accesorio(val displayName: String, val icon: String) {
    NINGUNO("Ninguno", ""),
    GAFAS("Gafas", "👓"),
    GAFAS_SOL("Gafas de sol", "🕶️"),
    GORRA("Gorra", "🧢"),
    AURICULARES("Auriculares", "🎧"),
    SOMBRERO("Sombrero", "🎩"),
    CAPUCHA("Capucha", "🪖"),
    CORONITA("Coronita", "👑")
}

// ---------------------------------------------------------------------------
// Complete avatar configuration
// ---------------------------------------------------------------------------

data class AvatarConfig(
    val tonoPiel: TonoPiel = TonoPiel.MEDIO_CLARO,
    val estiloPelo: EstiloPelo = EstiloPelo.CORTO,
    val colorPelo: ColorPelo = ColorPelo.CASTANO_OSCURO,
    val colorOjos: ColorOjos = ColorOjos.MARRON,
    val tipoCuerpo: TipoCuerpo = TipoCuerpo.ESTANDAR,
    val accesorio: Accesorio = Accesorio.NINGUNO
) {
    companion object {
        fun defaultParaClase(clase: ClaseRPG): AvatarConfig = when (clase) {
            ClaseRPG.DESARROLLADOR -> AvatarConfig(accesorio = Accesorio.AURICULARES, colorPelo = ColorPelo.NEGRO)
            ClaseRPG.EMPRENDEDOR -> AvatarConfig(accesorio = Accesorio.GORRA, colorPelo = ColorPelo.CASTANO)
            ClaseRPG.EDUCADOR -> AvatarConfig(accesorio = Accesorio.GAFAS, colorPelo = ColorPelo.CASTANO_OSCURO)
            ClaseRPG.DISENO -> AvatarConfig(colorPelo = ColorPelo.ROSA, accesorio = Accesorio.AURICULARES)
            ClaseRPG.TECNICO -> AvatarConfig(accesorio = Accesorio.CAPUCHA, colorPelo = ColorPelo.NEGRO)
            ClaseRPG.LOGISTICA -> AvatarConfig(accesorio = Accesorio.GORRA, colorPelo = ColorPelo.CASTANO_OSCURO)
            else -> AvatarConfig()
        }
    }
}
