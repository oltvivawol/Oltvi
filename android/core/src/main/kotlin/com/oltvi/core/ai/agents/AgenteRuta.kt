package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.RutaOptimizada
import com.oltvi.core.util.Haversine
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

@Singleton
class AgenteRuta @Inject constructor(
    @Named("geminiKey") geminiKey: String
) : BaseAgent(geminiKey, mockMode = geminiKey == "mock") {

    override val agentName: String = "AgenteRuta"

    override val systemPrompt: String = """
        Sos el agente de Optimización de Rutas de OLTVI. Calculás la mejor ruta para
        un viaje considerando tráfico en tiempo real, eventos viales reportados,
        clima y horario pico. Devolvés ruta principal + 2 alternativas con sus ETAs
        en minutos y distancia en km. Hablás en español neutral, breve, sin emojis.
    """.trimIndent()

    override val agentTools: List<Tool> = listOf(
        Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "calcular_ruta",
                    description = "Calcula la ruta óptima entre dos puntos.",
                    parameters = listOf(
                        Schema.int("etaMin", "Tiempo estimado en minutos"),
                        Schema.double("distanciaKm", "Distancia total en km"),
                        Schema.str("trafico", "Estado del tráfico: fluido, moderado, congestionado")
                    ),
                    requiredParameters = listOf("etaMin", "distanciaKm", "trafico")
                )
            )
        )
    )

    suspend fun optimizarRuta(origen: PuntoGeo, destino: PuntoGeo): RutaOptimizada {
        if (!mockMode) {
            runCatching {
                chat("Calcular ruta de ${origen.nombre} a ${destino.nombre}")
            }
        }
        return buildMockRoute(origen, destino, isAlternative = false)
    }

    private fun buildMockRoute(
        origen: PuntoGeo,
        destino: PuntoGeo,
        isAlternative: Boolean,
        offsetSeed: Int = 0
    ): RutaOptimizada {
        val distanciaKm = Haversine.distanceKm(origen.lat, origen.lng, destino.lat, destino.lng)
            .coerceAtLeast(0.4)
        val steps = 8
        val jitter = (offsetSeed.coerceAtLeast(0)) * 0.0008
        val puntos = (0..steps).map { i ->
            val t = i.toDouble() / steps
            val lat = origen.lat + (destino.lat - origen.lat) * t + (if (isAlternative) jitter else 0.0)
            val lng = origen.lng + (destino.lng - origen.lng) * t + (if (isAlternative) -jitter else 0.0)
            PuntoGeo(lat, lng)
        }

        val etaMin = (distanciaKm * 3.0 + if (isAlternative) 2 + offsetSeed else 0)
            .roundToInt()
            .coerceAtLeast(1)

        val trafico = when {
            distanciaKm > 12 -> "congestionado"
            distanciaKm > 5 -> "moderado"
            else -> "fluido"
        }

        val alternativas = if (!isAlternative) {
            listOf(
                buildMockRoute(origen, destino, isAlternative = true, offsetSeed = 1),
                buildMockRoute(origen, destino, isAlternative = true, offsetSeed = 2)
            )
        } else emptyList()

        return RutaOptimizada(
            puntos = puntos,
            etaMin = etaMin,
            distanciaKm = (distanciaKm * 1000.0).roundToInt() / 1000.0,
            trafico = trafico,
            alternativas = alternativas
        )
    }

    override suspend fun executeMock(input: Map<String, Any>): String {
        return """
            {
              "etaMin": 18,
              "distanciaKm": 6.4,
              "trafico": "moderado"
            }
        """.trimIndent().also { abs(it.hashCode()) /* keep stable */ }
    }
}
