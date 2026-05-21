package com.oltvi.conductor.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.oltvi.conductor.screens.components.IntensidadDemanda
import com.oltvi.conductor.screens.components.ZonaCaliente
import com.oltvi.core.ai.orchestrator.OltviOrchestrator
import com.oltvi.core.data.models.EstadoServicio
import com.oltvi.core.data.models.NivelServicio
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.Servicio
import com.oltvi.core.data.models.SugerenciaCopiloto
import com.oltvi.core.data.models.TipoServicio
import com.oltvi.core.data.services.LocationService
import com.oltvi.core.data.services.MockDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

/** Logical driver ID used for mock orchestration calls. */
private const val DRIVER_ID = "driver-001"

/**
 * Buenos Aires neighbourhoods that act as "hot zones" the AgenteCopiloto might
 * surface — coordinates are realistic CABA centroids.
 */
private val HOT_ZONES_CABA: List<ZonaCaliente> = listOf(
    ZonaCaliente(PuntoGeo(-34.5797, -58.4325, "Palermo", "Palermo, CABA"),         IntensidadDemanda.ALTA),
    ZonaCaliente(PuntoGeo(-34.5875, -58.3938, "Recoleta", "Recoleta, CABA"),       IntensidadDemanda.MEDIA),
    ZonaCaliente(PuntoGeo(-34.6037, -58.3816, "Microcentro", "Microcentro, CABA"), IntensidadDemanda.ALTA),
    ZonaCaliente(PuntoGeo(-34.6230, -58.3890, "San Telmo", "San Telmo, CABA"),     IntensidadDemanda.BAJA),
    ZonaCaliente(PuntoGeo(-34.5534, -58.4597, "Belgrano", "Belgrano, CABA"),       IntensidadDemanda.MEDIA),
)

