package com.oltvi.usuario.screens.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oltvi.core.ai.orchestrator.OltviOrchestrator
import com.oltvi.core.data.models.AlertaSeguridad
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.EstadoServicio
import com.oltvi.core.data.models.MonitoreoResultado
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.RutaOptimizada
import com.oltvi.core.data.models.TipoServicio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingUiState(
    val driverPosition: PuntoGeo = PuntoGeo(-34.6037 - 0.003, -58.3816 + 0.005, "Conductor"),
    val origen: PuntoGeo = PuntoGeo(-34.6037, -58.3816, "Mi ubicación"),
    val destino: PuntoGeo = PuntoGeo(-34.5797, -58.4325, "Palermo"),
    val ruta: RutaOptimizada? = null,
    val alertaActiva: AlertaSeguridad? = null,
    val enRutaSegura: Boolean = true,
    val etaMin: Int = 7,
    val estado: EstadoServicio = EstadoServicio.EN_CAMINO,
    val sosActivado: Boolean = false
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val orchestrator: OltviOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(TrackingUiState())
    val state: StateFlow<TrackingUiState> = _state.asStateFlow()

    private val userId: String = "user-001"
    private var monitoringJob: kotlinx.coroutines.Job? = null

    fun startMonitoring(servicioId: String) {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            while (true) {
                val current = _state.value
                try {
                    val contexto = ContextoViaje(
                        idViaje = servicioId,
                        idUsuario = userId,
                        origen = current.origen,
                        destino = current.destino,
                        estadoActual = current.estado,
                        tipoServicio = TipoServicio.PASAJERO
                    )
                    val resultado: MonitoreoResultado = orchestrator.monitorearViajeActivo(
                        contexto = contexto,
                        posicion = current.driverPosition
                    )
                    _state.update {
                        it.copy(
                            alertaActiva = resultado.alerta,
                            enRutaSegura = resultado.enRutaSegura,
                            ruta = resultado.rutaActualizada ?: it.ruta,
                            etaMin = resultado.rutaActualizada?.etaMin ?: it.etaMin
                        )
                    }
                } catch (_: Exception) {
                    // Silently retry — monitoring is best-effort polling.
                }
                delay(5_000L)
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    fun updateDriverPosition(nuevaPosicion: PuntoGeo) {
        _state.update { it.copy(driverPosition = nuevaPosicion) }
    }

    fun advanceEstado(nuevo: EstadoServicio) {
        _state.update { it.copy(estado = nuevo) }
    }

    fun activarSos() {
        _state.update { it.copy(sosActivado = true) }
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
