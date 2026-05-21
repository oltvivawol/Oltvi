package com.oltvi.core.data.models

import com.google.android.gms.maps.model.LatLng
import java.time.Instant

// -----------------------------------------------------------------------------
// Enums
// -----------------------------------------------------------------------------

enum class TipoServicio(val displayName: String) {
    PASAJERO("Pasajero"),
    MENSAJERIA("Mensajería"),
    CARGA("Carga"),
    VIAL("Reporte Vial")
}

enum class NivelServicio(val displayName: String, val priceMultiplier: Double) {
    ESTANDAR("Estándar", 1.0),
    PRIORITARIO("Prioritario", 1.3),
    EXPRES("Exprés", 1.6),
    ESPECIAL("Especial", 2.2)
}

enum class EstadoServicio(val displayName: String, val colorHex: String) {
    SOLICITADO("Solicitado", "#F1C40F"),
    ASIGNADO("Asignado", "#F39C12"),
    EN_CAMINO("En camino", "#E67E22"),
    LLEGANDO("Llegando", "#E67E22"),
    EN_RUTA("En ruta", "#27AE60"),
    ENTREGADO("Entregado", "#27AE60"),
    CANCELADO("Cancelado", "#E74C3C")
}

enum class TipoPerfil { CLIENTE, CONDUCTOR, ADMIN }

enum class NivelUsuario(val displayName: String, val minPoints: Int) {
    INICIADO("Iniciado", 0),
    COLABORADOR("Colaborador", 250),
    EXPERTO("Experto", 1000),
    LIDER("Líder", 2500),
    LEYENDA("Leyenda", 5000)
}

enum class TipoVehiculo(val displayName: String) {
    AUTO("Auto"),
    MOTO("Moto"),
    UTILITARIO("Utilitario"),
    CAMION("Camión"),
    CAMION_FRIO("Camión Frío")
}

enum class EstadoVehiculo { OPERATIVO, MANTENIMIENTO, FUERA_DE_SERVICIO }

enum class SeveridadAlerta { BAJA, MEDIA, ALTA, CRITICA }

enum class TipoInsight { DEMANDA, FLOTA, REVENUE, SEGURIDAD, GENERAL }

enum class AccionFraude { APROBAR, REVISAR, BLOQUEAR }

enum class TipoMensaje { TEXTO, OPCION, TARJETA, AUDIO }

enum class TipoEventoVial(val displayName: String, val icon: String) {
    BACHE("Bache", "🕳️"),
    OBRA("Obra", "🚧"),
    CORTE_TOTAL("Corte total", "⛔"),
    ACCIDENTE("Accidente", "🚨"),
    INUNDACION("Inundación", "🌊"),
    OTRO("Otro", "ℹ️")
}

enum class EstadoEventoVial { REPORTADO, VERIFICANDO, CONFIRMADO, RESUELTO, DESCARTADO }

// -----------------------------------------------------------------------------
// Geo
// -----------------------------------------------------------------------------

data class PuntoGeo(
    val lat: Double,
    val lng: Double,
    val nombre: String = "",
    val direccion: String = ""
) {
    fun toLatLng(): LatLng = LatLng(lat, lng)
}

// -----------------------------------------------------------------------------
// Domain models
// -----------------------------------------------------------------------------

data class Servicio(
    val id: String,
    val tipo: TipoServicio,
    val nivel: NivelServicio,
    val origen: PuntoGeo,
    val destino: PuntoGeo,
    val idCliente: String,
    val idConductor: String? = null,
    val nombreConductor: String? = null,
    val matriculaVehiculo: String? = null,
    val estado: EstadoServicio,
    val precio: Double,
    val distanciaKm: Double,
    val tiempoEstimadoMin: Int,
    val fechaCreacion: Instant,
    val fechaProgramada: Instant? = null,
    val detalles: String? = null,
    val puntosRuta: List<PuntoGeo> = emptyList()
)

data class Usuario(
    val id: String,
    val perfil: TipoPerfil,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val documento: String? = null,
    val fotoUrl: String? = null,
    val nivel: NivelUsuario,
    val puntos: Int,
    val rating: Float,
    val verificado: Boolean,
    val fechaRegistro: Instant
) {
    val inicial: String
        get() = nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

data class Vehiculo(
    val id: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val patente: String,
    val color: String,
    val tipo: TipoVehiculo,
    val estado: EstadoVehiculo,
    val capacidadKg: Double,
    val capacidadPasajeros: Int,
    val idConductor: String? = null,
    val vencimientoSeguro: Instant,
    val vencimientoVtv: Instant,
    val consumoPromedioKmL: Double
)

data class EventoVial(
    val id: String,
    val tipo: TipoEventoVial,
    val descripcion: String,
    val ubicacion: PuntoGeo,
    val reportadoPor: String,
    val fechaReporte: Instant,
    val estado: EstadoEventoVial,
    val severidad: SeveridadAlerta
)

data class ConductorDisponible(
    val idConductor: String,
    val nombre: String,
    val rating: Float,
    val distanciaMetros: Double,
    val tiempoEstimadoMin: Int,
    val vehiculo: Vehiculo,
    val posicion: PuntoGeo,
    val scoreFit: Float,
    val tasaAceptacion: Float
)

// -----------------------------------------------------------------------------
// AI results
// -----------------------------------------------------------------------------

data class ResultadoMatchmaking(
    val conductorElegido: ConductorDisponible,
    val alternativas: List<ConductorDisponible>,
    val explicacionIA: String,
    val confianza: Float
)

data class ResultadoPrecio(
    val precioFinal: Double,
    val desglose: Map<String, Double>,
    val explicacionIA: String,
    val factorDemanda: Double,
    val factorClima: String
)

data class AlertaSeguridad(
    val tipo: String,
    val severidad: SeveridadAlerta,
    val descripcion: String,
    val accionRecomendada: String,
    val timestamp: Instant
)

data class InsightOperaciones(
    val tipo: TipoInsight,
    val titulo: String,
    val descripcion: String,
    val impactoEstimado: String,
    val prioridad: SeveridadAlerta,
    val accionSugerida: String
)

data class MensajeChat(
    val id: String,
    val contenido: String,
    val esIA: Boolean,
    val timestamp: Instant,
    val tipo: TipoMensaje,
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
    val explicacion: String,
    val factoresRiesgo: List<String>
)

data class SugerenciaCopiloto(
    val titulo: String,
    val descripcion: String,
    val zonaRecomendada: PuntoGeo?,
    val potencialGanancia: Double,
    val urgencia: String,
    val accionInmediata: String?
)

data class RutaOptimizada(
    val puntos: List<PuntoGeo>,
    val etaMin: Int,
    val distanciaKm: Double,
    val trafico: String,
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

data class GananciasResumen(
    val total: Double,
    val viajes: Int,
    val propinas: Double,
    val horas: Double,
    val promedioPorViaje: Double
)
