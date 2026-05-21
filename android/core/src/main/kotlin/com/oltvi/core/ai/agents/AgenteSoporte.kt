package com.oltvi.core.ai.agents

import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.oltvi.core.data.models.ContextoViaje
import com.oltvi.core.data.models.MensajeChat
import com.oltvi.core.data.models.TipoMensaje
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AgenteSoporte @Inject constructor(
    @Named("geminiKey") geminiKey: String
) : BaseAgent(geminiKey, mockMode = geminiKey == "mock") {

    override val agentName: String = "AgenteSoporte"

    override val systemPrompt: String = """
        Sos el agente de Soporte al usuario de OLTVI. Atendés consultas en español
        rioplatense amigable. Resolvés dudas sobre: cancelaciones, reembolsos,
        objetos perdidos, problemas con el conductor, cobros y promociones. Sos
        empático, claro y siempre ofrecés un próximo paso concreto. No usás emojis
        salvo que el usuario los use primero.
    """.trimIndent()

    override val agentTools: List<Tool> = listOf(
        Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "responder_consulta",
                    description = "Responde una consulta de soporte y opcionalmente sugiere acciones.",
                    parameters = listOf(
                        Schema.str("respuesta", "Texto de la respuesta al usuario"),
                        Schema.str("categoria", "Categoría detectada: cancelacion, reembolso, perdido, conductor, cobro, general")
                    ),
                    requiredParameters = listOf("respuesta", "categoria")
                )
            )
        )
    )

    suspend fun resolverConsulta(
        idUsuario: String,
        mensaje: String,
        contextoViaje: ContextoViaje?
    ): MensajeChat {
        val texto: String
        val opciones: List<String>

        if (!mockMode) {
            val live = runCatching { chat(mensaje) }.getOrNull()
            if (!live.isNullOrBlank()) {
                return MensajeChat(
                    id = UUID.randomUUID().toString(),
                    contenido = live.trim(),
                    esIA = true,
                    timestamp = Instant.now(),
                    tipo = TipoMensaje.TEXTO,
                    opciones = emptyList()
                )
            }
        }

        val lower = mensaje.lowercase()
        when {
            "cancelar" in lower || "cancelo" in lower -> {
                texto = "Entiendo, querés cancelar el viaje. Si el conductor todavía no llegó, " +
                    "la cancelación es sin costo. Si ya está en camino hace más de 2 minutos, " +
                    "se aplica una tarifa mínima. ¿Querés que lo cancele ahora?"
                opciones = listOf("Cancelar sin cargo", "Esperar al conductor", "Hablar con un humano")
            }
            "reembolso" in lower || "devolver" in lower || "devolución" in lower -> {
                texto = "Para procesar un reembolso necesito el número de viaje y el motivo. " +
                    "El crédito vuelve al mismo medio de pago en 3 a 5 días hábiles."
                opciones = listOf("Iniciar reembolso", "Ver mis viajes", "Hablar con un humano")
            }
            "perdido" in lower || "olvidé" in lower || "objeto" in lower -> {
                texto = "Lamento lo del objeto perdido. Te conecto con el último conductor del viaje. " +
                    "Si no responde en 1 hora, escalamos al equipo de objetos perdidos."
                opciones = listOf("Contactar conductor", "Reportar objeto", "Hablar con un humano")
            }
            "problema" in lower || "queja" in lower || "mal" in lower -> {
                texto = "Disculpá la situación. Para ayudarte mejor, ¿podés contarme qué pasó " +
                    "durante el viaje? Tu reporte queda registrado y revisamos al conductor."
                opciones = listOf("Reportar conductor", "Reembolso parcial", "Hablar con un humano")
            }
            "promo" in lower || "código" in lower || "cupón" in lower -> {
                texto = "Tenemos promos activas para vos en la sección Beneficios. " +
                    "Si tenés un código específico, decímelo y lo valido."
                opciones = listOf("Ver beneficios", "Validar código")
            }
            "cobro" in lower || "precio" in lower || "caro" in lower -> {
                texto = "El precio se calcula por distancia, tiempo y demanda. " +
                    "Si pensás que algo está mal cobrado, abrimos una revisión sin compromiso."
                opciones = listOf("Revisar último cobro", "Hablar con un humano")
            }
            else -> {
                texto = "Hola${if (idUsuario.isNotBlank()) "" else ""}, te leo. " +
                    "Contame en una línea qué necesitás y te ayudo. " +
                    (contextoViaje?.idViaje?.let { "Veo que tenés activo el viaje $it." } ?: "")
                opciones = listOf("Mi último viaje", "Reembolsos", "Objeto perdido", "Hablar con un humano")
            }
        }

        return MensajeChat(
            id = UUID.randomUUID().toString(),
            contenido = texto,
            esIA = true,
            timestamp = Instant.now(),
            tipo = TipoMensaje.OPCION,
            opciones = opciones
        )
    }

    override suspend fun executeMock(input: Map<String, Any>): String {
        return """
            {
              "respuesta": "Hola, ¿en qué te puedo ayudar?",
              "categoria": "general"
            }
        """.trimIndent()
    }
}
