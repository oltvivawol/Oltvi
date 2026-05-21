package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.oltvi.core.data.models.NivelServicio
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.ResultadoPrecio
import com.oltvi.core.data.models.TipoServicio
import com.oltvi.core.util.Haversine
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

@Singleton
class AgentePrecio @Inject constructor(
    @Named("geminiKey") geminiKey: String
) : BaseAgent(geminiKey, mockMode = geminiKey == "mock") {

    override val agentName: String = "AgentePrecio"

    override val systemPrompt: String = """
        Sos el agente de Pricing dinámico de OLTVI para el mercado argentino. Calculás
        el precio de un viaje en pesos argentinos combinando: tarifa base, distancia,
        nivel de servicio (estándar/prioritario/exprés/especial), factor de demanda en
        tiempo real, clima y eventos viales activos. Explicás el desglose en lenguaje
        claro y amigable, sin tecnicismos. No usás emojis.
    """.trimIndent()

    override val agentTools: List<Tool> = listOf(
        Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "calcular_precio",
                    description = "Devuelve el precio final y el desglose por componente.",
                    parameters = listOf(
                        Schema.double("precioFinal", "Precio total en ARS"),
                        Schema.double("factorDemanda", "Factor multiplicador 1.0..2.0"),
                        Schema.str("explicacion", "Explicación amigable del precio")
                    ),
                    requiredParameters = listOf("precioFinal", "factorDemanda", "explicacion")
                )
            )
        )
    )

    suspend fun calcularPrecio(
        origen: PuntoGeo,
        destino: PuntoGeo,
        tipo: TipoServicio,
        nivel: NivelServicio,
        idUsuario: String
    ): ResultadoPrecio {
        if (!mockMode) {
            runCatching {
                chat("Calcular precio para viaje de ${origen.nombre} a ${destino.nombre} tipo $tipo nivel $nivel")
            }
        }

        val distanciaKm = Haversine.distanceKm(origen.lat, origen.lng, destino.lat, destino.lng)
            .coerceAtLeast(0.5)

        val tarifaBase = 800.0
        val precioPorKm = 350.0
        val distanciaCost = distanciaKm * precioPorKm

        // Demand factor 1.0..1.5 derived deterministically from user id and time slot.
        val seed = abs((idUsuario.hashCode() xor (System.currentTimeMillis() / 3_600_000L).toInt()))
        val factorDemanda = 1.0 + (seed % 51) / 100.0
        val factorClima = listOf("despejado", "nublado", "lluvia leve")[seed % 3]
        val factorTipo = when (tipo) {
            TipoServicio.PASAJERO -> 1.0
            TipoServicio.MENSAJERIA -> 0.85
            TipoServicio.CARGA -> 1.6
            TipoServicio.VIAL -> 0.0
        }

        val subTotal = (tarifaBase + distanciaCost) * factorTipo
        val nivelExtra = subTotal * (nivel.priceMultiplier - 1.0)
        val demandaExtra = (subTotal + nivelExtra) * (factorDemanda - 1.0)
        val precioFinal = (subTotal + nivelExtra + demandaExtra)

        val desglose = linkedMapOf(
            "Tarifa base" to tarifaBase * factorTipo,
            "Distancia" to distanciaCost * factorTipo,
            "Multiplicador ${nivel.displayName}" to nivelExtra,
            "Factor demanda" to demandaExtra
        )

        val explicacion = "Tu viaje cuesta \$${precioFinal.roundToInt()} porque cubrís " +
            "${"%.1f".format(distanciaKm)} km, elegiste nivel ${nivel.displayName.lowercase()} " +
            "(x${nivel.priceMultiplier}) y la demanda actual está en x${"%.2f".format(factorDemanda)} " +
            "con clima $factorClima."

        return ResultadoPrecio(
            precioFinal = precioFinal,
            desglose = desglose,
            explicacionIA = explicacion,
            factorDemanda = factorDemanda,
            factorClima = factorClima
        )
    }

    override suspend fun executeMock(input: Map<String, Any>): String {
        return """
            {
              "precioFinal": 4200.0,
              "factorDemanda": 1.25,
              "explicacion": "Tu viaje cuesta ${'$'}4200 porque hay alta demanda en esta zona."
            }
        """.trimIndent()
    }
}
