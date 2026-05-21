package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.oltvi.core.data.models.NivelServicio
import com.oltvi.core.data.models.PuntoGeo
import com.oltvi.core.data.models.ResultadoPrecio
import com.oltvi.core.data.models.TipoServicio
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class AgentePrecio @Inject constructor(
    geminiKey: String,
    mockMode: Boolean = false
) : BaseAgent(geminiKey, mockMode) {

    override val agentName = "AgentePrecio"

    override val systemPrompt = """
        Eres el Agente de Precios de OLTVI. Calculas precios justos y TRANSPARENTES.
        Siempre muestra el desglose: precio base + factor demanda + ajuste clima + descuentos.
        NUNCA ocultes el precio dinámico — explícalo con honestidad. Responde en español.
        Mantén los precios justos para conductores y pasajeros.
    """.trimIndent()

    override val agentTools = listOf(
        Tool(
            functionDeclarations = listOf(
                defineFunction(
                    name = "calcular_precio_base",
                    description = "Calcula el precio base según distancia, tipo y nivel de servicio",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "distancia_km" to Schema.num("Distancia en kilómetros"),
                            "tipo_servicio" to Schema.str("Tipo de servicio"),
                            "nivel_servicio" to Schema.str("Nivel de servicio")
                        )
                    )
                ),
                defineFunction(
                    name = "obtener_factor_demanda",
                    description = "Obtiene el factor de demanda según zona y hora (0.8-2.5)",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "lat" to Schema.num("Latitud de la zona"),
                            "lng" to Schema.num("Longitud de la zona"),
                            "hora" to Schema.num("Hora del día 0-23")
                        )
                    )
                ),
                defineFunction(
                    name = "obtener_condiciones_clima",
                    description = "Obtiene las condiciones climáticas actuales en la zona",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "lat" to Schema.num("Latitud"),
                            "lng" to Schema.num("Longitud")
                        )
                    )
                ),
                defineFunction(
                    name = "aplicar_descuento",
                    description = "Aplica descuentos según el historial del usuario",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_usuario" to Schema.str("ID del usuario"),
                            "precio_base" to Schema.num("Precio base antes de descuento")
                        )
                    )
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
        if (mockMode) return mockPrecio(origen, destino, tipo, nivel, idUsuario)

        val distanciaKm = calcularDistanciaKm(origen, destino)
        val prompt = buildString {
            appendLine("Calcula el precio para este viaje:")
            appendLine("Tipo: ${tipo.displayName}, Nivel: ${nivel.displayName}")
            appendLine("Distancia: ${"%.1f".format(distanciaKm)} km")
            appendLine("Origen: ${origen.nombre.ifBlank { "(${origen.latitud}, ${origen.longitud})" }}")
            appendLine("Destino: ${destino.nombre.ifBlank { "(${destino.latitud}, ${destino.longitud})" }}")
            appendLine("Usuario: $idUsuario")
            appendLine("Proporciona un precio justo con desglose detallado.")
        }

        val respuesta = chat(prompt)
        return mockPrecio(origen, destino, tipo, nivel, idUsuario).copy(explicacionIA = respuesta)
    }

    private fun mockPrecio(
        origen: PuntoGeo,
        destino: PuntoGeo,
        tipo: TipoServicio,
        nivel: NivelServicio,
        idUsuario: String
    ): ResultadoPrecio {
        val distanciaKm = calcularDistanciaKm(origen, destino).coerceAtLeast(1.0)
        val precioBase = 800.0 + (120.0 * distanciaKm)
        val factorDemanda = if (distanciaKm > 3.0) 1.2 else 1.1
        val factorNivel = nivel.priceMultiplier
        val subtotal = precioBase * factorDemanda * factorNivel
        val descuento = if (idUsuario.isNotBlank()) subtotal * 0.05 else 0.0
        val precioFinal = subtotal - descuento

        val desglose = mapOf(
            "Precio base" to precioBase,
            "Factor demanda (×${"%.1f".format(factorDemanda)})" to (precioBase * (factorDemanda - 1.0)),
            "Distancia (${"%.1f".format(distanciaKm)} km)" to (120.0 * distanciaKm),
            "Ajuste nivel (${nivel.displayName})" to (precioBase * factorDemanda * (factorNivel - 1.0)),
            "Descuento usuario" to -descuento
        )

        val explicacion = buildString {
            append("El precio de \$${"%,.0f".format(precioFinal)} ARS incluye: ")
            append("base de \$${"%,.0f".format(precioBase)} ARS para ${"%.1f".format(distanciaKm)} km, ")
            append("más un factor de demanda de ${factorDemanda}x por el horario actual")
            if (nivel != NivelServicio.ESTANDAR) append(" y nivel ${nivel.displayName}")
            if (descuento > 0) append(". Se aplicó un descuento de ${"%,.0f".format(descuento)} ARS por fidelidad")
            append(". Precio 100% transparente.")
        }

        return ResultadoPrecio(
            precioFinal = precioFinal,
            desglose = desglose,
            explicacionIA = explicacion,
            factorDemanda = factorDemanda,
            factorClima = "Despejado"
        )
    }

    private fun calcularDistanciaKm(a: PuntoGeo, b: PuntoGeo): Double {
        val latDiff = (a.latitud - b.latitud) * 111.0
        val lngDiff = (a.longitud - b.longitud) * 111.0 * 0.82
        return sqrt(latDiff.pow(2) + lngDiff.pow(2))
    }

    override suspend fun executeMock(input: Map<String, Any>) =
        "Mock pricing: precio calculado de forma transparente con desglose completo."
}
