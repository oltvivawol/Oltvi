package com.oltvi.jefe.screens.incidentes

import androidx.lifecycle.ViewModel
import com.oltvi.core.data.models.EstadoEventoVial
import com.oltvi.core.data.models.EventoVial
import com.oltvi.core.data.models.SeveridadAlerta
import com.oltvi.core.data.services.MockDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the road-incidents screen.
 *
 * Loads incidents from [MockDataService.getEventosViales] which returns the
 * canonical [EventoVial] model (with [com.oltvi.core.data.models.TipoEventoVial],
 * [EstadoEventoVial], and [SeveridadAlerta] fields). Exposes a severity filter
 * and mutations to resolve or dismiss individual incidents.
 */
@HiltViewModel
class IncidentesViewModel @Inject constructor(
    private val mockDataService: MockDataService,
) : ViewModel() {

    data class State(
        val incidentes: List<EventoVial> = emptyList(),
        /** When non-null, only incidents with this severity are shown. */
        val filtroSeveridad: SeveridadAlerta? = null,
        val isLoading: Boolean = true,
    ) {
        /** Derived list applying the current [filtroSeveridad]. */
        val incidentesFiltrados: List<EventoVial>
            get() = if (filtroSeveridad == null) incidentes
            else incidentes.filter { it.severidad == filtroSeveridad }

        val totalActivos: Int
            get() = incidentes.count {
                it.estado == EstadoEventoVial.REPORTADO ||
                        it.estado == EstadoEventoVial.VERIFICANDO ||
                        it.estado == EstadoEventoVial.CONFIRMADO
            }

        val totalResueltos: Int
            get() = incidentes.count {
                it.estado == EstadoEventoVial.RESUELTO ||
                        it.estado == EstadoEventoVial.DESCARTADO
            }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        _state.update { it.copy(incidentes = mockDataService.getEventosViales(), isLoading = false) }
    }

    // ── Public surface ─────────────────────────────────────────────────────────

    /** Applies a severity filter; pass `null` to show all incidents. */
    fun setFiltro(severidad: SeveridadAlerta?) {
        _state.update { it.copy(filtroSeveridad = severidad) }
    }

    /**
     * Marks the incident with [id] as [EstadoEventoVial.RESUELTO].
     * The row remains in the list so the admin can see it has been handled.
     */
    fun resolverIncidente(id: String) {
        _state.update { s ->
            s.copy(
                incidentes = s.incidentes.map { ev ->
                    if (ev.id == id) ev.copy(estado = EstadoEventoVial.RESUELTO) else ev
                }
            )
        }
    }

    /**
     * Marks the incident with [id] as [EstadoEventoVial.DESCARTADO] and
     * removes it from the list (false positive — no further action needed).
     */
    fun descartarIncidente(id: String) {
        _state.update { s ->
            s.copy(incidentes = s.incidentes.filter { it.id != id })
        }
    }

}