/**
 * Driver HUD ViewModel — central nervous system of the home screen.
 *
 * Drives:
 *  - Online/offline state (and the shift timer that ticks while online).
 *  - Location stream (via [LocationService]).
 *  - Daily earnings, trips and rating stats (mocked).
 *  - Copilot suggestion delivered by [OltviOrchestrator] when going online.
 *  - Incoming trip-request simulation while online (mock timer).
 *  - Heat-zone overlay data.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationService: LocationService,
    private val mockDataService: MockDataService,
    private val orchestrator: OltviOrchestrator,
) : ViewModel() {

    /**
     * Immutable UI state — all observable values live here so Compose can
     * recompose on a single StateFlow.
     */
    data class State(
        val isOnline: Boolean = false,
        val userLocation: LatLng? = null,
        val gananciasHoy: Double = 12_450.0,
        val viajesHoy: Int = 8,
        val ratingActual: Float = 4.85f,
        val tiempoActivo: Long = 0L, // milliseconds online since last toggle
        val sugerencia: SugerenciaCopiloto? = null,
        val solicitudEntrante: Servicio? = null,
        /** Last-known distance (km) from driver to incoming pickup. */
        val distanciaSolicitudKm: Double = 0.0,
        /** Display rating for the requesting passenger. */
        val ratingPasajero: Float = 4.7f,
        val zonasCalientes: List<ZonaCaliente> = HOT_ZONES_CABA,
        val transiciones: Int = 0, // for animation keys
        val errorMessage: String? = null,
    ) {
        val viajeAceptado: Servicio? = null
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    /** Emits true when the user has just accepted a trip; the screen observes
     * this to trigger navigation to OperacionScreen. */
    private val _navegarAOperacion = MutableStateFlow<Servicio?>(null)
    val navegarAOperacion: StateFlow<Servicio?> = _navegarAOperacion.asStateFlow()

    private var shiftTimerJob: Job? = null
    private var incomingRequestJob: Job? = null

    init {
        observeLocation()
    }

    // ── Online toggle ─────────────────────────────────────────────────────

    /**
     * Flip between ONLINE and OFFLINE. When going online we:
     *  1. Start the shift timer (1s tick).
     *  2. Ask the Orchestrator (AgenteCopiloto) for a starting suggestion.
     *  3. Schedule a mock incoming request after a few seconds.
     */
    fun toggleOnline() {
        val newOnline = !_state.value.isOnline
        _state.update { it.copy(isOnline = newOnline) }

        if (newOnline) {
            iniciarTurno()
        } else {
            cerrarTurno()
        }
    }

    private fun iniciarTurno() {
        // 1) Shift timer
        shiftTimerJob?.cancel()
        shiftTimerJob = viewModelScope.launch {
            val start = System.currentTimeMillis() - _state.value.tiempoActivo
            while (_state.value.isOnline) {
                _state.update { it.copy(tiempoActivo = System.currentTimeMillis() - start) }
                delay(1_000L)
            }
        }

        // 2) Copilot suggestion
        viewModelScope.launch {
            try {
                val pos = _state.value.userLocation?.let { PuntoGeo(it.latitude, it.longitude) }
                    ?: PuntoGeo(LocationService.fallback.latitude, LocationService.fallback.longitude)
                val sugerencia = orchestrator.iniciarTurnoConductor(
                    idConductor = DRIVER_ID,
                    posicion = pos,
                )
                _state.update { it.copy(sugerencia = sugerencia) }
            } catch (t: Throwable) {
                // Fall back to a hand-crafted suggestion so the HUD never feels dead.
                _state.update {
                    it.copy(
                        sugerencia = SugerenciaCopiloto(
                            titulo = "Movete a Palermo",
                            descripcion = "Alta demanda detectada — 8 solicitudes sin asignar en los últimos 5 min.",
                            zonaRecomendada = PuntoGeo(-34.5797, -58.4325, "Palermo"),
                            potencialGanancia = 4_500.0,
                            urgencia = "alta",
                            accionInmediata = "Dirigite ahora"
                        )
                    )
                }
            }
        }

        // 3) Mock incoming request stream
        scheduleNextIncomingRequest()
    }

    private fun cerrarTurno() {
        shiftTimerJob?.cancel()
        shiftTimerJob = null
        incomingRequestJob?.cancel()
        incomingRequestJob = null
        _state.update {
            it.copy(
                solicitudEntrante = null,
                sugerencia = null,
            )
        }
    }

    private fun scheduleNextIncomingRequest() {
        incomingRequestJob?.cancel()
        incomingRequestJob = viewModelScope.launch {
            // Wait 6–15 seconds before the next pretend ping.
            delay(6_000L + Random.nextLong(9_000L))
            if (!_state.value.isOnline) return@launch
            if (_state.value.solicitudEntrante != null) return@launch
            spawnMockRequest()
        }
    }

    private fun spawnMockRequest() {
        val origen = PuntoGeo(
            lat = -34.5797 + (Random.nextDouble() - 0.5) * 0.04,
            lng = -58.4325 + (Random.nextDouble() - 0.5) * 0.04,
            nombre = "Av. Santa Fe 3253",
            direccion = "Palermo, CABA"
        )
        val destino = PuntoGeo(
            lat = -34.6037 + (Random.nextDouble() - 0.5) * 0.05,
            lng = -58.3816 + (Random.nextDouble() - 0.5) * 0.05,
            nombre = "Microcentro",
            direccion = "Av. de Mayo 600, CABA"
        )
        val distanciaKm = 2.0 + Random.nextDouble() * 6.0
        val precio = 1_400.0 + 320.0 * distanciaKm + Random.nextInt(0, 400)

        val servicio = Servicio(
            id = "req-${UUID.randomUUID()}",
            tipo = TipoServicio.PASAJERO,
            nivel = if (Random.nextDouble() < 0.25) NivelServicio.PRIORITARIO else NivelServicio.ESTANDAR,
            origen = origen,
            destino = destino,
            idCliente = "user-${Random.nextInt(1, 9)}",
            idConductor = DRIVER_ID,
            estado = EstadoServicio.SOLICITADO,
            precio = precio,
            distanciaKm = distanciaKm,
            tiempoEstimadoMin = (distanciaKm * 2.4).toInt().coerceAtLeast(4),
            fechaCreacion = Instant.now()
        )
        _state.update {
            it.copy(
                solicitudEntrante = servicio,
                distanciaSolicitudKm = distanceKmFromUser(origen),
                ratingPasajero = (4.0f + Random.nextFloat() * 1.0f),
                transiciones = it.transiciones + 1,
            )
        }
    }

    // ── Accept / reject ───────────────────────────────────────────────────

    fun aceptarSolicitud(servicio: Servicio) {
        _state.update {
            it.copy(
                solicitudEntrante = null,
                gananciasHoy = it.gananciasHoy + servicio.precio,
                viajesHoy = it.viajesHoy + 1,
            )
        }
        _navegarAOperacion.value = servicio
    }

    fun rechazarSolicitud(servicio: Servicio) {
        _state.update { it.copy(solicitudEntrante = null) }
        if (_state.value.isOnline) {
            scheduleNextIncomingRequest()
        }
    }

    /** Called by the home screen after navigation has been consumed. */
    fun navegacionConsumida() {
        _navegarAOperacion.value = null
    }

    fun limpiarSugerencia() {
        _state.update { it.copy(sugerencia = null) }
    }

    fun aplicarSugerencia() {
        // Acknowledge — in production we'd centre the map on the recommended
        // zone. Here we simply dismiss and schedule another request fast.
        _state.update { it.copy(sugerencia = null) }
        if (_state.value.isOnline) scheduleNextIncomingRequest()
    }

    // ── Internals ─────────────────────────────────────────────────────────

    private fun observeLocation() {
        viewModelScope.launch {
            locationService.getLocationUpdates()
                .catch {
                    _state.update { st -> st.copy(userLocation = LocationService.fallback) }
                }
                .collect { latLng ->
                    _state.update { it.copy(userLocation = latLng) }
                }
        }
    }

    private fun distanceKmFromUser(point: PuntoGeo): Double {
        val user = _state.value.userLocation ?: LocationService.fallback
        // Simple equirectangular approximation — good enough for HUD distance.
        val dLat = (user.latitude - point.lat) * 111.0
        val dLng = (user.longitude - point.lng) * 111.0 * 0.82
        return kotlin.math.sqrt(dLat * dLat + dLng * dLng).let { abs(it) }
    }

    override fun onCleared() {
        shiftTimerJob?.cancel()
        incomingRequestJob?.cancel()
        super.onCleared()
    }
}
