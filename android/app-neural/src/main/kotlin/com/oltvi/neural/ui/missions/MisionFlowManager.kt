package com.oltvi.neural.ui.missions

import com.oltvi.neural.data.Mision
import com.oltvi.neural.data.TipoMision

// ---------------------------------------------------------------------------
// VAWOL Mission Flow — each mission type demands a real action before XP
// ---------------------------------------------------------------------------

sealed class MisionFlujo {
    /** Simple yes/no confirmation — for community missions */
    data class Confirmacion(
        val pregunta: String = "¿Completaste esta misión?"
    ) : MisionFlujo()

    /** Free-text reflection — minimum chars before button unlocks */
    data class Formulario(
        val pregunta: String,
        val minChars: Int = 20
    ) : MisionFlujo()

    /** GPS check-in — player must be in the zone radius */
    data class CheckinZona(
        val zonaId: String,
        val zonaNombre: String
    ) : MisionFlujo()

    /** Countdown timer — player must stay present for N seconds */
    data class Temporizador(
        val segundos: Int
    ) : MisionFlujo()
}

/** Returns the appropriate flow for a mission based on its type and action required. */
fun misionFlujo(mision: Mision): MisionFlujo {
    // Special override via accionRequerida
    if (mision.accionRequerida == "studio_crear_modelo") {
        return MisionFlujo.Confirmacion("¿Generaste tu primera prenda con el Estudio de Creación?")
    }
    return when (mision.tipo) {
        TipoMision.EXPLORACION -> MisionFlujo.CheckinZona(
            zonaId    = mision.zonaId ?: "la_esperanza",
            zonaNombre = zonaDisplayName(mision.zonaId)
        )
        TipoMision.EDUCACION -> MisionFlujo.Formulario(
            pregunta = "¿Qué aprendiste o qué acción concreta tomaste?"
        )
        TipoMision.TRABAJO -> MisionFlujo.Formulario(
            pregunta = "Describí brevemente el paso que diste hacia tu objetivo laboral"
        )
        TipoMision.EMPRENDIMIENTO -> MisionFlujo.Formulario(
            pregunta = "¿Cuál fue tu idea o acción emprendedora hoy?"
        )
        TipoMision.SALUD -> MisionFlujo.Temporizador(segundos = 120)
        TipoMision.COMUNIDAD -> MisionFlujo.Confirmacion(
            pregunta = "¿Completaste esta tarea con alguien de tu comunidad?"
        )
    }
}

private fun zonaDisplayName(id: String?): String = when (id) {
    "la_esperanza" -> "Ingenio La Esperanza / San Pedro"
    "palermo"      -> "Palermo"
    "recoleta"     -> "Recoleta"
    "centro"       -> "Centro / Microcentro"
    "boedo"        -> "Boedo"
    "belgrano"     -> "Belgrano"
    "flores"       -> "Flores"
    else           -> "tu zona actual"
}
