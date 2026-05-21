package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.oltvi.core.data.models.InsightOperaciones
import com.oltvi.core.data.models.TipoInsight
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgenteOperaciones @Inject constructor(
    geminiKey: String,
    mockMode: Boolean = false
) : BaseAgent(geminiKey, mockMode) {

    override val agentName = "AgenteOperaciones"

    override val systemPrompt = """
        Eres el Agente de Inteligencia Operacional de OLTVI. Proveés insights predictivos al equipo admin.
        Analizás: demanda por zona/hora, disponibilidad de flota, revenue por segmento, anomalías operativas.
        Tus insights son accionables y tienen impacto cuantificado en pesos argentinos.
        Priorizás por impacto económico y urgencia operacional. Responde en español.
        Sé preciso con los números — el equipo toma decisiones con tus datos.
    """.trimIndent()

    override val agentTools = listOf(
        Tool(
            functionDeclarations = listOf(
                defineFunction(
                    name = "predecir_demanda",
                    description = "Predice la demanda de servicios por zona y franja horaria",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "zona" to Schema.str("Zona geográfica de Buenos Aires"),
                            "hora_inicio" to Schema.num("Hora de inicio de la franja (0-23)"),
                            "hora_fin" to Schema.num("Hora de fin de la franja (0-23)"),
                            "dias_historico" to Schema.num("Días de histórico a considerar")
                        )
                    )
                ),
                defineFunction(
                    name = "detectar_escasez_conductores",
                    description = "Detecta zonas con escasez de conductores disponibles",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "umbral_tiempo_espera_min" to Schema.num("Umbral de tiempo de espera en minutos"),
                            "zona" to Schema.str("Zona a analizar, 'todas' para ciudad completa")
                        )
                    )
                ),
                defineFunction(
                    name = "generar_reporte_flota",
                    description = "Genera un reporte de salud de la flota activa",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "incluir_mantenimiento" to Schema.bool("Si incluir estado de mantenimiento"),
                            "incluir_documentacion" to Schema.bool("Si incluir estado de documentación"),
                            "formato" to Schema.str("Formato del reporte: resumen, detallado")
                        )
                    )
                ),
                defineFunction(
                    name = "identificar_anomalias_revenue",
                    description = "Identifica anomalías en los ingresos comparado con períodos anteriores",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "periodo" to Schema.str("Período actual: dia, semana, mes"),
                            "umbral_desviacion_pct" to Schema.num("Umbral de desviación porcentual para alertar")
                        )
                    )
                )
            )
        )
    )

    suspend fun generarInsight(tipo: TipoInsight): InsightOperaciones {
        if (mockMode) return mockInsight(tipo)

        val prompt = buildString {
            appendLine("Genera un insight operacional de tipo ${tipo.name} para OLTVI.")
            appendLine("Debe ser concreto, con datos numéricos y acción recomendada.")
            appendLine("Contexto: plataforma de ride-sharing en Buenos Aires, Argentina.")
        }

        val respuesta = chat(prompt)
        return mockInsight(tipo).copy(descripcion = respuesta)
    }

    private fun mockInsight(tipo: TipoInsight): InsightOperaciones = when (tipo) {
        TipoInsight.DEMANDA -> InsightOperaciones(
            tipo = TipoInsight.DEMANDA,
            titulo = "Pico de demanda proyectado en Palermo",
            descripcion = "Los datos históricos indican un aumento del 340% en solicitudes en Palermo " +
                    "entre las 20:00 y 23:00 los viernes. Con solo 12 conductores activos en la zona, " +
                    "el tiempo de espera promedio subirá a 14 min. Recomendamos incentivar 8 conductores " +
                    "adicionales con bono de \$800 ARS por viaje completado en esa ventana.",
            impactoEstimado = "+\$180.000 ARS en revenue incremental",
            prioridad = 1
        )
        TipoInsight.FLOTA -> InsightOperaciones(
            tipo = TipoInsight.FLOTA,
            titulo = "3 vehículos requieren mantenimiento urgente",
            descripcion = "Los vehículos ABC-123, DEF-456 y GHI-789 superaron los 10.000 km desde " +
                    "su último servicio. Según el protocolo OLTVI, deben ser desafectados del servicio " +
                    "antes de las 18:00 de hoy. El conductor promedio pierde \$4.200 ARS/día de servicio, " +
                    "por lo que coordinamos turnos para minimizar el impacto.",
            impactoEstimado = "-\$12.600 ARS pérdida evitable si se gestiona hoy",
            prioridad = 2
        )
        TipoInsight.REVENUE -> InsightOperaciones(
            tipo = TipoInsight.REVENUE,
            titulo = "Revenue 18% por debajo del martes pasado",
            descripcion = "El revenue acumulado hoy (\$1.24M ARS) está 18% por debajo del mismo " +
                    "período del martes pasado (\$1.51M ARS). La caída se concentra en el segmento " +
                    "MENSAJERÍA (-31%) posiblemente por la competencia de Rappi en zona Norte. " +
                    "Se sugiere activar campaña de descuentos del 15% para mensajería hasta el domingo.",
            impactoEstimado = "Recuperación estimada de \$270.000 ARS",
            prioridad = 1
        )
        TipoInsight.SEGURIDAD -> InsightOperaciones(
            tipo = TipoInsight.SEGURIDAD,
            titulo = "Zona Villa Lugano con alertas elevadas",
            descripcion = "Se registraron 4 alertas de seguridad en Villa Lugano en las últimas 6 horas, " +
                    "contra un promedio histórico de 0.8 alertas. Dos conductores solicitaron asistencia " +
                    "manual. Recomendamos restringir viajes nocturnos en esa zona y notificar proactivamente " +
                    "a los 8 conductores activos en el área.",
            impactoEstimado = "Prevención de 2+ incidentes graves estimados",
            prioridad = 1
        )
        TipoInsight.GENERAL -> InsightOperaciones(
            tipo = TipoInsight.GENERAL,
            titulo = "Satisfacción del conductor cayó 0.3 puntos",
            descripcion = "El NPS interno de conductores bajó de 7.8 a 7.5 esta semana. " +
                    "Las 3 quejas más frecuentes: demoras en el pago (38%), asignaciones lejanas (27%) " +
                    "y soporte lento (21%). La app de conductores tiene 4.1★ en Play Store, " +
                    "abajo de la competencia (4.4★). Acciones sugeridas: pago instantáneo y mejora del copiloto.",
            impactoEstimado = "Retención de ~40 conductores activos en riesgo",
            prioridad = 2
        )
    }

    override suspend fun executeMock(input: Map<String, Any>) =
        "Mock operaciones: insight generado con datos históricos de Buenos Aires."
}
