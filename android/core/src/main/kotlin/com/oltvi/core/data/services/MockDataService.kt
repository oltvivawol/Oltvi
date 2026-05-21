package com.oltvi.core.data.services

import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.data.models.EstadoServicio
import com.oltvi.core.data.models.EstadoVehiculo
import com.oltvi.core.data.models.NivelServicio
import com.oltvi.core.data.models.NivelUsuario
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.ResultadoMatchmaking
import com.oltvi.core.data.models.ResultadoPrecio
import com.oltvi.core.data.models.RutaOptimizada
import com.oltvi.core.data.models.Servicio
import com.oltvi.core.data.models.SolicitudViajeResultado
import com.oltvi.core.data.models.TipoPerfil
import com.oltvi.core.data.models.TipoServicio
import com.oltvi.core.data.models.TipoVehiculo
import com.oltvi.core.data.models.Usuario
import com.oltvi.core.data.models.Vehiculo
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

// Buenos Aires default coordinates
private const val BA_LAT = -34.6037
private const val BA_LNG = -58.3816

@Singleton
class MockDataService @Inject constructor() {

    // ── Predefined conductors ──────────────────────────────────────────────

    private val conductoresMock = listOf(
        DriverDef("driver-001", "Carlos Rodríguez", 4.8, "Toyota", "Corolla", 2022, "AB123CD", "Gris"),
        DriverDef("driver-002", "María González", 4.7, "Honda", "City", 2021, "EF456GH", "Blanco"),
        DriverDef("driver-003", "Diego Fernández", 4.9, "Volkswagen", "Polo", 2023, "IJ789KL", "Negro"),
        DriverDef("driver-004", "Lucía Martínez", 4.5, "Chevrolet", "Cruze", 2020, "MN012OP", "Plateado"),
        DriverDef("driver-005", "Andrés López", 4.6, "Ford", "Focus", 2021, "QR345ST", "Azul"),
        DriverDef("driver-006", "Valentina García", 4.8, "Nissan", "Sentra", 2022, "UV678WX", "Rojo"),
        DriverDef("driver-007", "Pablo Sánchez", 4.2, "Renault", "Logan", 2019, "YZ901AB", "Beige"),
        DriverDef("driver-008", "Camila Díaz", 4.7, "Toyota", "Yaris", 2023, "CD234EF", "Blanco Perla")
    )

    // ── Mock users ─────────────────────────────────────────────────────────

    private val usuariosMock = listOf(
        Usuario(
            id = "user-001",
            perfil = TipoPerfil.CLIENTE,
            nombre = "Alejandro Torres",
            correo = "alejandro.torres@gmail.com",
            telefono = "+54 11 5555-1234",
            nivel = NivelUsuario.EXPERTO,
            puntos = 3750,
            rating = 4.8,
            verificado = true
        ),
        Usuario(
            id = "user-002",
            perfil = TipoPerfil.CLIENTE,
            nombre = "Sofía Romero",
            correo = "sofia.romero@outlook.com",
            telefono = "+54 11 4444-5678",
            nivel = NivelUsuario.COLABORADOR,
            puntos = 820,
            rating = 4.6,
            verificado = true
        ),
        Usuario(
            id = "user-003",
            perfil = TipoPerfil.CLIENTE,
            nombre = "Martín Pérez",
            correo = "martin.perez@gmail.com",
            telefono = "+54 11 3333-9012",
            nivel = NivelUsuario.INICIADO,
            puntos = 150,
            rating = 4.9,
            verificado = false
        ),
        Usuario(
            id = "user-004",
            perfil = TipoPerfil.ADMIN,
            nombre = "Laura Vega",
            correo = "laura.vega@oltvi.com",
            telefono = "+54 11 2222-3456",
            nivel = NivelUsuario.LEYENDA,
            puntos = 25000,
            rating = 5.0,
            verificado = true
        ),
        Usuario(
            id = "user-005",
            perfil = TipoPerfil.CLIENTE,
            nombre = "Facundo Méndez",
            correo = "facundo.mendez@yahoo.com.ar",
            telefono = "+54 11 1111-7890",
            nivel = NivelUsuario.LIDER,
            puntos = 7200,
            rating = 4.7,
            verificado = true
        )
    )

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Returns all 8 mock conductors with realistic distances/ETAs computed
     * from the given lat/lng reference point.
     */
    fun getDriversNear(lat: Double, lng: Double): List<ConductorDisponible> {
        // Fixed offsets to make distances deterministic per driver
        val offsets = listOf(
            Pair(0.0032, -0.0041),
            Pair(-0.0055, 0.0028),
            Pair(0.0071, 0.0019),
            Pair(-0.0024, -0.0062),
            Pair(0.0088, -0.0033),
            Pair(-0.0011, 0.0075),
            Pair(0.0049, 0.0052),
            Pair(-0.0067, -0.0018)
        )

        return conductoresMock.mapIndexed { i, def ->
            val (dLat, dLng) = offsets[i]
            val driverLat = lat + dLat
            val driverLng = lng + dLng
            val distanciaMetros = calcularDistanciaMetros(
                lat, lng, driverLat, driverLng
            )
            val etaMin = (distanciaMetros / 400.0).toInt().coerceIn(2, 18)

            ConductorDisponible(
                idConductor = def.id,
                nombre = def.nombre,
                rating = def.rating,
                distanciaMetros = distanciaMetros,
                tiempoEstimadoMin = etaMin,
                vehiculo = Vehiculo(
                    id = "veh-${def.id}",
                    marca = def.marca,
                    modelo = def.modelo,
                    anio = def.anio,
                    patente = def.patente,
                    color = def.color,
                    tipo = TipoVehiculo.AUTO,
                    estado = EstadoVehiculo.OPERATIVO,
                    capacidadPasajeros = 4,
                    idConductor = def.id
                ),
                posicion = PuntoGeo(driverLat, driverLng, def.nombre),
                scoreFit = 0.0
            )
        }.sortedBy { it.distanciaMetros }
    }

