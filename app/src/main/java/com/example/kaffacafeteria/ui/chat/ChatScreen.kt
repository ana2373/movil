package com.example.kaffacafeteria.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kaffacafeteria.data.remote.dto.ContactoUsuarioDto
import com.example.kaffacafeteria.ui.components.LoadingIndicator
import com.example.kaffacafeteria.util.createViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = createViewModel { ChatViewModel(it) }
) {
    val state = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme

    val currentContact = state.selectedContact

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        TopAppBar(
            title = { Text(currentContact?.nombre ?: "Chat") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colorScheme.onPrimary)
                }
            },
            actions = {
                if (currentContact != null) {
                    TextButton(onClick = { viewModel.loadContacts() }) { Text("Mis chats", color = colorScheme.onPrimary) }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary, titleContentColor = colorScheme.onPrimary)
        )

        if (state.error != null) {
            Text(state.error!!, modifier = Modifier.fillMaxWidth().padding(12.dp), color = colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }

        if (currentContact == null) {
            ContactList(
                contacts = state.contacts,
                isLoading = state.isLoading,
                onSelect = viewModel::selectContact,
                onRefresh = viewModel::loadContacts
            )
        } else {
            ConversationView(
                contact = currentContact,
                messages = state.messages,
                myUserId = state.myUserId,
                isLoading = state.isLoading,
                inputText = state.inputText,
                sending = state.sending,
                onInputChange = viewModel::updateInput,
                onSend = viewModel::sendMessage
            )
        }
    }
}

@Composable
private fun ContactList(
    contacts: List<ContactoUsuarioDto>,
    isLoading: Boolean,
    onSelect: (ContactoUsuarioDto) -> Unit,
    onRefresh: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    if (isLoading && contacts.isEmpty()) {
        LoadingIndicator()
        return
    }
    if (contacts.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Chat, contentDescription = null, tint = colorScheme.outline, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No hay contactos disponibles", style = MaterialTheme.typography.titleMedium, color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Usa el botón de actualizar para recargar", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                onClick = { onSelect(contact) }
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = colorScheme.primaryContainer, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(contact.nombre.firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Black, color = colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contact.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(contact.correo, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            TextButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text("Actualizar contactos") }
        }
    }
}

@Composable
private fun ConversationView(
    contact: ContactoUsuarioDto,
    messages: List<com.example.kaffacafeteria.data.remote.dto.MensajeDto>,
    myUserId: Int?,
    isLoading: Boolean,
    inputText: String,
    sending: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading && messages.isEmpty()) {
                item { LoadingIndicator() }
            } else if (messages.isEmpty()) {
                item {
                    Text("Sin mensajes. ¡Saluda a ${contact.nombre}!", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp))
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.remitenteId == myUserId
                    MessageBubble(text = msg.mensaje, isMine = isMine, colorScheme = colorScheme)
                }
            }
        }

        HorizontalDivider(color = colorScheme.outlineVariant)
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = { Text("Escribe un mensaje...") },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorScheme.primary, focusedLabelColor = colorScheme.primary, cursorColor = colorScheme.primary, unfocusedBorderColor = colorScheme.outline)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !sending,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = colorScheme.secondary, contentColor = colorScheme.onSecondary)
            ) {
                if (sending) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSecondary, strokeWidth = 2.dp)
                else Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}

@Composable
private fun MessageBubble(text: String, isMine: Boolean, colorScheme: androidx.compose.material3.ColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(if (isMine) 16.dp else 16.dp),
            color = if (isMine) colorScheme.primary else colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(if (isMine) 0.75f else 0.75f)
        ) {
            Text(
                text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMine) colorScheme.onPrimary else colorScheme.onSurface
            )
        }
    }
}
