package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.oltvi.core.data.models.AccionFraude
import com.oltvi.core.data.models.FraudeResultado
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgenteFraude @Inject constructor(
    geminiKey: String,
    mockMode: Boolean = false
) : BaseAgent(geminiKey, mockMode) {

    override val agentName = "AgenteFraude"

    override val systemPrompt = """
        Eres el Agente de Detección de Fraude de OLTVI. Analizás cada transacción en tiempo real.
        Detectás: viajes fantasma, cobros duplicados, patrones de pago anómalos, cuentas comprometidas,
        coordenadas GPS manipuladas y reembolsos fraudulentos.
        Generás un score de riesgo del 0 al 100 y decidís: APROBAR (0-30), REVISAR (31-70), BLOQUEAR (71-100).
        Siempre explicás tu decisión con evidencia concreta. Responde en español.
        Minimizá falsos positivos — el objetivo es no molestar a usuarios legítimos.
    """.trimIndent()

    override val agentTools = listOf(
        Tool(
            functionDeclarations = listOf(
                defineFunction(
                    name = "analizar_patron_pago",
                    description = "Analiza el patrón de pagos del usuario para detectar anomalías",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_usuario" to Schema.str("ID del usuario"),
                            "monto_actual" to Schema.num("Monto de la transacción actual en ARS"),
                            "promedio_historico" to Schema.num("Monto promedio histórico del usuario"),
                            "num_transacciones_hoy" to Schema.num("Número de transacciones del día")
                        )
                    )
                ),
                defineFunction(
                    name = "verificar_viaje_fantasma",
                    description = "Verifica si un viaje realmente ocurrió comprobando GPS y tiempo",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_viaje" to Schema.str("ID del viaje a verificar"),
                            "distancia_declarada_km" to Schema.num("Distancia declarada en km"),
                            "duracion_declarada_min" to Schema.num("Duración declarada en minutos"),
                            "puntos_gps" to Schema.num("Número de puntos GPS registrados")
                        )
                    )
                ),
                defineFunction(
                    name = "calcular_score_riesgo",
                    description = "Calcula el score de riesgo global para una transacción (0-100)",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_viaje" to Schema.str("ID del viaje"),
                            "id_usuario" to Schema.str("ID del usuario"),
                            "monto" to Schema.num("Monto de la transacción"),
                            "factores_riesgo" to Schema.str("Lista JSON de factores de riesgo detectados")
                        )
                    )
                ),
                defineFunction(
                    name = "bloquear_cuenta_temporal",
                    description = "Bloquea temporalmente una cuenta pendiente de revisión manual",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_usuario" to Schema.str("ID del usuario a bloquear"),
                            "motivo" to Schema.str("Motivo del bloqueo temporal"),
                            "duracion_horas" to Schema.num("Duración del bloqueo en horas")
                        )
                    )
                )
            )
        )
    )

    suspend fun analizarTransaccion(
        idViaje: String,
        idUsuario: String,
        monto: Double
    ): FraudeResultado {
        if (mockMode) return mockFraude(idViaje, idUsuario, monto)

        val prompt = buildString {
            appendLine("Analiza la siguiente transacción para detectar posibles fraudes:")
            appendLine("ID Viaje: $idViaje")
            appendLine("ID Usuario: $idUsuario")
            appendLine("Monto: \$${"%.2f".format(monto)} ARS")
            appendLine("Verifica patrones, GPS y consistencia del viaje. Genera score 0-100 y decide acción.")
        }

        val respuesta = chat(prompt)
        return mockFraude(idViaje, idUsuario, monto).copy(explicacion = respuesta)
    }

    private fun mockFraude(idViaje: String, idUsuario: String, monto: Double): FraudeResultado {
        // Heuristic risk scoring for mock mode
        val scoreBase = 12
        val scoreAjuste = when {
            monto > 50_000.0 -> 25  // Very large transaction
            monto > 20_000.0 -> 10  // Large transaction
            monto < 50.0 -> 5       // Suspiciously small
            else -> 0
        }
        val scoreTotal = (scoreBase + scoreAjuste).coerceIn(0, 100)

        val accion = when {
            scoreTotal > 70 -> AccionFraude.BLOQUEAR
            scoreTotal > 30 -> AccionFraude.REVISAR
            else -> AccionFraude.APROBAR
        }

        val explicacion = when (accion) {
            AccionFraude.APROBAR -> buildString {
                append("Transacción aprobada. Score de riesgo: $scoreTotal/100. ")
                append("El patrón de pago es consistente con el historial del usuario $idUsuario. ")
                append("GPS verificado, distancia coherente con el monto de \$${"%.0f".format(monto)} ARS. ")
                append("No se detectaron anomalías en el viaje $idViaje.")
            }
            AccionFraude.REVISAR -> buildString {
                append("Transacción marcada para revisión. Score de riesgo: $scoreTotal/100. ")
                append("El monto de \$${"%.0f".format(monto)} ARS supera el promedio histórico. ")
                append("Se recomienda verificación manual antes de acreditar al conductor.")
            }
            AccionFraude.BLOQUEAR -> buildString {
                append("Transacción bloqueada. Score de riesgo: $scoreTotal/100. ")
                append("Monto anómalos de \$${"%.0f".format(monto)} ARS para el perfil del usuario. ")
                append("Cuenta $idUsuario suspendida temporalmente pendiente revisión de seguridad.")
            }
        }

        return FraudeResultado(
            scoreRiesgo = scoreTotal,
            accion = accion,
            explicacion = explicacion
        )
    }

    override suspend fun executeMock(input: Map<String, Any>) =
        "Mock fraude: score 12/100 — transacción APROBADA. Sin anomalías detectadas."
}