    /**
     * Creates a service request with Haversine-based distance, price and ETA.
     */
    fun createServiceRequest(
        origen: PuntoGeo,
        destino: PuntoGeo,
        tipo: TipoServicio
    ): Servicio {
        val distanciaKm = calcularDistanciaKm(origen, destino).coerceAtLeast(0.5)
        val precio = (800.0 + 120.0 * distanciaKm) * tipo.priceMultiplierFor()
        val etaMin = (distanciaKm / 30.0 * 60.0).toInt().coerceAtLeast(2)
        val conductor = conductoresMock.first()

        return Servicio(
            id = UUID.randomUUID().toString(),
            tipo = tipo,
            nivel = NivelServicio.ESTANDAR,
            origen = origen,
            destino = destino,
            idCliente = "user-001",
            idConductor = conductor.id,
            nombreConductor = conductor.nombre,
            matriculaVehiculo = conductor.patente,
            estado = EstadoServicio.SOLICITADO,
            precio = precio,
            distanciaKm = distanciaKm,
            tiempoEstimadoMin = etaMin
        )
    }

    fun getUserById(id: String): Usuario? = usuariosMock.find { it.id == id }

    /**
     * Returns 5 active services in different states (realistic Buenos Aires data).
     */
    fun getActiveServices(): List<Servicio> {
        val now = Instant.now()
        val conductores = conductoresMock
        val estados = listOf(
            EstadoServicio.SOLICITADO,
            EstadoServicio.EN_CAMINO,
            EstadoServicio.EN_RUTA,
            EstadoServicio.LLEGANDO,
            EstadoServicio.ENTREGADO
        )
        val rutas = listOf(
            Pair(PuntoGeo(BA_LAT, BA_LNG, "Microcentro"), PuntoGeo(-34.5797, -58.4325, "Palermo")),
            Pair(PuntoGeo(-34.5875, -58.3938, "Recoleta"), PuntoGeo(-34.6214, -58.3731, "San Telmo")),
            Pair(PuntoGeo(-34.5607, -58.4531, "Belgrano"), PuntoGeo(BA_LAT, BA_LNG, "Microcentro")),
            Pair(PuntoGeo(-34.6183, -58.4417, "Caballito"), PuntoGeo(-34.5534, -58.4597, "Belgrano")),
            Pair(PuntoGeo(-34.6230, -58.3890, "San Telmo"), PuntoGeo(-34.5885, -58.4260, "Palermo Soho"))
        )

        return rutas.mapIndexed { i, (origen, destino) ->
            val c = conductores[i]
            val distanciaKm = calcularDistanciaKm(origen, destino)
            val precio = 800.0 + 120.0 * distanciaKm
            Servicio(
                id = "active-00${i + 1}",
                tipo = TipoServicio.PASAJERO,
                nivel = NivelServicio.ESTANDAR,
                origen = origen,
                destino = destino,
                idCliente = "user-00${(i % 5) + 1}",
                idConductor = c.id,
                nombreConductor = c.nombre,
                matriculaVehiculo = c.patente,
                estado = estados[i],
                precio = precio,
                distanciaKm = distanciaKm,
                tiempoEstimadoMin = (distanciaKm / 30.0 * 60.0).toInt().coerceAtLeast(3),
                fechaCreacion = now.minusSeconds((i * 900).toLong())
            )
        }
    }

    // ── Backward-compat helpers used by old code ───────────────────────────

    fun getMockUser(): Usuario = usuariosMock.first()

