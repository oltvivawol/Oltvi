package com.oltvi.jefe.screens.flota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.data.services.MockDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the fleet management screen.
 *
 * Loads the list of drivers available near the Buenos Aires city center from
 * [MockDataService] and re-fetches every 10 seconds to simulate live GPS
 * updates. Exposes filter state and a selected-driver detail panel.
 *
 * All data is sourced from [MockDataService.getDriversNear]; replace with a
 * real fleet repository when the backend is available.
 */
@HiltViewModel
class FlotaViewModel @Inject constructor(
    private val mockDataService: MockDataService,
) : ViewModel() {

    /** Status filter tokens understood by [conductoresFiltrados]. */
    object Filtros {
        const val TODOS = "todos"
        const val DISPONIBLES = "disponibles"
        const val EN_VIAJE = "en_viaje"
    }

    data class State(
        val conductores: List<ConductorDisponible> = emptyList(),
        /** Raw filter key — one of [Filtros] constants. */
        val filtroEstado: String = Filtros.TODOS,
        val filtroTipo: String = Filtros.TODOS,
        val conductorSeleccionado: ConductorDisponible? = null,
        val isLoading: Boolean = true,
    ) {
        /**
         * Derived list applying the current [filtroEstado].
         *
         * The mock service returns 8 drivers; the first 5 (sorted by proximity)
         * are considered "disponibles" and the remaining 3 "en_viaje".
         */
        val conductoresFiltrados: List<ConductorDisponible>
            get() = when (filtroEstado) {
                Filtros.DISPONIBLES -> conductores.take(5)
                Filtros.EN_VIAJE -> conductores.drop(5)
                else -> conductores
            }

        /** Number of drivers currently shown in the filtered list. */
        val totalFiltrados: Int get() = conductoresFiltrados.size
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    // Buenos Aires city center — the reference point passed to MockDataService.
    private val refLat = -34.6037
    private val refLng = -58.3816

    init {
        viewModelScope.launch {
            cargarFlota()
            // Simulate live GPS positions being updated from the server.
            while (true) {
                delay(10_000L)
                cargarFlota()
            }
        }
    }

    // ── Public surface ─────────────────────────────────────────────────────────

    /** Selects or deselects a driver for the detail panel. */
    fun seleccionarConductor(conductor: ConductorDisponible?) {
        _state.update { it.copy(conductorSeleccionado = conductor) }
    }

    /** Applies a status filter. Pass one of the [Filtros] constants. */
    fun setFiltroEstado(estado: String) {
        _state.update { it.copy(filtroEstado = estado) }
    }

    /** Applies a vehicle-type filter. Reserved for future UI; kept for parity with State. */
    fun setFiltroTipo(tipo: String) {
        _state.update { it.copy(filtroTipo = tipo) }
    }

    /** Manual refresh. */
    fun refresh() {
        viewModelScope.launch { cargarFlota() }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun cargarFlota() {
        val conductores = mockDataService.getDriversNear(refLat, refLng)
        _state.update { it.copy(conductores = conductores, isLoading = false) }
    }
}
