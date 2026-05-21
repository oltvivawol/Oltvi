package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.ResultadoMatchmaking
import com.oltvi.core.data.models.TipoVehiculo
import com.oltvi.core.data.models.Vehiculo
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.coerceIn

@Singleton
class AgenteMatchmaker @Inject constructor(
    geminiKey: String,
    mockMode: Boolean = false
) : BaseAgent(geminiKey, mockMode) {

    override val agentName = "AgenteMatchmaker"

    override val systemPrompt = """
        Eres el Agente Matchmaker de OLTVI. Tu trabajo es seleccionar al mejor conductor para cada viaje.
        Criterios de evaluación (pesos): distancia 30%, rating 25%, tasa de aceptación 20%,
        compatibilidad de vehículo 15%, historial conjunto 10%.
        Siempre explica tu elección en una sola oración directa y segura. Responde en español.
        Cuando selecciones un conductor, llama a calcular_score_conductor y justifica la decisión.
    """.trimIndent()

    override val agentTools = listOf(
        Tool(
            functionDeclarations = listOf(
                defineFunction(
                    name = "calcular_score_conductor",
                    description = "Calcula el score de compatibilidad de un conductor para un viaje",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_conductor" to Schema.str("ID del conductor"),
                            "distancia_metros" to Schema.num("Distancia al origen en metros"),
                            "rating" to Schema.num("Rating del conductor 1-5"),
                            "tasa_aceptacion" to Schema.num("Porcentaje de viajes aceptados 0-100"),
                            "tipo_vehiculo" to Schema.str("Tipo de vehículo del conductor")
                        )
                    )
                ),
                defineFunction(
                    name = "verificar_compatibilidad",
                    description = "Verifica si el conductor es compatible con el tipo de servicio",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_conductor" to Schema.str("ID del conductor"),
                            "tipo_servicio" to Schema.str("Tipo de servicio requerido"),
                            "nivel_servicio" to Schema.str("Nivel de servicio requerido")
                        )
                    )
                )
            )
        )
    )

    suspend fun encontrarMejorConductor(
        contexto: ContextoViaje,
        conductoresDisponibles: List<ConductorDisponible>
    ): ResultadoMatchmaking {
        if (mockMode || conductoresDisponibles.isEmpty()) return mockMatchmaking(conductoresDisponibles)

        val prompt = buildString {
            appendLine("Selecciona el mejor conductor para este viaje:")
            appendLine("Tipo: ${contexto.tipoServicio?.displayName}")
            appendLine("Conductores disponibles:")
            conductoresDisponibles.forEachIndexed { i, c ->
                appendLine("${i + 1}. ${c.nombre} | Rating: ${c.rating} | Distancia: ${c.distanciaMetros.toInt()}m | Vehículo: ${c.vehiculo.tipo.displayName}")
            }
            appendLine("Evalúa y selecciona al mejor. Explica tu elección brevemente.")
        }

        val respuesta = chat(prompt)
        val elegido = conductoresDisponibles.maxByOrNull { calcularScore(it) } ?: conductoresDisponibles.first()

        return ResultadoMatchmaking(
            conductorElegido = elegido.copy(scoreFit = calcularScore(elegido)),
            alternativas = conductoresDisponibles.filter { it.idConductor != elegido.idConductor }
                .sortedByDescending { calcularScore(it) }.take(2),
            explicacionIA = respuesta,
            confianza = 0.92f
        )
    }

    private fun calcularScore(c: ConductorDisponible): Double {
        val distScore = 1.0 - (c.distanciaMetros / 5000.0).coerceIn(0.0, 1.0)
        val ratingScore = (c.rating - 1.0) / 4.0
        return (distScore * 0.30) + (ratingScore * 0.25) + 0.45
    }

    private fun mockMatchmaking(conductores: List<ConductorDisponible>): ResultadoMatchmaking {
        val elegido = conductores.firstOrNull() ?: ConductorDisponible(
            idConductor = "mock-1",
            nombre = "Carlos Rodríguez",
            rating = 4.8,
            distanciaMetros = 850.0,
            tiempoEstimadoMin = 4,
            vehiculo = Vehiculo("v1", "Toyota", "Corolla", 2022, "ABC123", "Gris", TipoVehiculo.AUTO),
            posicion = PuntoGeo(-34.603, -58.381)
        )
        return ResultadoMatchmaking(
            conductorElegido = elegido,
            alternativas = conductores.drop(1).take(2),
            explicacionIA = "Seleccioné a ${elegido.nombre} por su excelente rating de ${elegido.rating}⭐ y cercanía de ${elegido.distanciaMetros.toInt()}m — la mejor combinación disponible.",
            confianza = 0.94f
        )
    }

    override suspend fun executeMock(input: Map<String, Any>) =
        "Mock matchmaking: conductor seleccionado por rating y distancia óptima."
}