    fun buildMockSolicitudResultado(destino: PuntoGeo): SolicitudViajeResultado {
        val drivers = getDriversNear(BA_LAT, BA_LNG)
        val elegido = drivers.first()
        val distanciaKm = calcularDistanciaKm(
            PuntoGeo(BA_LAT, BA_LNG), destino
        ).coerceAtLeast(0.5)
        val precio = 800.0 + 120.0 * distanciaKm
        return SolicitudViajeResultado(
            matchmaking = ResultadoMatchmaking(
                conductorElegido = elegido,
                alternativas = drivers.drop(1).take(2),
                explicacionIA = "Seleccioné a ${elegido.nombre} por su rating de ${elegido.rating}⭐ " +
                        "y cercanía de ${"%.0f".format(elegido.distanciaMetros)}m — la mejor combinación disponible.",
                confianza = 0.94f
            ),
            precio = ResultadoPrecio(
                precioFinal = precio,
                desglose = mapOf(
                    "Precio base" to 800.0,
                    "Distancia (${"%.1f".format(distanciaKm)} km)" to (120.0 * distanciaKm),
                    "Factor demanda (×1.1)" to precio * 0.1,
                    "Descuento usuario" to -(precio * 0.05)
                ),
                explicacionIA = "Precio de \$${"%.0f".format(precio)} ARS para ${"%.1f".format(distanciaKm)} km con demanda normal.",
                factorDemanda = 1.1,
                factorClima = "Despejado"
            ),
            ruta = RutaOptimizada(
                puntos = listOf(
                    PuntoGeo(BA_LAT, BA_LNG, "Origen"),
                    PuntoGeo(BA_LAT + 0.005, BA_LNG + 0.003, "Punto intermedio"),
                    destino
                ),
                etaMin = elegido.tiempoEstimadoMin + (distanciaKm / 30.0 * 60.0).toInt().coerceAtLeast(3),
                distanciaKm = distanciaKm,
                trafico = "Normal"
            )
        )
    }

    fun getEventosViales(): List<EventoVial> {
        val now = java.time.Instant.now()
        return listOf(
            EventoVial("ev1", TipoEventoVial.BACHE, "Bache profundo en la calzada", PuntoGeo(-34.6037, -58.3816, "Av. Corrientes"), "usr1", now.minusSeconds(1800), EstadoEventoVial.CONFIRMADO, SeveridadAlerta.MEDIA),
            EventoVial("ev2", TipoEventoVial.OBRA, "Obra de gas, carril izquierdo cortado", PuntoGeo(-34.6100, -58.3900, "Santa Fe y Callao"), "usr2", now.minusSeconds(3600), EstadoEventoVial.REPORTADO, SeveridadAlerta.ALTA),
            EventoVial("ev3", TipoEventoVial.ACCIDENTE, "Choque entre dos vehículos. Tránsito lento.", PuntoGeo(-34.5980, -58.3750, "Autopista 25 de Mayo"), "usr3", now.minusSeconds(600), EstadoEventoVial.VERIFICANDO, SeveridadAlerta.ALTA),
            EventoVial("ev4", TipoEventoVial.CORTE_TOTAL, "Marcha política — calle cerrada", PuntoGeo(-34.6150, -58.3700, "9 de Julio y Diagonal Norte"), "usr1", now.minusSeconds(7200), EstadoEventoVial.CONFIRMADO, SeveridadAlerta.CRITICA),
            EventoVial("ev5", TipoEventoVial.INUNDACION, "Agua acumulada, tránsito dificultoso", PuntoGeo(-34.6200, -58.4000, "Av. San Juan"), "usr4", now.minusSeconds(2400), EstadoEventoVial.REPORTADO, SeveridadAlerta.MEDIA),
            EventoVial("ev6", TipoEventoVial.OTRO, "Semáforo fuera de servicio", PuntoGeo(-34.6050, -58.3850, "Av. Rivadavia y Pueyrredón"), "usr2", now.minusSeconds(900), EstadoEventoVial.CONFIRMADO, SeveridadAlerta.BAJA),
        )
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private fun calcularDistanciaMetros(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = (lat1 - lat2) * 111000.0
        val dLng = (lng1 - lng2) * 111000.0 * 0.82
        return sqrt(dLat.pow(2) + dLng.pow(2))
    }

    private fun calcularDistanciaKm(a: PuntoGeo, b: PuntoGeo): Double {
        val dLat = (a.lat - b.lat) * 111.0
        val dLng = (a.lng - b.lng) * 111.0 * 0.82
        return sqrt(dLat.pow(2) + dLng.pow(2))
    }

    private fun TipoServicio.priceMultiplierFor(): Double = when (this) {
        TipoServicio.PASAJERO -> 1.0
        TipoServicio.MENSAJERIA -> 0.8
        TipoServicio.CARGA -> 1.5
        TipoServicio.VIAL -> 0.5
    }
}

private data class DriverDef(
    val id: String,
    val nombre: String,
    val rating: Double,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val patente: String,
    val color: String
)
