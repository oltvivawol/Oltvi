package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.SugerenciaCopiloto
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AgenteCopiloto @Inject constructor(
    geminiKey: String,
    mockMode: Boolean = false
) : BaseAgent(geminiKey, mockMode) {

    override val agentName = "AgenteCopiloto"

    override val systemPrompt = """
        Eres el Copiloto IA de OLTVI para conductores. Tu misión: maximizar las ganancias del conductor
        manteniendo un balance de vida saludable.
        Sugerís: zonas de alta demanda, horarios pico, descansos estratégicos, mantenimiento preventivo.
        Sos como un compañero inteligente que conoce la ciudad y el negocio. Respondé en español.
        Sé directo y accionable — el conductor está manejando, no tiene tiempo para textos largos.
    """.trimIndent()

    override val agentTools = listOf(
        Tool(
            functionDeclarations = listOf(
                defineFunction(
                    name = "obtener_zonas_calientes",
                    description = "Obtiene las zonas de alta demanda en tiempo real en la ciudad",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "lat_centro" to Schema.num("Latitud del centro de búsqueda"),
                            "lng_centro" to Schema.num("Longitud del centro de búsqueda"),
                            "radio_km" to Schema.num("Radio de búsqueda en kilómetros")
                        )
                    )
                ),
                defineFunction(
                    name = "calcular_ganancias_periodo",
                    description = "Calcula las ganancias del conductor en un período",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_conductor" to Schema.str("ID del conductor"),
                            "periodo" to Schema.str("Período: hoy, semana, mes"),
                            "incluir_propinas" to Schema.bool("Si incluir propinas en el cálculo")
                        )
                    )
                ),
                defineFunction(
                    name = "sugerir_proxima_zona",
                    description = "Sugiere la próxima zona a la que debe dirigirse el conductor",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "lat_actual" to Schema.num("Latitud actual del conductor"),
                            "lng_actual" to Schema.num("Longitud actual del conductor"),
                            "ganancia_objetivo" to Schema.num("Meta de ganancia para el turno en ARS")
                        )
                    )
                ),
                defineFunction(
                    name = "verificar_mantenimiento",
                    description = "Verifica el estado de mantenimiento del vehículo del conductor",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_vehiculo" to Schema.str("ID del vehículo"),
                            "km_actuales" to Schema.num("Kilometraje actual del vehículo")
                        )
                    )
                )
            )
        )
    )

    // Buenos Aires hot zones (lat, lng, nombre)
    private val zonasCalientes = listOf(
        Triple(-34.5885, -58.4260, "Palermo"),
        Triple(-34.5852, -58.3854, "Recoleta"),
        Triple(-34.6037, -58.3816, "Microcentro"),
        Triple(-34.6230, -58.3890, "San Telmo"),
        Triple(-34.5534, -58.4597, "Belgrano"),
        Triple(-34.5765, -58.4193, "Villa Crespo"),
        Triple(-34.6170, -58.3680, "Puerto Madero")
    )

    suspend fun obtenerSugerencia(
        idConductor: String,
        posicion: PuntoGeo,
        gananciasHoy: Double
    ): SugerenciaCopiloto {
        if (mockMode) return mockCopiloto(idConductor, posicion, gananciasHoy)

        val prompt = buildString {
            appendLine("Dá una sugerencia de zona al conductor $idConductor.")
            appendLine("Posición actual: (${posicion.latitud}, ${posicion.longitud})")
            appendLine("Ganancias de hoy: \$${"%.0f".format(gananciasHoy)} ARS")
            appendLine("Sugiere la mejor zona para maximizar ganancias ahora. Sé breve y directo.")
        }

        val respuesta = chat(prompt)
        return mockCopiloto(idConductor, posicion, gananciasHoy).copy(descripcion = respuesta)
    }

    private fun mockCopiloto(
        idConductor: String,
        posicion: PuntoGeo,
        gananciasHoy: Double
    ): SugerenciaCopiloto {
        // Find nearest hot zone
        val zonaMasCercana = zonasCalientes.minByOrNull { (lat, lng, _) ->
            val dLat = abs(lat - posicion.latitud)
            val dLng = abs(lng - posicion.longitud)
            dLat + dLng
        } ?: zonasCalientes.first()

        val (lat, lng, nombreZona) = zonaMasCercana
        val metaDiaria = 15_000.0
        val restante = (metaDiaria - gananciasHoy).coerceAtLeast(0.0)
        val potencial = if (restante > 0) restante * 0.4 else 3_000.0

        val urgencia = when {
            gananciasHoy < 5_000.0 -> "alta"
            gananciasHoy < 10_000.0 -> "media"
            else -> "baja"
        }

        val descripcion = buildString {
            when {
                gananciasHoy < 5_000.0 -> {
                    append("Movete a $nombreZona — hay alta demanda ahora. ")
                    append("Llevás \$${"%.0f".format(gananciasHoy)} hoy, podés sumar ~\$${"%.0f".format(potencial)} más en 2 horas.")
                }
                gananciasHoy < 10_000.0 -> {
                    append("$nombreZona está activo. Con tu ritmo actual, superás la meta diaria. ")
                    append("Potencial de \$${"%.0f".format(potencial)} ARS adicionales esta tarde.")
                }
                else -> {
                    append("¡Excelente turno! Ya superaste \$${"%.0f".format(gananciasHoy)} ARS. ")
                    append("$nombreZona tiene demanda residual si querés extender el turno.")
                }
            }
        }

        return SugerenciaCopiloto(
            titulo = "Ir a $nombreZona",
            descripcion = descripcion,
            zonaRecomendada = PuntoGeo(lat, lng, nombreZona, "Buenos Aires"),
            potencialGanancia = potencial,
            urgencia = urgencia
        )
    }

    override suspend fun executeMock(input: Map<String, Any>) =
        "Mock copiloto: Dirigite a Palermo — alta demanda, potencial +\$3.200 ARS en la próxima hora."
}
