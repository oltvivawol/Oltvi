package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.oltvi.core.data.models.AlertaSeguridad
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.SeveridadAlerta
import com.oltvi.core.util.Haversine
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AgenteSeguridad @Inject constructor(
    @Named("geminiKey") geminiKey: String
) : BaseAgent(geminiKey, mockMode = geminiKey == "mock") {

    override val agentName: String = "AgenteSeguridad"

    override val systemPrompt: String = """
        Sos el agente de Seguridad de OLTVI. Monitoreás cada viaje en tiempo real y
        emitís alertas cuando detectás: desvíos significativos de la ruta, frenadas
        bruscas, paradas inesperadas en zonas riesgosas, sobrevelocidad o pérdida de
        señal prolongada. Tu output debe ser sobrio, en español neutral, accionable
        y nunca alarmista. Indicás severidad y la acción recomendada.
    """.trimIndent()

    override val agentTools: List<Tool> = listOf(
        Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "emitir_alerta",
                    description = "Emite una alerta de seguridad para un viaje activo.",
                    parameters = listOf(
                        Schema.str("tipo", "Tipo de alerta (desvio, parada, velocidad, senal, zona)"),
                        Schema.str("severidad", "BAJA | MEDIA | ALTA | CRITICA"),
                        Schema.str("descripcion", "Descripción de la anomalía"),
                        Schema.str("accionRecomendada", "Acción concreta a tomar")
                    ),
                    requiredParameters = listOf("tipo", "severidad", "descripcion", "accionRecomendada")
                )
            )
        )
    )

    suspend fun monitorearViaje(
        contexto: ContextoViaje,
        posicionActual: PuntoGeo
    ): AlertaSeguridad? {
        if (!mockMode) {
            runCatching {
                chat("Evaluá riesgo del viaje ${contexto.idViaje} en posición ${posicionActual.lat},${posicionActual.lng}")
            }
        }

        // Deterministic mock — 80% return null, 20% produce a synthetic alert.
        val seed = abs(("${contexto.idViaje}-${posicionActual.lat}-${posicionActual.lng}").hashCode())
        if (seed % 5 != 0) return null

        val origen = contexto.origen
        val desvioKm = if (origen != null) {
            Haversine.distanceKm(origen.lat, origen.lng, posicionActual.lat, posicionActual.lng)
        } else 0.0

        val (tipo, severidad, descripcion, accion) = when (seed % 4) {
            0 -> Quad(
                "desvio_ruta",
                if (desvioKm > 3.0) SeveridadAlerta.ALTA else SeveridadAlerta.MEDIA,
                "El conductor se desvió ${"%.1f".format(desvioKm)} km de la ruta planificada.",
                "Contactá al conductor por chat para confirmar el motivo del desvío."
            )
            1 -> Quad(
                "parada_inesperada",
                SeveridadAlerta.MEDIA,
                "Se detectó una parada de más de 4 minutos fuera de los puntos de la ruta.",
                "Verificá con el conductor el motivo de la detención."
            )
            2 -> Quad(
                "sobrevelocidad",
                SeveridadAlerta.ALTA,
                "El vehículo superó los 90 km/h en zona urbana en los últimos 60 segundos.",
                "Sugerí al conductor moderar la velocidad por seguridad."
            )
            else -> Quad(
                "perdida_senal",
                SeveridadAlerta.BAJA,
                "Sin señal GPS durante 2 minutos. Última posición registrada estable.",
                "Esperá 60 segundos antes de escalar. Probablemente sea zona sin cobertura."
            )
        }

        return AlertaSeguridad(
            tipo = tipo,
            severidad = severidad,
            descripcion = descripcion,
            accionRecomendada = accion,
            timestamp = Instant.now()
        )
    }

    private data class Quad(
        val a: String,
        val b: SeveridadAlerta,
        val c: String,
        val d: String
    )

    override suspend fun executeMock(input: Map<String, Any>): String {
        return """
            {
              "tipo": "desvio_ruta",
              "severidad": "MEDIA",
              "descripcion": "Pequeño desvío detectado, probablemente por tráfico.",
              "accionRecomendada": "Monitorear durante los próximos 3 minutos."
            }
        """.trimIndent()
    }
}
