package com.oltvi.usuario.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.oltvi.core.ai.orchestrator.OltviOrchestrator
import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.SolicitudViajeResultado
import com.oltvi.core.data.models.TipoServicio
import com.oltvi.core.data.services.LocationService
import com.oltvi.core.data.services.MockDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userLocation: LatLng? = null,
    val nearbyDrivers: List<ConductorDisponible> = emptyList(),
    val isLoading: Boolean = false,
    val solicitudResultado: SolicitudViajeResultado? = null,
    val showBookingSheet: Boolean = false,
    val destino: PuntoGeo? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationService: LocationService,
    private val mockDataService: MockDataService,
    private val orchestrator: OltviOrchestrator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadLocation()
        loadNearbyDrivers()
    }

    private fun loadLocation() {
        viewModelScope.launch {
            locationService.getLocationUpdates()
                .catch { _uiState.update { it.copy(userLocation = LocationService.fallback) } }
                .collect { location ->
                    _uiState.update { state -> state.copy(userLocation = location) }
                }
        }
    }

    private fun loadNearbyDrivers() {
        viewModelScope.launch {
            val location = _uiState.value.userLocation ?: LocationService.fallback
            val drivers = mockDataService.getDriversNear(location.latitude, location.longitude)
            _uiState.update { it.copy(nearbyDrivers = drivers) }
        }
    }

    fun setDestino(destino: PuntoGeo) {
        _uiState.update { it.copy(destino = destino, showBookingSheet = true, solicitudResultado = null) }
        requestRide()
    }

    fun requestRide() {
        val state = _uiState.value
        val destino = state.destino ?: return
        val userLoc = state.userLocation ?: LocationService.fallback

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val contexto = ContextoViaje(
                    idUsuario = "user-001",
                    origen = PuntoGeo(userLoc.latitude, userLoc.longitude, "Mi ubicación"),
                    destino = destino,
                    tipoServicio = TipoServicio.PASAJERO
                )
                val resultado = orchestrator.procesarSolicitudViaje(
                    contexto = contexto,
                    conductoresDisponibles = state.nearbyDrivers
                )
                _uiState.update { it.copy(solicitudResultado = resultado, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error inesperado", isLoading = false) }
            }
        }
    }

    fun showBookingSheet(show: Boolean) {
        _uiState.update { it.copy(showBookingSheet = show) }
    }

    fun dismissBookingSheet() {
        _uiState.update {
            it.copy(
                showBookingSheet = false,
                destino = null,
                solicitudResultado = null,
                error = null
            )
        }
    }

    fun confirmRide() {
        // In production: create the ride in Firebase, transition to TrackingScreen
        _uiState.update { it.copy(showBookingSheet = false) }
    }
}
