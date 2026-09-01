package com.example.kaffacafeteria.ui.gestion

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

data class TurnosUiState(
    val turnos: List<Turno> = emptyList()
)

class TurnosViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf(TurnosUiState())
        private set

    fun add(turno: Turno) {
        uiState = uiState.copy(turnos = uiState.turnos + turno)
    }

    fun remove(turno: Turno) {
        uiState = uiState.copy(turnos = uiState.turnos.filter { it != turno })
    }
}
