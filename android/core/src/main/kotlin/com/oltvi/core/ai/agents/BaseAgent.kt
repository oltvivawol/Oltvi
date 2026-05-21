package com.oltvi.core.ai.agents

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.delay

/**
 * Shared base class for every OLTVI AI agent.
 *
 *  - Each agent supplies its own `systemPrompt`, optional function-calling
 *    `agentTools`, and a deterministic `executeMock` used when [mockMode] is
 *    enabled OR when the live Gemini call fails after 3 retries with
 *    exponential back-off (1s -> 2s -> 4s).
 *  - The Gemini [GenerativeModel] is built lazily so unit tests in mock mode
 *    don't accidentally instantiate it.
 */
abstract class BaseAgent(
    protected val geminiKey: String,
    protected val mockMode: Boolean
) {
    abstract val systemPrompt: String
    abstract val agentName: String
    open val agentTools: List<Tool> = emptyList()

    protected val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = geminiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 1024
            },
            systemInstruction = content(role = "system") { text(systemPrompt) },
            tools = if (agentTools.isNotEmpty()) agentTools else null
        )
    }

    protected suspend fun chat(userMessage: String, history: List<Content> = emptyList()): String {
        if (mockMode) return executeMock(mapOf("message" to userMessage))
        var lastError: Exception? = null
        repeat(3) { attempt ->
            try {
                val chatInstance = model.startChat(history)
                val response = chatInstance.sendMessage(userMessage)
                return response.text ?: ""
            } catch (e: Exception) {
                lastError = e
                Log.w("OltviAI", "[$agentName] Attempt ${attempt + 1} failed: ${e.message}")
                delay(1000L * (1 shl attempt))
            }
        }
        Log.e("OltviAI", "[$agentName] All retries failed, using mock fallback", lastError)
        return executeMock(mapOf("message" to userMessage))
    }

    protected abstract suspend fun executeMock(input: Map<String, Any>): String
}
