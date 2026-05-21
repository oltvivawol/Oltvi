package com.oltvi.usuario.screens.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oltvi.core.ai.orchestrator.OltviOrchestrator
import com.oltvi.core.data.models.MensajeChat
import com.oltvi.core.data.models.TipoMensaje
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<MensajeChat> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val orchestrator: OltviOrchestrator
) : ViewModel() {

    private val _uiState = mutableStateOf(ChatUiState())
    val uiStateFlow: State<ChatUiState> = _uiState

    init {
        // Welcome message from Olivi
        addAiMessage(
            "¡Hola! Soy Olivi, tu asistente OLTVI. ¿En qué te puedo ayudar hoy? Puedo ayudarte con información sobre tu viaje, pagos, cancelaciones y mucho más."
        )
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage(text: String = _uiState.value.inputText) {
        if (text.isBlank()) return

        val userMessage = MensajeChat(
            id = UUID.randomUUID().toString(),
            contenido = text,
            esIA = false,
            tipo = TipoMensaje.TEXT
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            inputText = "",
            isTyping = true
        )

        viewModelScope.launch {
            try {
                val response = orchestrator.atenderSoporte(
                    mensaje = text,
                    historialMensajes = _uiState.value.messages
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + response,
                    isTyping = false
                )
            } catch (e: Exception) {
                addAiMessage("Lo siento, hubo un problema al procesar tu consulta. Por favor, intentá de nuevo.")
                _uiState.value = _uiState.value.copy(isTyping = false)
            }
        }
    }

    private fun addAiMessage(content: String) {
        val msg = MensajeChat(
            id = UUID.randomUUID().toString(),
            contenido = content,
            esIA = true,
            tipo = TipoMensaje.TEXT
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + msg
        )
    }
}
