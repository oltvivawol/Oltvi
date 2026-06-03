package com.oltvi.neural.data

import com.google.android.gms.maps.model.LatLng
import java.time.Instant

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

enum class ClaseRPG(
    val displayName: String,
    val icon: String,
    val description: String,
    val colorHex: String
) {
    EMPRENDEDOR("Emprendedor", "🚀", "Crear negocios y oportunidades", "#FF6B35"),
    DESARROLLADOR("Desarrollador", "💻", "Tecnología y programación", "#00D4FF"),
    TECNICO("Técnico", "⚙️", "Oficios y habilidades prácticas", "#FFB800"),
    EDUCADOR("Educador", "📚", "Enseñar y transmitir conocimiento", "#7B2FBE"),
    LOGISTICA("Logística", "🚚", "Transporte y movilidad urbana", "#27AE60"),
    SALUD("Salud", "❤️", "Bienestar y cuidado comunitario", "#E74C3C"),
    DISENO("Diseñador", "🎨", "Creatividad y comunicación visual", "#E67E22"),
    EXPLORADOR("Explorador", "🗺️", "Descubrir y conectar la ciudad", "#9B59B6")
}

enum class ObjetivoVida(val displayName: String, val icon: String) {
    TRABAJO("Conseguir trabajo", "💼"),
    NEGOCIO("Abrir un negocio", "🏪"),
    EDUCACION("Aprender y crecer", "🎓"),
    DINERO("Generar ingresos", "💰"),
    CASA("Tener mi casa", "🏠"),
    SALUD("Mejorar mi salud", "💪"),
    AMIGOS("Conectar con personas", "🤝"),
    LIBERTAD("Independizarme", "✈️")
}

enum class TipoMision(val displayName: String, val colorHex: String) {
    EDUCACION("Educación", "#7B2FBE"),
    TRABAJO("Trabajo", "#00D4FF"),
    COMUNIDAD("Comunidad", "#27AE60"),
    SALUD("Salud", "#E74C3C"),
    EMPRENDIMIENTO("Emprendimiento", "#FF6B35"),
    EXPLORACION("Exploración", "#FFB800")
}

enum class NivelNeural(val displayName: String, val minXp: Int, val title: String) {
    INICIADO("Iniciado", 0, "Recién llegado a la ciudad"),
    VECINO("Vecino", 500, "Comenzás a conocer el barrio"),
    ACTIVO("Activo", 1500, "Parte del tejido comunitario"),
    REFERENTE("Referente", 4000, "Los demás te reconocen"),
    LIDER("Líder", 9000, "Influís en tu comunidad"),
    LEYENDA("Leyenda", 20000, "Tu impacto es permanente")
}

// ---------------------------------------------------------------------------
// Domain models
// ---------------------------------------------------------------------------

data class PerfilNeural(
    val id: String,
    val nombre: String,
    val clase: ClaseRPG,
    val objetivo: ObjetivoVida,
    val avatar: AvatarConfig = AvatarConfig(),
    val nivel: NivelNeural = NivelNeural.INICIADO,
    val xpActual: Int = 0,
    val reputacion: Int = 100,
    val misionesCompletadas: Int = 0,
    val logros: List<Logro> = emptyList(),
    val fechaIngreso: Instant = Instant.now()
) {
    val inicial: String get() = nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    val xpParaSiguienteNivel: Int
        get() {
            val entries = NivelNeural.entries
            val idx = entries.indexOf(nivel)
            return if (idx < entries.lastIndex) entries[idx + 1].minXp else Int.MAX_VALUE
        }

    val progresoNivel: Float
        get() {
            val entries = NivelNeural.entries
            val idx = entries.indexOf(nivel)
            val minActual = nivel.minXp
            val minSiguiente = if (idx < entries.lastIndex) entries[idx + 1].minXp else minActual + 1
            return ((xpActual - minActual).toFloat() / (minSiguiente - minActual)).coerceIn(0f, 1f)
        }

    val numeroNivel: Int get() = NivelNeural.entries.indexOf(nivel) + 1
}

data class Mision(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoMision,
    val xpRecompensa: Int,
    val completada: Boolean = false,
    val progreso: Float = 0f,
    val accionRequerida: String? = null,
    val duracionEstimada: String = "~15 min"
)

data class Logro(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val icono: String,
    val xpBonus: Int,
    val obtenido: Boolean = false
)

data class ZonaBarrio(
    val id: String,
    val nombre: String,
    val centro: LatLng,
    val radio: Double,
    val activa: Boolean = true,
    val misionesActivas: Int = 0,
    val colorHex: String = "#7B2FBE",
    val descripcion: String = ""
)

data class MensajeGuia(
    val id: String,
    val contenido: String,
    val esIA: Boolean,
    val timestamp: Instant = Instant.now()
)

data class RutaAprendizaje(
    val objetivo: ObjetivoVida,
    val clase: ClaseRPG,
    val etapas: List<EtapaAprendizaje>
)

data class EtapaAprendizaje(
    val numero: Int,
    val titulo: String,
    val descripcion: String,
    val xpRecompensa: Int,
    val completada: Boolean = false,
    val misionIds: List<String> = emptyList()
)

// ---------------------------------------------------------------------------
// Seed data
// ---------------------------------------------------------------------------

object NeuralSeedData {

