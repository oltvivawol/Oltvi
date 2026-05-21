package com.oltvi.core.ai.orchestrator

import com.oltvi.core.ai.agents.AgenteCopiloto
import com.oltvi.core.ai.agents.AgenteFraude
import com.oltvi.core.ai.agents.AgenteMatchmaker
import com.oltvi.core.ai.agents.AgenteOperaciones
import com.oltvi.core.ai.agents.AgentePrecio
import com.oltvi.core.ai.agents.AgenteRuta
import com.oltvi.core.ai.agents.AgenteSeguridad
import com.oltvi.core.ai.agents.AgenteSoporte
import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.FraudeResultado
import com.oltvi.core.data.models.InsightOperaciones
import com.oltvi.core.data.models.MensajeChat
import com.oltvi.core.data.models.MonitoreoResultado
import com.oltvi.core.data.models.NivelServicio
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.SeveridadAlerta
import com.oltvi.core.data.models.SolicitudViajeResultado
import com.oltvi.core.data.models.SugerenciaCopiloto
import com.oltvi.core.data.models.TipoInsight
import com.oltvi.core.data.models.TipoServicio
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OltviOrchestrator @Inject constructor(
    private val matchmaker: AgenteMatchmaker,
    private val precio: AgentePrecio,
    private val seguridad: AgenteSeguridad,
    private val ruta: AgenteRuta,
    private val soporte: AgenteSoporte,
    private val fraude: AgenteFraude,
    private val copiloto: AgenteCopiloto,
    private val operaciones: AgenteOperaciones,
) {
    suspend fun procesarSolicitudViaje(
        contexto: ContextoViaje,
        conductoresDisponibles: List<ConductorDisponible>
    ): SolicitudViajeResultado = coroutineScope {
        val matchAsync = async { matchmaker.encontrarMejorConductor(contexto, conductoresDisponibles) }
        val precioAsync = async {
            precio.calcularPrecio(
                contexto.origen ?: PuntoGeo(-34.6037, -58.3816),
                contexto.destino ?: PuntoGeo(-34.5875, -58.3974),
                contexto.tipoServicio ?: TipoServicio.PASAJERO,
                NivelServicio.ESTANDAR,
                contexto.idUsuario ?: ""
            )
        }
        val rutaAsync = async {
            ruta.optimizarRuta(
                contexto.origen ?: PuntoGeo(-34.6037, -58.3816),
                contexto.destino ?: PuntoGeo(-34.5875, -58.3974)
            )
        }
        SolicitudViajeResultado(
            matchmaking = matchAsync.await(),
            precio = precioAsync.await(),
            ruta = rutaAsync.await()
        )
    }

    suspend fun monitorearViajeActivo(
        contexto: ContextoViaje,
        posicion: PuntoGeo
    ): MonitoreoResultado = coroutineScope {
        val alertaAsync = async { seguridad.monitorearViaje(contexto, posicion) }
        val rutaAsync = async {
            contexto.destino?.let { ruta.optimizarRuta(posicion, it) }
        }
        val alerta = alertaAsync.await()
        MonitoreoResultado(
            alerta = alerta,
            rutaActualizada = rutaAsync.await(),
            enRutaSegura = alerta?.severidad?.let {
                it != SeveridadAlerta.HIGH && it != SeveridadAlerta.CRITICAL
            } ?: true
        )
    }

    suspend fun atenderSoporte(
        idUsuario: String,
        mensaje: String,
        contexto: ContextoViaje? = null
    ): MensajeChat = soporte.resolverConsulta(idUsuario, mensaje, contexto)

    suspend fun iniciarTurnoConductor(
        idConductor: String,
        posicion: PuntoGeo,
        gananciasHoy: Double = 0.0
    ): SugerenciaCopiloto = copiloto.obtenerSugerencia(idConductor, posicion, gananciasHoy)

    suspend fun generarReporteOperaciones(): List<InsightOperaciones> = coroutineScope {
        TipoInsight.entries.map { tipo ->
            async { operaciones.generarInsight(tipo) }
        }.map { it.await() }
    }

    suspend fun verificarPago(
        idViaje: String,
        idUsuario: String,
        monto: Double
    ): FraudeResultado = fraude.analizarTransaccion(idViaje, idUsuario, monto)
}
