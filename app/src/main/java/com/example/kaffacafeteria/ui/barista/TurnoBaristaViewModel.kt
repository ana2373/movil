package com.example.kaffacafeteria.ui.barista

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaffacafeteria.KaffaApp
import com.example.kaffacafeteria.data.remote.api.CashRegisterApi
import com.example.kaffacafeteria.data.remote.api.TurnoApi
import com.example.kaffacafeteria.data.remote.dto.CajaCierreRequest
import com.example.kaffacafeteria.data.remote.dto.CajaRequest
import com.example.kaffacafeteria.data.remote.dto.TurnoActivoDto
import com.example.kaffacafeteria.util.ApiErrors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Estado operativo del barista.
 *
 * Reglas que aplica:
 *  - Sin turno vigente → no puede operar (mensaje del backend).
 *  - Con turno pero sin caja abierta → no puede operar: primero debe abrir caja.
 *  - Con turno y caja abierta → puede operar.
 *
 * Abrir caja es el paso de arranque permitido: por eso la app NO bloquea la
 * navegación a la pantalla de caja cuando el motivo es CAJA_CERRADA.
 */
data class TurnoUiState(
    val cargando: Boolean = false,
    val turno: TurnoActivoDto? = null,
    val error: String? = null,

    val isOpeningCaja: Boolean = false,
    val isClosingCaja: Boolean = false,
    val cajaId: Int? = null,
    val montoAperturaCaja: String = "",
    val montoCierreCaja: String = "",
    val mensaje: String? = null
) {
    /** El backend es la autoridad: `puede_operar` ya combina turno y caja. */
    val puedeOperar: Boolean get() = turno?.puedeOperar == true

    val turnoActivo: Boolean get() = turno?.turnoActivo == true

    val cajaAbierta: Boolean get() = turno?.cajaAbierta == true

    /** Sin caja abierta pero con turno: hay que abrir la caja. */
    val debeAbrirCaja: Boolean get() = turno?.turnoActivo == true && turno?.cajaAbierta == false

    val motivoBloqueo: String?
        get() = if (puedeOperar) null else turno?.motivoBloqueo

    /** Texto listo para mostrar en el banner de bloqueo. */
    val mensajeBloqueo: String?
        get() = when {
            puedeOperar -> null
            turno == null -> "No se pudo verificar tu turno"
            !turnoActivo -> turno.motivoBloqueo ?: "No tienes un turno activo"
            !cajaAbierta -> "Debes abrir la caja para empezar a operar"
            else -> turno.motivoBloqueo ?: "No puedes operar en este momento"
        }
}

class TurnoBaristaViewModel(application: Application) : AndroidViewModel(application) {
    private val turnoApi: TurnoApi = (application as KaffaApp).container.turnoApi
    private val cashRegisterApi: CashRegisterApi = (application as KaffaApp).container.cashRegisterApi

    var uiState by mutableStateOf(TurnoUiState())
        private set

    init {
        refrescar()
    }

    /** Consulta el estado de turno y caja. Es seguro llamarlo cada cierto tiempo. */
    fun refrescar(silencioso: Boolean = false) {
        viewModelScope.launch {
            if (!silencioso) uiState = uiState.copy(cargando = true, error = null)
            try {
                val response = turnoApi.getTurnoActivo()
                if (response.isSuccessful && response.body() != null) {
                    val turno = response.body()!!
                    uiState = uiState.copy(
                        cargando = false,
                        turno = turno,
                        cajaId = turno.cajaId ?: uiState.cajaId,
                        error = null
                    )
                } else {
                    uiState = uiState.copy(
                        cargando = false,
                        error = ApiErrors.parse(response, "No se pudo verificar tu turno")
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(cargando = false, error = "No se pudo conectar con el servidor")
            }
        }
    }

    /**
     * Refresca el estado cada 15 segundos para que abrir o cerrar caja desde
     * otra pestaña se refleje sin que el barista tenga que recargar a mano.
     */
    fun iniciarVigilancia() {
        viewModelScope.launch {
            while (true) {
                delay(15_000)
                refrescar(silencioso = true)
            }
        }
    }

    fun updateMontoApertura(value: String) {
        uiState = uiState.copy(montoAperturaCaja = value.filter { it.isDigit() || it == '.' })
    }

    fun updateMontoCierre(value: String) {
        uiState = uiState.copy(montoCierreCaja = value.filter { it.isDigit() || it == '.' })
    }

    /**
     * Abre la caja con el monto físico inicial.
     *
     * El backend rechaza una segunda caja abierta, por eso tras el éxito se
     * vuelve a consultar el estado en vez de asumir que ya puede operar.
     */
    fun abrirCaja() {
        val monto = uiState.montoAperturaCaja.toDoubleOrNull() ?: 0.0
        if (uiState.isOpeningCaja) return

        viewModelScope.launch {
            uiState = uiState.copy(isOpeningCaja = true, error = null, mensaje = null)
            try {
                val response = cashRegisterApi.createCaja(CajaRequest(montoAperturaFisico = monto))
                if (response.isSuccessful) {
                    uiState = uiState.copy(
                        isOpeningCaja = false,
                        montoAperturaCaja = "",
                        mensaje = "Caja abierta. Ya puedes atender pedidos."
                    )
                    refrescar()
                } else {
                    uiState = uiState.copy(
                        isOpeningCaja = false,
                        error = ApiErrors.parse(response, "No se pudo abrir la caja")
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isOpeningCaja = false, error = "No se pudo conectar con el servidor")
            }
        }
    }

    /** Cierra la caja con el monto físico de cierre. */
    fun cerrarCaja() {
        val cajaId = uiState.cajaId ?: uiState.turno?.cajaId
        val monto = uiState.montoCierreCaja.toDoubleOrNull() ?: 0.0
        if (cajaId == null || uiState.isClosingCaja) return

        viewModelScope.launch {
            uiState = uiState.copy(isClosingCaja = true, error = null, mensaje = null)
            try {
                val response = cashRegisterApi.cerrarCaja(
                    cajaId,
                    CajaCierreRequest(montoCierreFisico = monto)
                )
                if (response.isSuccessful) {
                    uiState = uiState.copy(
                        isClosingCaja = false,
                        montoCierreCaja = "",
                        cajaId = null,
                        mensaje = "Caja cerrada correctamente"
                    )
                    refrescar()
                } else {
                    uiState = uiState.copy(
                        isClosingCaja = false,
                        error = ApiErrors.parse(response, "No se pudo cerrar la caja")
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isClosingCaja = false, error = "No se pudo conectar con el servidor")
            }
        }
    }

    fun limpiarMensajes() {
        uiState = uiState.copy(mensaje = null, error = null)
    }
}
