package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.usecase.guards.RegisterGuardUseCase
import co.edu.unicauca.urbanguard.domain.validation.InputValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NO_COMPLEX_MESSAGE =
    "No hay un conjunto seleccionado. Elige uno e inténtalo de nuevo."

private const val EMAIL_BLANK_MESSAGE = "Ingresa el correo del vigilante"
private const val EMAIL_FORMAT_MESSAGE = "El correo no tiene un formato válido"
private const val NOMBRES_BLANK_MESSAGE = "Ingresa los nombres del vigilante"
private const val APELLIDOS_BLANK_MESSAGE = "Ingresa los apellidos del vigilante"
private const val PHONE_BLANK_MESSAGE = "Ingresa el teléfono del vigilante"

// Enseña qué se acepta en vez de repetir el nombre de la regla: el `@Pattern`
// del backend solo diría "mal", así que este texto es la única guía que el
// usuario tiene antes de que el servidor rechace la entrada.
private const val PHONE_CHARS_MESSAGE = "El teléfono solo admite dígitos, +, (), - y espacios"

/**
 * Alta de vigilante (HU-009) desde el menú de seguridad del administrador.
 *
 * El `complexId` no viaja por la ruta de navegación: se resuelve contra el
 * conjunto activo de la sesión, igual que hace la pantalla de join code. Así
 * `Routes.REGISTER_GUARD` sigue siendo una ruta sin argumentos y el
 * administrador no tiene que volver a elegir el conjunto en cada alta.
 */
@HiltViewModel
class RegisterGuardViewModel @Inject constructor(
    private val registerGuardUseCase: RegisterGuardUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterGuardUiState())
    val uiState: StateFlow<RegisterGuardUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null, errorMessage = null)
    }

    fun onNombresChange(value: String) {
        _uiState.value = _uiState.value.copy(nombres = value, nombresError = null, errorMessage = null)
    }

    fun onApellidosChange(value: String) {
        _uiState.value = _uiState.value.copy(
            apellidos = value,
            apellidosError = null,
            errorMessage = null
        )
    }

    fun onTelefonoChange(value: String) {
        _uiState.value = _uiState.value.copy(
            telefono = value,
            telefonoError = null,
            errorMessage = null
        )
    }

    fun onSubmit() {
        if (_uiState.value.isSubmitting) return

        val state = _uiState.value

        val invalid = state.withFieldErrors()
        if (invalid != null) {
            _uiState.value = invalid
            return
        }

        val complexId = targetComplexId()
        if (complexId == null) {
            _uiState.value = state.copy(errorMessage = NO_COMPLEX_MESSAGE)
            return
        }

        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            registerGuardUseCase(
                complexId = complexId,
                email = state.email.trim(),
                nombres = state.nombres.trim(),
                apellidos = state.apellidos.trim(),
                telefono = state.telefono.trim()
            ).fold(
                onSuccess = { created ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        createdGuard = created
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = AppError.from(error).message
                    )
                }
            )
        }
    }

    /**
     * Descarta el resultado exitoso. La pantalla lo usa justo antes de navegar
     * atrás, de modo que si el usuario vuelve a abrir la pantalla de registro no
     * le salte el diálogo de un alta anterior.
     */
    fun onCreatedGuardHandled() {
        _uiState.value = _uiState.value.copy(createdGuard = null)
    }

    /**
     * Conjunto destino del alta: el activo y, si aún no se ha fijado, el
     * primero en el que el usuario tiene membresía. Es la misma resolución que
     * usa la pantalla de join code.
     */
    private fun targetComplexId(): String? =
        sessionManager.getActiveConjunto()?.conjuntoId
            ?: sessionManager.session.value?.conjuntosAsociados?.firstOrNull()?.conjuntoId
}

/**
 * Copia del estado con los errores de cada campo ya resueltos, o `null` si los
 * cuatro están correctos.
 *
 * Devolver el estado completo en vez de un `Map<String, String>` obliga a que
 * la pantalla siga leyendo los errores desde un solo sitio, y que no se pueda
 * olvidar limpiar el banner al invalidar el formulario.
 */
private fun RegisterGuardUiState.withFieldErrors(): RegisterGuardUiState? {
    val emailError = when {
        email.isBlank() -> EMAIL_BLANK_MESSAGE
        !InputValidators.isValidEmail(email) -> EMAIL_FORMAT_MESSAGE
        else -> null
    }
    val nombresError = when {
        nombres.isBlank() -> NOMBRES_BLANK_MESSAGE
        !InputValidators.isValidName(nombres) -> nameTooLongMessage("Los nombres")
        else -> null
    }
    val apellidosError = when {
        apellidos.isBlank() -> APELLIDOS_BLANK_MESSAGE
        !InputValidators.isValidName(apellidos) -> nameTooLongMessage("Los apellidos")
        else -> null
    }
    val telefonoError = when {
        telefono.isBlank() -> PHONE_BLANK_MESSAGE
        telefono.trim().length > InputValidators.PHONE_MAX_LENGTH ->
            "El teléfono no puede exceder ${InputValidators.PHONE_MAX_LENGTH} caracteres"
        !InputValidators.isValidPhone(telefono) -> PHONE_CHARS_MESSAGE
        else -> null
    }

    if (listOf(emailError, nombresError, apellidosError, telefonoError).all { it == null }) {
        return null
    }
    return copy(
        emailError = emailError,
        nombresError = nombresError,
        apellidosError = apellidosError,
        telefonoError = telefonoError,
        errorMessage = null
    )
}

/** Redacta el mismo límite para "Los nombres" y para "Los apellidos". */
private fun nameTooLongMessage(label: String): String =
    "$label no pueden exceder ${InputValidators.NAME_MAX_LENGTH} caracteres"
