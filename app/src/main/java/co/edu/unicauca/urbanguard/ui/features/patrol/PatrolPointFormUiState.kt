package co.edu.unicauca.urbanguard.ui.features.patrol

/**
 * Estado del formulario de punto de control (crear y editar).
 *
 * [nfcState] describe el lector: reposo, esperando tag, tag leído o equipo
 * sin NFC. El id también se puede escribir a mano para equipos sin lector.
 */
data class PatrolPointFormUiState(
    val step: Int = 0,
    val name: String = "",
    val location: String = "",
    val nfcTagId: String = "",
    val isActive: Boolean = true,
    val nfcState: NfcReadState = NfcReadState.IDLE,
    val nameError: String? = null,
    val tagError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

enum class NfcReadState {
    IDLE,
    WAITING,
    READ,
    UNSUPPORTED
}
