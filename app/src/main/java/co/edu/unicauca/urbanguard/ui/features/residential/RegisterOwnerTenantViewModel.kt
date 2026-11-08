package co.edu.unicauca.urbanguard.ui.features.residential

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.ResidentType
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.usecase.residential.RegisterOwnerTenantUseCase
import co.edu.unicauca.urbanguard.domain.validation.InputValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterOwnerTenantViewModel @Inject constructor(
    private val registerOwnerTenantUseCase: RegisterOwnerTenantUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterOwnerTenantUiState())
    val uiState: StateFlow<RegisterOwnerTenantUiState> = _uiState.asStateFlow()

    fun onOpen(unitId: String) {
        if (_uiState.value.isSaving) return
        _uiState.value = RegisterOwnerTenantUiState(unitId = unitId)
    }

    fun onSuccessConsumed() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun onTypeChange(type: ResidentType) {
        _uiState.value = _uiState.value.copy(type = type, serverError = null)
    }

    fun onNombresChange(value: String) {
        _uiState.value = _uiState.value.copy(
            nombres = value,
            fieldErrors = _uiState.value.fieldErrors - OwnerTenantField.NOMBRES,
            serverError = null
        )
    }

    fun onApellidosChange(value: String) {
        _uiState.value = _uiState.value.copy(
            apellidos = value,
            fieldErrors = _uiState.value.fieldErrors - OwnerTenantField.APELLIDOS,
            serverError = null
        )
    }

    fun onDocumentoChange(value: String) {
        _uiState.value = _uiState.value.copy(
            documento = value,
            fieldErrors = _uiState.value.fieldErrors - OwnerTenantField.DOCUMENTO,
            serverError = null
        )
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            fieldErrors = _uiState.value.fieldErrors - OwnerTenantField.EMAIL,
            serverError = null
        )
    }

    fun onTelefonoChange(value: String) {
        _uiState.value = _uiState.value.copy(
            telefono = value,
            fieldErrors = _uiState.value.fieldErrors - OwnerTenantField.TELEFONO,
            serverError = null
        )
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isSaving) return
        if (sessionManager.currentRole() != UserRole.ADMIN) {
            _uiState.value = state.copy(
                serverError = AppError.Forbidden(NO_ADMIN_MESSAGE)
            )
            return
        }

        val fieldErrors = validate(state)
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(fieldErrors = fieldErrors, serverError = null)
            return
        }

        val unitId = state.unitId
        if (unitId.isNullOrBlank()) {
            _uiState.value = state.copy(
                serverError = AppError.Validation(NO_UNIT_MESSAGE)
            )
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(
                serverError = AppError.Validation(NO_ACTIVE_COMPLEX_MESSAGE)
            )
            return
        }

        val draft = OwnerTenantDraft(
            type = state.type,
            nombres = state.nombres.trim(),
            apellidos = state.apellidos.trim(),
            documento = state.documento.trim(),
            email = state.email.trim(),
            telefono = state.telefono.trim()
        )
        _uiState.value = state.copy(
            isSaving = true,
            fieldErrors = emptyMap(),
            serverError = null,
            successMessage = null
        )
        viewModelScope.launch {
            registerOwnerTenantUseCase(complexId, unitId, draft).fold(
                onSuccess = { resident ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        successMessage = "${resident.fullName} asignado al apartamento " +
                            "${resident.unitIdentifier} como ${resident.type.label}"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        serverError = AppError.from(error)
                    )
                }
            )
        }
    }

    private fun validate(state: RegisterOwnerTenantUiState): Map<OwnerTenantField, String> =
        buildMap {
            if (!InputValidators.isValidName(state.nombres)) {
                put(OwnerTenantField.NOMBRES, INVALID_NAMES_MESSAGE)
            }
            if (!InputValidators.isValidName(state.apellidos)) {
                put(OwnerTenantField.APELLIDOS, INVALID_LAST_NAMES_MESSAGE)
            }
            if (!InputValidators.isValidDocument(state.documento)) {
                put(OwnerTenantField.DOCUMENTO, INVALID_DOCUMENT_MESSAGE)
            }
            if (!InputValidators.isValidEmail(state.email)) {
                put(OwnerTenantField.EMAIL, INVALID_EMAIL_MESSAGE)
            }
            if (!InputValidators.isValidPhone(state.telefono)) {
                put(OwnerTenantField.TELEFONO, INVALID_PHONE_MESSAGE)
            }
        }

    private companion object {
        const val NO_ADMIN_MESSAGE =
            "No tienes permisos de administrador para asignar residentes."
        const val NO_UNIT_MESSAGE = "No se seleccionó un apartamento."
        const val NO_ACTIVE_COMPLEX_MESSAGE = "No hay un conjunto activo seleccionado."
        const val INVALID_NAMES_MESSAGE = "Ingresa nombres válidos de hasta 100 caracteres."
        const val INVALID_LAST_NAMES_MESSAGE =
            "Ingresa apellidos válidos de hasta 100 caracteres."
        const val INVALID_DOCUMENT_MESSAGE =
            "Ingresa un documento válido de hasta 30 caracteres."
        const val INVALID_EMAIL_MESSAGE = "Ingresa un correo electrónico válido."
        const val INVALID_PHONE_MESSAGE = "Ingresa un teléfono válido de hasta 30 caracteres."
    }
}
