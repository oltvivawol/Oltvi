package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.oltvi.core.data.models.AlertaSeguridad
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.SeveridadAlerta
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.random.Random

@Singleton
class AgenteSeguridad @Inject constructor(
    geminiKey: String,
    mockMode: Boolean = false
) : BaseAgent(geminiKey, mockMode) {

    override val agentName = "AgenteSeguridad"

    override val systemPrompt = """
        Eres el Agente de Seguridad de OLTVI. Monitoreás viajes en tiempo real.
        Detectás: desvíos de ruta >300m, paradas inusuales >3 min, velocidad excesiva >120 km/h en zona urbana,
        cambios de destino no autorizados.
        Cuando detectás anomalías, generás alertas claras con severidad y acción recomendada.
        Responde en español.
    """.trimIndent()

    override val agentTools = listOf(
        Tool(
            functionDeclarations = listOf(
                defineFunction(
                    name = "analizar_desvio_ruta",
                    description = "Analiza si el vehículo se ha desviado de la ruta planificada",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_viaje" to Schema.str("ID del viaje activo"),
                            "lat_actual" to Schema.num("Latitud actual del vehículo"),
                            "lng_actual" to Schema.num("Longitud actual del vehículo"),
                            "umbral_metros" to Schema.num("Umbral de desvío en metros")
                        )
                    )
                ),
                defineFunction(
                    name = "detectar_parada_anomala",
                    description = "Detecta paradas prolongadas fuera del destino",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_viaje" to Schema.str("ID del viaje"),
                            "duracion_segundos" to Schema.num("Duración de la parada en segundos"),
                            "lat" to Schema.num("Latitud de la parada"),
                            "lng" to Schema.num("Longitud de la parada")
                        )
                    )
                ),
                defineFunction(
                    name = "calcular_velocidad_actual",
                    description = "Calcula la velocidad actual basada en posiciones consecutivas",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "lat1" to Schema.num("Latitud posición anterior"),
                            "lng1" to Schema.num("Longitud posición anterior"),
                            "lat2" to Schema.num("Latitud posición actual"),
                            "lng2" to Schema.num("Longitud posición actual"),
                            "delta_segundos" to Schema.num("Tiempo transcurrido en segundos")
                        )
                    )
                ),
                defineFunction(
                    name = "enviar_alerta_emergencia",
                    description = "Envía una alerta de emergencia al centro de control",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_viaje" to Schema.str("ID del viaje"),
                            "tipo_alerta" to Schema.str("Tipo de alerta"),
                            "descripcion" to Schema.str("Descripción detallada"),
                            "lat" to Schema.num("Latitud de la emergencia"),
                            "lng" to Schema.num("Longitud de la emergencia")
                        )
                    )
                )
            )
        )
    )

    suspend fun monitorearViaje(
        contexto: ContextoViaje,
        posicionActual: PuntoGeo
    ): AlertaSeguridad? {
        if (mockMode) return mockMonitoreo(contexto, posicionActual)

        val prompt = buildString {
            appendLine("Monitorea el estado de seguridad del viaje:")
            appendLine("ID viaje: ${contexto.idViaje}")
            appendLine("Estado: ${contexto.estadoActual?.displayName}")
            appendLine("Posición actual: (${posicionActual.latitud}, ${posicionActual.longitud})")
            contexto.destino?.let {
                appendLine("Destino: (${it.latitud}, ${it.longitud})")
            }
            appendLine("Puntos de historial: ${contexto.historialPosiciones.size}")
            appendLine("¿Detectas alguna anomalía? Si no hay riesgo, responde 'SEGURO'.")
        }

        val respuesta = chat(prompt)
        return if (respuesta.contains("SEGURO", ignoreCase = true)) {
            null
        } else {
            AlertaSeguridad(
                tipo = "ANOMALIA_DETECTADA",
                severidad = SeveridadAlerta.MEDIUM,
                descripcion = respuesta,
                accionRecomendada = "Verificar estado del viaje con el pasajero"
            )
        }
    }

    private fun mockMonitoreo(contexto: ContextoViaje, posicion: PuntoGeo): AlertaSeguridad? {
        // 80% del tiempo el viaje está en curso normal
        val random = Random(System.currentTimeMillis())
        if (random.nextFloat() < 0.80f) return null

        // 20% generamos una alerta LOW
        val destino = contexto.destino
        val desvioDetectado = if (destino != null) {
            val latDiff = abs(posicion.latitud - destino.latitud) * 111000
            val lngDiff = abs(posicion.longitud - destino.longitud) * 111000
            latDiff > 500 || lngDiff > 500
        } else false

        return if (desvioDetectado) {
            AlertaSeguridad(
                tipo = "DESVIO_RUTA",
                severidad = SeveridadAlerta.LOW,
                descripcion = "El vehículo se ha desviado ligeramente de la ruta planificada.",
                accionRecomendada = "Confirmar con el conductor que la ruta es correcta."
            )
        } else {
            AlertaSeguridad(
                tipo = "PARADA_INUSUAL",
                severidad = SeveridadAlerta.LOW,
                descripcion = "Se detectó una parada breve no programada en la ruta.",
                accionRecomendada = "Monitorear por 2 minutos adicionales antes de escalar."
            )
        }
    }

    override suspend fun executeMock(input: Map<String, Any>) =
        "SEGURO — viaje transcurre sin anomalías detectadas."
}