    fun misionesPorObjetivo(objetivo: ObjetivoVida): List<Mision> = when (objetivo) {
        ObjetivoVida.TRABAJO -> listOf(
            Mision("t1", "Actualizá tu CV", "Revisá y mejorá tu currículum con la ayuda de la IA.", TipoMision.TRABAJO, 150, duracionEstimada = "~20 min"),
            Mision("t2", "Explorá ofertas de trabajo", "Revisá al menos 5 ofertas en tu barrio o zona.", TipoMision.TRABAJO, 100, duracionEstimada = "~10 min"),
            Mision("t3", "Aprendé a comunicarte en una entrevista", "Completá el módulo de comunicación efectiva.", TipoMision.EDUCACION, 200, duracionEstimada = "~30 min"),
            Mision("t4", "Conectá con alguien de tu rubro", "Presentate a un vecino con experiencia en tu área.", TipoMision.COMUNIDAD, 120, duracionEstimada = "~15 min")
        )
        ObjetivoVida.NEGOCIO -> listOf(
            Mision("n1", "Definí tu idea de negocio", "Describí en 3 oraciones qué vas a vender y a quién.", TipoMision.EMPRENDIMIENTO, 200, duracionEstimada = "~20 min"),
            Mision("n2", "Analizá la competencia local", "Visitá (o investigá) 3 negocios similares en tu barrio.", TipoMision.EXPLORACION, 150, duracionEstimada = "~1 hora"),
            Mision("n3", "Calculá cuánto necesitás para empezar", "Completá el módulo de finanzas básicas para emprendedores.", TipoMision.EDUCACION, 250, duracionEstimada = "~45 min"),
            Mision("n4", "Creá tu primer perfil comercial", "Configurá el perfil de tu negocio en Capa Neural.", TipoMision.EMPRENDIMIENTO, 300, duracionEstimada = "~15 min")
        )
        ObjetivoVida.EDUCACION -> listOf(
            Mision("e1", "Elegí una habilidad para desarrollar", "Seleccioná una rama del árbol de habilidades y completá el primer nivel.", TipoMision.EDUCACION, 150, duracionEstimada = "~20 min"),
            Mision("e2", "Completá un microcurso", "Terminá el primer módulo de cualquier área de aprendizaje.", TipoMision.EDUCACION, 300, duracionEstimada = "~1 hora"),
            Mision("e3", "Enseñale algo a otro usuario", "Compartí un recurso útil en la comunidad de tu barrio.", TipoMision.COMUNIDAD, 200, duracionEstimada = "~15 min"),
            Mision("e4", "Asistí a un evento de capacitación", "Participá en un evento formativo de tu zona.", TipoMision.COMUNIDAD, 250, duracionEstimada = "~2 horas")
        )
        else -> listOf(
            Mision("g1", "Explorá tu barrio", "Caminá 3 zonas marcadas en el mapa de tu barrio.", TipoMision.EXPLORACION, 100, duracionEstimada = "~30 min"),
            Mision("g2", "Presentate en la comunidad", "Hacé tu primera publicación en el foro del barrio.", TipoMision.COMUNIDAD, 150, duracionEstimada = "~10 min"),
            Mision("g3", "Completá tu perfil", "Agregá tu clase, objetivo y una descripción corta.", TipoMision.EDUCACION, 100, duracionEstimada = "~5 min"),
            Mision("g4", "Hablá con la IA guía", "Tené tu primera conversación con la IA sobre tus metas.", TipoMision.EDUCACION, 200, duracionEstimada = "~10 min")
        )
    }

    fun logrosIniciales(): List<Logro> = listOf(
        Logro("l1", "Primer Paso", "Iniciaste tu viaje en Capa Neural.", "👣", 50),
        Logro("l2", "Primer Contacto", "Hablaste por primera vez con la IA guía.", "🤖", 100),
        Logro("l3", "Vecino Activo", "Completaste tu primera misión comunitaria.", "🏘️", 200),
        Logro("l4", "En Camino", "Alcanzaste el nivel Vecino.", "📈", 300),
        Logro("l5", "Constructor", "Completaste 5 misiones.", "🏗️", 500)
    )

    fun zonasBarrio(): List<ZonaBarrio> = listOf(
        ZonaBarrio("palermo", "Palermo", com.google.android.gms.maps.model.LatLng(-34.5875, -58.4150), 1500.0, misionesActivas = 7, colorHex = "#7B2FBE"),
        ZonaBarrio("recoleta", "Recoleta", com.google.android.gms.maps.model.LatLng(-34.5885, -58.3960), 1200.0, misionesActivas = 5, colorHex = "#00D4FF"),
        ZonaBarrio("centro", "Centro / Microcentro", com.google.android.gms.maps.model.LatLng(-34.6037, -58.3816), 1800.0, misionesActivas = 12, colorHex = "#FFB800"),
        ZonaBarrio("boedo", "Boedo", com.google.android.gms.maps.model.LatLng(-34.6291, -58.4189), 900.0, misionesActivas = 3, colorHex = "#27AE60"),
        ZonaBarrio("belgrano", "Belgrano", com.google.android.gms.maps.model.LatLng(-34.5605, -58.4584), 1400.0, misionesActivas = 6, colorHex = "#FF6B35"),
        ZonaBarrio("flores", "Flores", com.google.android.gms.maps.model.LatLng(-34.6289, -58.4629), 1100.0, misionesActivas = 4, colorHex = "#E74C3C")
    )
}
