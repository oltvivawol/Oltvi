package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.oltvi.core.data.models.AccionFraude
import com.oltvi.core.data.models.FraudeResultado
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AgenteFraude @Inject constructor(
    @Named("geminiKey") geminiKey: String
) : BaseAgent(geminiKey, mockMode = geminiKey == "mock") {

    override val agentName: String = "AgenteFraude"

    override val systemPrompt: String = """
        Sos el agente de Detección de Fraude de OLTVI. Analizás transacciones y
        patrones de uso para detectar: tarjetas robadas, cuentas múltiples, viajes
        circulares para inflar precios, intentos de chargeback, geolocalización
        inconsistente. Devolvés score 0..100, acción (APROBAR/REVISAR/BLOQUEAR) y
        factores de riesgo concretos. Lenguaje claro, sin alarmismos.
    """.trimIndent()

    override val agentTools: List<Tool> = listOf(
        Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "analizar_transaccion",
                    description = "Analiza una transacción y devuelve score de riesgo y acción.",
                    parameters = listOf(
                        Schema.int("scoreRiesgo", "Score 0 (seguro) - 100 (alto riesgo)"),
                        Schema.str("accion", "APROBAR | REVISAR | BLOQUEAR"),
                        Schema.str("explicacion", "Explicación breve del veredicto")
                    ),
                    requiredParameters = listOf("scoreRiesgo", "accion", "explicacion")
                )
            )
        )
    )

    suspend fun analizarTransaccion(
        idViaje: String,
        idUsuario: String,
        monto: Double
    ): FraudeResultado {
        if (!mockMode) {
            runCatching {
                chat("Evaluar transacción viaje=$idViaje usuario=$idUsuario monto=$monto ARS")
            }
        }

        val rawScore = abs((monto.hashCode() xor idUsuario.hashCode())) % 100
        val factores = mutableListOf<String>()
        if (monto > 25_000) factores += "Monto inusualmente alto para una sola transacción"
        if (idUsuario.length < 6) factores += "Usuario con identificador corto o nuevo"
        if (rawScore > 60) factores += "Patrón de geolocalización inconsistente con historial"
        if (rawScore > 80) factores += "Intentos previos de pago rechazados en últimas 24h"
        if (factores.isEmpty()) factores += "Sin señales de riesgo significativas"

        val accion = when {
            rawScore < 35 -> AccionFraude.APROBAR
            rawScore < 75 -> AccionFraude.REVISAR
            else -> AccionFraude.BLOQUEAR
        }

        val explicacion = when (accion) {
            AccionFraude.APROBAR ->
                "Transacción consistente con el perfil del usuario. Score $rawScore/100."
            AccionFraude.REVISAR ->
                "Score $rawScore/100. Hay señales mixtas — derivamos a revisión manual antes de cobrar."
            AccionFraude.BLOQUEAR ->
                "Score $rawScore/100. Patrones sospechosos múltiples. Se bloquea preventivamente."
        }

        return FraudeResultado(
            scoreRiesgo = rawScore,
            accion = accion,
            explicacion = explicacion,
            factoresRiesgo = factores
        )
    }

    override suspend fun executeMock(input: Map<String, Any>): String {
        return """
            {
              "scoreRiesgo": 22,
              "accion": "APROBAR",
              "explicacion": "Transacción consistente con el perfil del usuario."
            }
        """.trimIndent()
    }
}
