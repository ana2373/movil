package com.example.kaffacafeteria.ui.chat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.dto.ContactoUsuarioDto
import com.example.kaffacafeteria.data.remote.dto.MensajeDto
import com.example.kaffacafeteria.data.remote.dto.MensajeEnviarRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatUiState(
    val myUserId: Int? = null,
    val contacts: List<ContactoUsuarioDto> = emptyList(),
    val selectedContact: ContactoUsuarioDto? = null,
    val messages: List<MensajeDto> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val sending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val mensajeApi = (application as KaffaApp).container.mensajeApi
    private val authRepository = (application as KaffaApp).container.authRepository

    var uiState by mutableStateOf(ChatUiState())
        private set

    init {
        loadMe()
        loadContacts()
        startPolling()
    }

    private fun loadMe() {
        viewModelScope.launch {
            val result = authRepository.getMe()
            if (result is com.example.kaffacafeteria.util.Resource.Success) {
                uiState = uiState.copy(myUserId = result.data.id)
            }
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = mensajeApi.getContactos()
                uiState = uiState.copy(contacts = response.body() ?: emptyList(), isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error al cargar contactos")
            }
        }
    }

    fun selectContact(contact: ContactoUsuarioDto) {
        uiState = uiState.copy(selectedContact = contact, messages = emptyList())
        loadHilo(contact.id)
        marcarLeidos(contact.id)
    }

    fun loadHilo(conId: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val response = mensajeApi.getHilo(conId)
                uiState = uiState.copy(messages = response.body()?.mensajes?.data?.reversed() ?: emptyList(), isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Error al cargar conversación")
            }
        }
    }

    fun marcarLeidos(conId: Int) {
        viewModelScope.launch {
            try {
                mensajeApi.marcarLeidos(com.example.kaffacafeteria.data.remote.api.MarcarLeidosRequest(conId))
            } catch (e: Exception) {
                // no crítico
            }
        }
    }

    fun updateInput(value: String) {
        uiState = uiState.copy(inputText = value, error = null)
    }

    fun sendMessage() {
        val text = uiState.inputText.trim()
        val contact = uiState.selectedContact ?: return
        if (text.isEmpty()) return

        viewModelScope.launch {
            uiState = uiState.copy(sending = true, error = null)
            try {
                val response = mensajeApi.enviarMensaje(MensajeEnviarRequest(destinatarioId = contact.id, mensaje = text))
                if (response.isSuccessful) {
                    val msj = response.body()
                    if (msj != null) {
                        uiState = uiState.copy(
                            messages = uiState.messages + msj,
                            inputText = "",
                            sending = false
                        )
                    } else {
                        uiState = uiState.copy(sending = false, error = "Respuesta vacía del servidor")
                    }
                } else {
                    uiState = uiState.copy(sending = false, error = "No se pudo enviar: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(sending = false, error = e.message ?: "Error de conexión")
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                uiState.selectedContact?.let { contact ->
                    try {
                        val response = mensajeApi.getHilo(contact.id)
                        uiState = uiState.copy(messages = response.body()?.mensajes?.data?.reversed() ?: uiState.messages)
                        marcarLeidos(contact.id)
                    } catch (e: Exception) {
                        // silencioso
                    }
                }
                loadContacts()
            }
        }
    }
}
