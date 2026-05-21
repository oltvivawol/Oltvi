package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.oltvi.core.data.models.ConductorDisponible
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.ResultadoMatchmaking
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class AgenteMatchmaker @Inject constructor(
    @Named("geminiKey") geminiKey: String
) : BaseAgent(geminiKey, mockMode = geminiKey == "mock") {

    override val agentName: String = "AgenteMatchmaker"

    override val systemPrompt: String = """
        Sos el agente Matchmaker de OLTVI. Tu objetivo es elegir el mejor conductor
        para un viaje, optimizando una mezcla de rating, distancia, tiempo estimado,
        tasa de aceptación histórica y compatibilidad de vehículo con el tipo de
        servicio (pasajero, mensajería, carga, vial). Respondés siempre en español
        rioplatense neutral, sin emojis, y explicás brevemente la elección.
    """.trimIndent()

    override val agentTools: List<Tool> = listOf(
        Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "elegir_conductor",
                    description = "Devuelve el id del conductor elegido y una explicación corta.",
                    parameters = listOf(
                        Schema.str("idConductor", "Identificador del conductor elegido"),
                        Schema.str("explicacion", "Por qué se eligió a ese conductor"),
                        Schema.double("confianza", "Nivel de confianza entre 0 y 1")
                    ),
                    requiredParameters = listOf("idConductor", "explicacion", "confianza")
                )
            )
        )
    )

    suspend fun encontrarMejorConductor(
        contexto: ContextoViaje,
        conductoresDisponibles: List<ConductorDisponible>
    ): ResultadoMatchmaking {
        require(conductoresDisponibles.isNotEmpty()) {
            "No hay conductores disponibles para asignar"
        }

        if (!mockMode) {
            runCatching {
                chat(
                    "Tenés ${conductoresDisponibles.size} conductores en zona. " +
                        "Origen ${contexto.origen?.nombre ?: "—"}. " +
                        "Devolvé el id elegido en JSON."
                )
            }
        }

        val ranked = conductoresDisponibles.sortedByDescending { score(it) }
        val mejor = ranked.first()
        val alternativas = ranked.drop(1).take(3)

        val explicacion = "Elegí a ${mejor.nombre} por su rating de ${"%.1f".format(mejor.rating)} " +
            "y proximidad de ${mejor.distanciaMetros.roundToInt()}m. " +
            "Es la mejor combinación calidad/tiempo."

        return ResultadoMatchmaking(
            conductorElegido = mejor,
            alternativas = alternativas,
            explicacionIA = explicacion,
            confianza = (0.85f + (mejor.rating - 4f) * 0.05f).coerceIn(0.6f, 0.99f)
        )
    }

    private fun score(c: ConductorDisponible): Double {
        val distanceKm = c.distanciaMetros / 1000.0
        return c.rating * 0.5 + (1.0 / (distanceKm + 1.0)) * 0.5
    }

    override suspend fun executeMock(input: Map<String, Any>): String {
        return """
            {
              "idConductor": "mock-driver-1",
              "explicacion": "Conductor con mejor rating y más cercano a tu posición.",
              "confianza": 0.92
            }
        """.trimIndent()
    }
}
