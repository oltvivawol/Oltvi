package com.oltvi.core.ai.agents

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.delay

abstract class BaseAgent(
    protected val geminiKey: String,
    protected val mockMode: Boolean = false
) {
    abstract val agentName: String
    abstract val systemPrompt: String
    abstract val agentTools: List<Tool>

    protected val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = geminiKey,
            tools = agentTools,
            systemInstruction = content { text(systemPrompt) },
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 1024
            }
        )
    }

    protected suspend fun chat(
        userMessage: String,
        history: List<Content> = emptyList()
    ): String {
        if (mockMode) return executeMock(mapOf("message" to userMessage))
        repeat(3) { attempt ->
            try {
                val chat = model.startChat(history)
                val response = chat.sendMessage(userMessage)
                return response.text ?: "Sin respuesta"
            } catch (e: Exception) {
                Log.w(agentName, "Attempt ${attempt + 1} failed: ${e.message}")
                if (attempt < 2) delay((1000L * (attempt + 1)))
            }
        }
        return executeMock(mapOf("message" to userMessage, "fallback" to "true"))
    }

    protected abstract suspend fun executeMock(input: Map<String, Any>): String
}
