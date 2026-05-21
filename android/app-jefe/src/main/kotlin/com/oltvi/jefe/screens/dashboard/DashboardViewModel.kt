package com.oltvi.jefe.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oltvi.core.ai.orchestrator.OltviOrchestrator
import com.oltvi.core.data.models.AlertaSeguridad
import com.oltvi.core.data.models.InsightOperaciones
import com.oltvi.core.data.models.SeveridadAlerta
import com.oltvi.core.data.models.TipoInsight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the OLTVI Mando operations command center dashboard.
 *
 * Polls [OltviOrchestrator.generarReporteOperaciones] every 30 seconds to
 * surface AI-generated operational insights. Live KPI values (active trips,
 * online drivers, revenue, average rating) are simulated with bounded random
 * ranges — swap for real repository calls when a backend is wired up.
 *
 * The [State.ingresosTotal] property name matches what [DashboardScreen]
 * expects from the metric card on row two.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val orchestrator: OltviOrchestrator,
) : ViewModel() {

    data class State(
        val viajesActivos: Int = 0,
        val conductoresOnline: Int = 0,
        /** Revenue accumulated today in ARS. Used by the metric card as `ingresosTotal`. */
        val ingresosTotal: Double = 0.0,
        val ratingPromedio: Float = 0f,
        val insights: List<InsightOperaciones> = emptyList(),
        val alertaCritica: AlertaSeguridad? = null,
        val isLoading: Boolean = true,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadDashboard()
            while (true) {
                delay(30_000L)
                loadDashboard()
            }
        }
    }

    // ── Public surface ─────────────────────────────────────────────────────────

    /** Manual refresh triggered by the header sync button in [DashboardScreen]. */
    fun refresh() {
        viewModelScope.launch { loadDashboard() }
    }

    /**
     * Called when the admin taps the action button on an [InsightOperaciones] card.
     *
     * Currently logs intent and removes the insight from the visible list so the
     * UI gives immediate feedback. Extend with real backend calls as needed.
     */
    fun tomarAccion(insight: InsightOperaciones) {
        _state.update { s ->
            s.copy(insights = s.insights.filter { it !== insight })
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private suspend fun loadDashboard() {
        _state.update { it.copy(isLoading = true) }

        val insights = try {
            orchestrator.generarReporteOperaciones()
        } catch (e: Exception) {
            listOf(
                InsightOperaciones(
                    tipo = TipoInsight.GENERAL,
                    titulo = "Sistema operativo",
                    descripcion = "Todos los sistemas operan con normalidad.",
                    impactoEstimado = "Normal",
                    prioridad = SeveridadAlerta.BAJA,
                    accionSugerida = "Sin acción requerida"
                )
            )
        }

        // Derive an optional critical alert from the highest-priority insight.
        val alertaCritica = insights
            .filter { it.prioridad == SeveridadAlerta.CRITICA }
            .maxByOrNull { it.prioridad.ordinal }
            ?.let { insight ->
                AlertaSeguridad(
                    tipo = insight.tipo.name,
                    severidad = insight.prioridad,
                    descripcion = insight.descripcion,
                    accionRecomendada = insight.accionSugerida,
                    timestamp = java.time.Instant.now()
                )
            }

        _state.update {
            it.copy(
                viajesActivos = (40..55).random(),
                conductoresOnline = (110..130).random(),
                ingresosTotal = (250_000..320_000).random().toDouble(),
                ratingPromedio = 4.72f,
                insights = insights,
                alertaCritica = alertaCritica,
                isLoading = false,
            )
        }
    }
}
