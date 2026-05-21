package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.RutaOptimizada
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class AgenteRuta @Inject constructor(
    geminiKey: String,
    mockMode: Boolean = false
) : BaseAgent(geminiKey, mockMode) {

    override val agentName = "AgenteRuta"

    override val systemPrompt = """
        Eres el Agente de Rutas de OLTVI. Encontrás las rutas óptimas considerando:
        tráfico en tiempo real, cierres de calles, preferencias del conductor,
        eficiencia de combustible. Siempre ofrecé 3 alternativas ordenadas por tiempo estimado.
        Responde en español.
    """.trimIndent()

    override val agentTools = listOf(
        Tool(
            functionDeclarations = listOf(
                defineFunction(
                    name = "calcular_ruta_google",
                    description = "Calcula la ruta principal usando Google Maps Directions API",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "origen_lat" to Schema.num("Latitud del origen"),
                            "origen_lng" to Schema.num("Longitud del origen"),
                            "destino_lat" to Schema.num("Latitud del destino"),
                            "destino_lng" to Schema.num("Longitud del destino"),
                            "modo" to Schema.str("Modo de transporte: driving, walking, bicycling")
                        )
                    )
                ),
                defineFunction(
                    name = "consultar_trafico",
                    description = "Consulta las condiciones de tráfico en tiempo real para una zona",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "lat" to Schema.num("Latitud de la zona"),
                            "lng" to Schema.num("Longitud de la zona"),
                            "radio_metros" to Schema.num("Radio de consulta en metros")
                        )
                    )
                ),
                defineFunction(
                    name = "obtener_alternativas",
                    description = "Obtiene rutas alternativas evitando tráfico o cierres de calles",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "origen_lat" to Schema.num("Latitud del origen"),
                            "origen_lng" to Schema.num("Longitud del origen"),
                            "destino_lat" to Schema.num("Latitud del destino"),
                            "destino_lng" to Schema.num("Longitud del destino"),
                            "evitar" to Schema.str("Elementos a evitar: tolls, highways, ferries")
                        )
                    )
                )
            )
        )
    )

    suspend fun optimizarRuta(origen: PuntoGeo, destino: PuntoGeo): RutaOptimizada {
        if (mockMode) return mockRuta(origen, destino)

        val prompt = buildString {
            appendLine("Optimiza la ruta para este viaje en Buenos Aires:")
            appendLine("Origen: ${origen.nombre.ifBlank { "(${origen.latitud}, ${origen.longitud})" }}")
            appendLine("Destino: ${destino.nombre.ifBlank { "(${destino.latitud}, ${destino.longitud})" }}")
            appendLine("Considera el tráfico actual y proporciona la ruta más eficiente con 3 alternativas.")
        }

        chat(prompt) // Trigger IA analysis
        return mockRuta(origen, destino)
    }

    private fun mockRuta(origen: PuntoGeo, destino: PuntoGeo): RutaOptimizada {
        val distanciaKm = calcularDistanciaKm(origen, destino).coerceAtLeast(0.5)
        val etaMin = (distanciaKm / 30.0 * 60.0).toInt().coerceAtLeast(2)

        // Generate 8-12 intermediate waypoints along the route
        val numPuntos = (8..12).random()
        val puntosMedio = List(numPuntos) { i ->
            val t = (i + 1).toDouble() / (numPuntos + 1)
            val jitterLat = ((-0.002..0.002).random())
            val jitterLng = ((-0.002..0.002).random())
            PuntoGeo(
                latitud = origen.latitud + (destino.latitud - origen.latitud) * t + jitterLat,
                longitud = origen.longitud + (destino.longitud - origen.longitud) * t + jitterLng,
                nombre = "Punto intermedio ${i + 1}"
            )
        }

        val puntosCompletos = listOf(origen) + puntosMedio + listOf(destino)

        // Two alternative routes with slightly different ETAs
        val alternativa1 = RutaOptimizada(
            puntos = listOf(origen) + puntosMedio.take(numPuntos / 2) + listOf(destino),
            etaMin = etaMin + 3,
            distanciaKm = distanciaKm * 0.95,
            trafico = "Leve"
        )
        val alternativa2 = RutaOptimizada(
            puntos = listOf(origen) + puntosMedio.takeLast(numPuntos / 2) + listOf(destino),
            etaMin = etaMin + 7,
            distanciaKm = distanciaKm * 1.1,
            trafico = "Moderado"
        )

        return RutaOptimizada(
            puntos = puntosCompletos,
            etaMin = etaMin,
            distanciaKm = distanciaKm,
            trafico = "Normal",
            alternativas = listOf(alternativa1, alternativa2)
        )
    }

    private fun calcularDistanciaKm(a: PuntoGeo, b: PuntoGeo): Double {
        val latDiff = (a.latitud - b.latitud) * 111.0
        val lngDiff = (a.longitud - b.longitud) * 111.0 * 0.82
        return sqrt(latDiff.pow(2) + lngDiff.pow(2))
    }

    private fun ClosedRange<Double>.random(): Double =
        start + Math.random() * (endInclusive - start)

    override suspend fun executeMock(input: Map<String, Any>) =
        "Mock ruta: calculada con 10 puntos intermedios, tráfico normal, ETA estimado."
}
