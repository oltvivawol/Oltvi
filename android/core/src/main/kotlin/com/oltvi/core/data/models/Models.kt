package com.oltvi.core.data.models

import java.time.Instant

// ── Enums ──────────────────────────────────────────────────────────────────

enum class TipoServicio(val displayName: String, val emoji: String) {
    PASAJERO("Viaje", "🚗"),
    MENSAJERIA("Mensajería", "📦"),
    CARGA("Carga", "🚛"),
    VIAL("Reporte Vial", "🚧")
}

enum class NivelServicio(val displayName: String, val priceMultiplier: Double) {
    ESTANDAR("Estándar", 1.0),
    PRIORITARIO("Prioritario", 1.3),
    EXPRES("Exprés", 1.6),
    ESPECIAL("Especial", 2.0)
}

enum class EstadoServicio(val displayName: String, val colorHex: String) {
    SOLICITADO("Buscando", "#F1C40F"),
    ASIGNADO("Asignado", "#3498DB"),
    EN_CAMINO("En camino", "#E67E22"),
    LLEGANDO("Llegando", "#F39C12"),
    EN_RUTA("En ruta", "#9B59B6"),
    ENTREGADO("Completado", "#27AE60"),
    CANCELADO("Cancelado", "#E74C3C")
}

enum class TipoPerfil { CLIENTE, CONDUCTOR, ADMIN }

enum class NivelUsuario(val displayName: String, val puntosRequeridos: Int, val colorHex: String) {
    INICIADO("Iniciado", 0, "#95A5A6"),
    COLABORADOR("Colaborador", 500, "#3498DB"),
    EXPERTO("Experto", 2000, "#9B59B6"),
    LIDER("Líder", 5000, "#E67E22"),
    LEYENDA("Leyenda", 15000, "#F1C40F")
}

enum class SeveridadAlerta { LOW, MEDIUM, HIGH, CRITICAL }

enum class TipoInsight { DEMANDA, FLOTA, REVENUE, SEGURIDAD, GENERAL }

enum class AccionFraude { APROBAR, REVISAR, BLOQUEAR }

enum class EstadoVehiculo(val displayName: String) {
    OPERATIVO("Operativo"),
    MANTENIMIENTO("En mantenimiento"),
    FUERA_DE_SERVICIO("Fuera de servicio")
}

enum class TipoVehiculo(val displayName: String) {
    AUTO("Auto"),
    MOTO("Moto"),
    UTILITARIO("Utilitario"),
    CAMION("Camión")
}

enum class TipoMensaje { TEXT, OPTION, CARD }

// ── Data Classes ───────────────────────────────────────────────────────────

data class PuntoGeo(
    val latitud: Double,
    val longitud: Double,
    val nombre: String = "",
    val direccion: String = ""
)

data class Servicio(
    val id: String,
    val tipo: TipoServicio,
    val nivel: NivelServicio = NivelServicio.ESTANDAR,
    val origen: PuntoGeo,
    val destino: PuntoGeo,
    val idCliente: String,
    val idConductor: String? = null,
    val nombreConductor: String? = null,
    val matriculaVehiculo: String? = null,
    val estado: EstadoServicio = EstadoServicio.SOLICITADO,
    val precio: Double,
    val distanciaKm: Double,
    val tiempoEstimadoMin: Int,
    val fechaCreacion: Instant = Instant.now(),
    val puntosRuta: List<PuntoGeo> = emptyList(),
    val detalles: String? = null
)

data class Usuario(
    val id: String,
    val perfil: TipoPerfil,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val fotoUrl: String? = null,
    val nivel: NivelUsuario = NivelUsuario.INICIADO,
    val puntos: Int = 0,
    val rating: Double = 5.0,
    val verificado: Boolean = false,
    val fechaRegistro: Instant = Instant.now()
) {
    val inicial: String get() = nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

data class Vehiculo(
    val id: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val patente: String,
    val color: String,
    val tipo: TipoVehiculo,
    val estado: EstadoVehiculo = EstadoVehiculo.OPERATIVO,
    val capacidadPasajeros: Int = 4,
    val idConductor: String? = null
) {
    val descripcionCorta: String get() = "$marca $modelo · $patente"
}

data class EventoVial(
    val id: String,
    val tipo: String,
    val descripcion: String,
    val ubicacion: PuntoGeo,
    val reportadoPor: String,
    val activo: Boolean = true,
    val fechaReporte: Instant = Instant.now()
)

data class ConductorDisponible(
    val idConductor: String,
    val nombre: String,
    val rating: Double,
    val distanciaMetros: Double,
    val tiempoEstimadoMin: Int,
    val vehiculo: Vehiculo,
    val posicion: PuntoGeo,
    val scoreFit: Double = 0.0,
    val fotoUrl: String? = null
)

data class ResultadoMatchmaking(
    val conductorElegido: ConductorDisponible,
    val alternativas: List<ConductorDisponible> = emptyList(),
    val explicacionIA: String,
    val confianza: Float = 0.9f
)

data class ResultadoPrecio(
    val precioFinal: Double,
    val desglose: Map<String, Double>,
    val explicacionIA: String,
    val factorDemanda: Double = 1.0,
    val factorClima: String = "Normal"
)

data class AlertaSeguridad(
    val tipo: String,
    val severidad: SeveridadAlerta,
    val descripcion: String,
    val accionRecomendada: String,
    val timestamp: Instant = Instant.now()
)

data class InsightOperaciones(
    val tipo: TipoInsight,
    val titulo: String,
    val descripcion: String,
    val impactoEstimado: String,
    val prioridad: Int = 1
)

data class MensajeChat(
    val id: String,
    val contenido: String,
    val esIA: Boolean,
    val timestamp: Instant = Instant.now(),
    val tipo: TipoMensaje = TipoMensaje.TEXT,
    val opciones: List<String> = emptyList()
)

data class ContextoViaje(
    val idViaje: String? = null,
    val idUsuario: String? = null,
    val idConductor: String? = null,
    val origen: PuntoGeo? = null,
    val destino: PuntoGeo? = null,
    val estadoActual: EstadoServicio? = null,
    val tipoServicio: TipoServicio? = null,
    val precioEstimado: Double? = null,
    val historialPosiciones: List<PuntoGeo> = emptyList(),
    val metadatos: Map<String, String> = emptyMap()
)

data class FraudeResultado(
    val scoreRiesgo: Int,
    val accion: AccionFraude,
    val explicacion: String
)

data class SugerenciaCopiloto(
    val titulo: String,
    val descripcion: String,
    val zonaRecomendada: PuntoGeo? = null,
    val potencialGanancia: Double = 0.0,
    val urgencia: String = "normal"
)

data class RutaOptimizada(
    val puntos: List<PuntoGeo>,
    val etaMin: Int,
    val distanciaKm: Double,
    val trafico: String = "Normal",
    val alternativas: List<RutaOptimizada> = emptyList()
)

data class SolicitudViajeResultado(
    val matchmaking: ResultadoMatchmaking,
    val precio: ResultadoPrecio,
    val ruta: RutaOptimizada
)

data class MonitoreoResultado(
    val alerta: AlertaSeguridad?,
    val rutaActualizada: RutaOptimizada?,
    val enRutaSegura: Boolean
)
