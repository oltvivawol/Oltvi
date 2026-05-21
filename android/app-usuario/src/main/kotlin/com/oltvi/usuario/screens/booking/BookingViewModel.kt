package com.oltvi.usuario.screens.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oltvi.core.ai.orchestrator.OltviOrchestrator
import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.NivelServicio
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.SolicitudViajeResultado
import com.oltvi.core.data.models.TipoServicio
import com.oltvi.core.data.services.LocationService
import com.oltvi.core.data.services.MockDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Optional dedicated ViewModel for use inside [BookingSheet] when launched
 * standalone (outside the [HomeScreen] flow). Mirrors the booking sub-state
 * that [com.oltvi.usuario.screens.home.HomeViewModel] manages, so the booking
 * flow can be reused on its own route if needed.
 */
data class BookingUiState(
    val origen: PuntoGeo? = null,
    val destino: PuntoGeo? = null,
    val nivel: NivelServicio = NivelServicio.ESTANDAR,
    val tipo: TipoServicio = TipoServicio.PASAJERO,
    val nearbyDrivers: List<ConductorDisponible> = emptyList(),
    val resultado: SolicitudViajeResultado? = null,
    val isCalculating: Boolean = false,
    val isConfirmed: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val locationService: LocationService,
    private val mockDataService: MockDataService,
    private val orchestrator: OltviOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state.asStateFlow()

    private val userId: String = "user-001"

    init {
        viewModelScope.launch {
            val loc = locationService.getCurrentLocation() ?: LocationService.fallback
            val drivers = mockDataService.getDriversNear(loc.latitude, loc.longitude)
            _state.update {
                it.copy(
                    origen = PuntoGeo(loc.latitude, loc.longitude, "Mi ubicación"),
                    nearbyDrivers = drivers
                )
            }
        }
    }

    fun setDestino(destino: PuntoGeo) {
        _state.update { it.copy(destino = destino, error = null) }
        calcular()
    }

    fun setNivel(nivel: NivelServicio) {
        _state.update { it.copy(nivel = nivel, resultado = null) }
        calcular()
    }

    fun setTipo(tipo: TipoServicio) {
        _state.update { it.copy(tipo = tipo, resultado = null) }
        calcular()
    }

    private fun calcular() {
        val current = _state.value
        val origen = current.origen ?: return
        val destino = current.destino ?: return

        viewModelScope.launch {
            _state.update { it.copy(isCalculating = true, error = null) }
            try {
                val contexto = ContextoViaje(
                    idUsuario = userId,
                    origen = origen,
                    destino = destino,
                    tipoServicio = current.tipo
                )
                val resultado = orchestrator.procesarSolicitudViaje(
                    contexto = contexto,
                    conductoresDisponibles = current.nearbyDrivers
                )
                _state.update { it.copy(resultado = resultado, isCalculating = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "No pudimos calcular el viaje. Intentá de nuevo.",
                        isCalculating = false
                    )
                }
            }
        }
    }

    fun confirmar() {
        _state.update { it.copy(isConfirmed = true) }
    }

    fun reset() {
        _state.update {
            it.copy(
                destino = null,
                resultado = null,
                isCalculating = false,
                isConfirmed = false,
                error = null
            )
        }
    }
}
