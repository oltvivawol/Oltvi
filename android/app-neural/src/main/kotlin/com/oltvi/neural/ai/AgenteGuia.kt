package com.oltvi.neural.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.MensajeGuia
import com.oltvi.neural.data.NivelNeural
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import kotlinx.coroutines.delay
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * IA guía permanente de Capa Neural.
 *
 * Conoce el perfil del usuario (clase RPG, objetivo de vida, nivel, XP),
 * la ciudad y la filosofía de la plataforma. Acompaña al usuario en su
 * progreso real, no solo en el juego.
 */
@Singleton
class AgenteGuia @Inject constructor(
    @Named("neuralGeminiKey") private val geminiKey: String
) {
    private val mockMode = geminiKey.isBlank() || geminiKey == "mock"

    private val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = geminiKey,
            generationConfig = generationConfig {
                temperature = 0.8f
                maxOutputTokens = 512
            },
            systemInstruction = content(role = "system") { text(BASE_SYSTEM_PROMPT) }
        )
    }

    private val conversationHistory = mutableListOf<com.google.ai.client.generativeai.type.Content>()

    suspend fun saludar(perfil: PerfilNeural): MensajeGuia {
        val prompt = buildSaludoPrompt(perfil)
        val respuesta = chat(prompt)
        return MensajeGuia(UUID.randomUUID().toString(), respuesta, esIA = true)
    }

    suspend fun responder(
        mensajeUsuario: String,
        perfil: PerfilNeural
    ): MensajeGuia {
        val contexto = buildContextoPrompt(perfil)
        val prompt = "$contexto\n\nUsuario: $mensajeUsuario"
        val respuesta = chat(prompt)
        return MensajeGuia(UUID.randomUUID().toString(), respuesta, esIA = true)
    }

    suspend fun generarPrimerMision(perfil: PerfilNeural): String {
        val prompt = """
            El usuario ${perfil.nombre} acaba de ingresar a Capa Neural.
            Su clase es ${perfil.clase.displayName} y su objetivo es ${perfil.objetivo.displayName}.
            Sugiere una primera misión concreta, motivadora y alcanzable en el día de hoy.
            Formato: título (máx 8 palabras), luego una descripción de 2-3 oraciones.
            Sin emojis en el título. Respondé en español rioplatense.
        """.trimIndent()
        return chat(prompt)
    }

    private suspend fun chat(userMessage: String): String {
        if (mockMode) return mockResponse(userMessage)
        var lastError: Exception? = null
        repeat(3) { attempt ->
            try {
                val chatSession = model.startChat(conversationHistory)
                val response = chatSession.sendMessage(userMessage)
                val text = response.text ?: ""
                if (text.isNotBlank()) {
                    conversationHistory.addAll(listOf(
                        content(role = "user") { text(userMessage) },
                        content(role = "model") { text(text) }
                    ))
                }
                return text
            } catch (e: Exception) {
                lastError = e
                Log.w("NeuralAI", "[AgenteGuia] Attempt ${attempt + 1} failed: ${e.message}")
                delay(1000L * (1 shl attempt))
            }
        }
        Log.e("NeuralAI", "[AgenteGuia] All retries failed, using mock", lastError)
        return mockResponse(userMessage)
    }

    fun limpiarHistorial() {
        conversationHistory.clear()
    }

    private fun buildSaludoPrompt(perfil: PerfilNeural): String = """
        Contexto del usuario:
        - Nombre: ${perfil.nombre}
        - Clase: ${perfil.clase.displayName} (${perfil.clase.description})
        - Objetivo de vida: ${perfil.objetivo.displayName}
        - Nivel actual: ${perfil.nivel.displayName} (Nivel ${perfil.numeroNivel})
        - XP: ${perfil.xpActual}
        - Misiones completadas: ${perfil.misionesCompletadas}

        Es la primera vez que el usuario entra a Capa Neural en esta sesión.
        Saludalo de manera cálida y personalizada. Recordale su objetivo.
        Mencioná qué puede hacer hoy para avanzar. Máximo 3 oraciones.
        Sin emojis. Español rioplatense.
    """.trimIndent()

    private fun buildContextoPrompt(perfil: PerfilNeural): String = """
        Contexto actual del usuario:
        Nombre: ${perfil.nombre} | Clase: ${perfil.clase.displayName} |
        Objetivo: ${perfil.objetivo.displayName} | Nivel: ${perfil.nivel.displayName} |
        XP: ${perfil.xpActual} | Reputación: ${perfil.reputacion}
    """.trimIndent()

    private fun mockResponse(input: String): String {
        val lower = input.lowercase()
        return when {
            "salud" in lower || "bienvenid" in lower ->
                "Buenas. El camino hacia tu objetivo empieza con un primer paso concreto. ¿Qué podés hacer hoy que te acerque aunque sea un poco a donde querés estar?"
            "trabajo" in lower ->
                "Para conseguir trabajo hay que moverse en dos frentes: mejorar lo que ofrecés y acercarte a quienes pueden necesitarte. ¿Querés que armemos un plan para esta semana?"
            "misión" in lower || "mision" in lower ->
                "Cada misión que completás es un paso real. No importa el tamaño, importa la constancia. ¿Querés que te sugiera la siguiente misión según tu objetivo?"
            "negocio" in lower ->
                "Un negocio empieza con una idea clara y un cliente real. Antes de pensar en escalar, encontrá a la primera persona dispuesta a pagarte por lo que ofrecés."
            "aprender" in lower || "estudiar" in lower ->
                "Aprender es más efectivo cuando está conectado a un objetivo concreto. ¿Qué querés poder hacer cuando terminés de aprender eso?"
            else ->
                "Entendido. ¿Querés que te ayude a convertir eso en una misión concreta o preferís explorar el mapa de tu barrio para ver qué oportunidades hay cerca?"
        }
    }

    companion object {
        private val BASE_SYSTEM_PROMPT = """
            Sos la IA guía de CAPA NEURAL, una plataforma que convierte la ciudad real en un
            mundo interactivo donde las personas pueden progresar, aprender, trabajar y conectarse.

            Tu rol es ser guía, mentor y compañero de progreso. No sos un asistente genérico:
            conocés al usuario, sus metas y su barrio. Tu función es ayudarle a avanzar en la
            vida real, no solo en el juego.

            Filosofía central:
            - Las plataformas tradicionales capturan atención. CAPA NEURAL genera progreso.
            - No preguntamos qué quiere hacer el usuario. Preguntamos qué quiere lograr.
            - El éxito no es tiempo de uso: es impacto real en la vida de la persona.

            Reglas de comunicación:
            - Español rioplatense natural y cálido. Sin tecnicismos innecesarios.
            - Respuestas cortas y accionables (máximo 4 oraciones).
            - Sin emojis en las respuestas.
            - Nunca prometés resultados: mostrás rutas posibles.
            - Si el usuario está bloqueado, proponés el paso más pequeño posible.
            - Celebrás los logros sin exagerar. El progreso real es suficiente.
        """.trimIndent()
    }
}
