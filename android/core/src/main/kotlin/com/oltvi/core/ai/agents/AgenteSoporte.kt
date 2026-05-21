package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.MensajeChat
import com.oltvi.core.data.models.TipoMensaje
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgenteSoporte @Inject constructor(
    geminiKey: String,
    mockMode: Boolean = false
) : BaseAgent(geminiKey, mockMode) {

    override val agentName = "AgenteSoporte"

    override val systemPrompt = """
        Eres Olivi, el asistente de soporte de OLTVI. Sos amable, empático y resolvés problemas rápido.
        Podés: cancelar viajes, gestionar reembolsos, reportar problemas, escalar a humanos.
        Tu meta es resolver el 90% de consultas sin intervención humana.
        Respondé siempre en español. Mensajes cortos, máximo 3 oraciones salvo que sea un proceso complejo.
    """.trimIndent()

    override val agentTools = listOf(
        Tool(
            functionDeclarations = listOf(
                defineFunction(
                    name = "buscar_en_faqs",
                    description = "Busca la respuesta a una consulta en la base de conocimiento de OLTVI",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "consulta" to Schema.str("Texto de la consulta del usuario"),
                            "categoria" to Schema.str("Categoría: viaje, pago, conductor, app, cuenta")
                        )
                    )
                ),
                defineFunction(
                    name = "cancelar_viaje",
                    description = "Cancela un viaje activo con el motivo indicado",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_viaje" to Schema.str("ID del viaje a cancelar"),
                            "motivo" to Schema.str("Motivo de la cancelación"),
                            "id_usuario" to Schema.str("ID del usuario que cancela")
                        )
                    )
                ),
                defineFunction(
                    name = "iniciar_reembolso",
                    description = "Inicia el proceso de reembolso para un viaje",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_viaje" to Schema.str("ID del viaje"),
                            "monto" to Schema.num("Monto a reembolsar en ARS"),
                            "motivo" to Schema.str("Motivo del reembolso")
                        )
                    )
                ),
                defineFunction(
                    name = "crear_ticket_soporte",
                    description = "Crea un ticket de soporte para seguimiento",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_usuario" to Schema.str("ID del usuario"),
                            "descripcion" to Schema.str("Descripción del problema"),
                            "prioridad" to Schema.str("Prioridad: baja, media, alta, critica")
                        )
                    )
                ),
                defineFunction(
                    name = "escalar_a_humano",
                    description = "Escala la consulta a un agente humano de soporte",
                    parameters = Schema.obj(
                        properties = mapOf(
                            "id_usuario" to Schema.str("ID del usuario"),
                            "motivo_escalado" to Schema.str("Por qué se necesita intervención humana"),
                            "prioridad" to Schema.str("Prioridad del caso")
                        )
                    )
                )
            )
        )
    )

    suspend fun resolverConsulta(
        idUsuario: String,
        mensaje: String,
        contextoViaje: ContextoViaje?
    ): MensajeChat {
        if (mockMode) return mockSoporte(idUsuario, mensaje, contextoViaje)

        val prompt = buildString {
            appendLine("Usuario: $idUsuario")
            appendLine("Mensaje: $mensaje")
            contextoViaje?.let {
                appendLine("Contexto del viaje: ID=${it.idViaje}, Estado=${it.estadoActual?.displayName}")
            }
        }

        val respuesta = chat(prompt)
        return MensajeChat(
            id = UUID.randomUUID().toString(),
            contenido = respuesta,
            esIA = true,
            tipo = TipoMensaje.TEXT
        )
    }

    private fun mockSoporte(
        idUsuario: String,
        mensaje: String,
        contexto: ContextoViaje?
    ): MensajeChat {
        val mensajeLower = mensaje.lowercase()

        val respuesta = when {
            mensajeLower.contains("cancel") -> {
                val idViaje = contexto?.idViaje ?: "tu viaje"
                "Entiendo que querés cancelar $idViaje. Lo procesé sin cargo ya que el conductor aún no llegó. ¿Hay algo más en lo que te pueda ayudar?"
            }
            mensajeLower.contains("reembolso") || mensajeLower.contains("devolucion") ||
                    mensajeLower.contains("devolución") -> {
                "Iniciamos el reembolso a tu método de pago original. El monto se acredita en 3-5 días hábiles. Te enviaré un email de confirmación en breve."
            }
            mensajeLower.contains("conductor") || mensajeLower.contains("chofer") -> {
                "Lamentamos el inconveniente con el conductor. Registré tu reporte y el equipo lo va a revisar. Tu rating del viaje ayuda mucho. ¿Querés que escale el caso?"
            }
            mensajeLower.contains("pago") || mensajeLower.contains("cobro") -> {
                "Verifico tu historial de pagos. Todos los cobros están registrados correctamente. Si ves un monto incorrecto, enviame el comprobante y lo revisamos juntos."
            }
            mensajeLower.contains("demora") || mensajeLower.contains("tarda") -> {
                "El conductor está en camino, las condiciones de tráfico están afectando el ETA. Te aviso cuando esté a 2 minutos. ¡Gracias por la paciencia!"
            }
            mensajeLower.contains("hola") || mensajeLower.contains("ayuda") -> {
                "¡Hola! Soy Olivi, tu asistente de OLTVI. Puedo ayudarte con viajes, pagos, reembolsos o cualquier consulta. ¿Con qué te ayudo hoy?"
            }
            else -> {
                "Entendí tu consulta. Déjame verificar los detalles de tu cuenta para darte la mejor respuesta. ¿Podés darme un poco más de contexto sobre tu situación?"
            }
        }

        val opciones = when {
            mensajeLower.contains("cancel") -> listOf("Confirmar cancelación", "Mantener viaje")
            mensajeLower.contains("hola") || mensajeLower.contains("ayuda") ->
                listOf("Cancelar viaje", "Solicitar reembolso", "Reportar problema", "Hablar con humano")
            else -> emptyList()
        }

        return MensajeChat(
            id = UUID.randomUUID().toString(),
            contenido = respuesta,
            esIA = true,
            tipo = if (opciones.isNotEmpty()) TipoMensaje.OPTION else TipoMensaje.TEXT,
            opciones = opciones
        )
    }

    override suspend fun executeMock(input: Map<String, Any>) =
        "¡Hola! Soy Olivi. Estoy aquí para ayudarte con cualquier consulta sobre tu viaje."
}
