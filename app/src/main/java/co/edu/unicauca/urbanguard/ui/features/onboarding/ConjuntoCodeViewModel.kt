package co.edu.unicauca.urbanguard.ui.features.onboarding

import co.edu.unicauca.urbanguard.domain.model.AppError

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GenerateConjuntoCodeUseCase
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GetConjuntoCodeUseCase
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.RegenerateConjuntoCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConjuntoCodeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val getConjuntoCodeUseCase: GetConjuntoCodeUseCase,
    private val generateConjuntoCodeUseCase: GenerateConjuntoCodeUseCase,
    private val regenerateConjuntoCodeUseCase: RegenerateConjuntoCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConjuntoCodeUiState())
    val uiState: StateFlow<ConjuntoCodeUiState> = _uiState.asStateFlow()

    init {
        // Al abrir la pantalla, intentamos cargar el código existente
        loadExistingCode()
    }

    private fun getTargetComplexId(): String? {
        return sessionManager.getActiveConjunto()?.conjuntoId 
            ?: sessionManager.session.value?.conjuntosAsociados?.firstOrNull()?.conjuntoId
    }

    private fun loadExistingCode() {
        val complexId = getTargetComplexId()
        if (complexId == null) {
            _uiState.value = _uiState.value.copy(
                isLoadingInitial = false,
                errorMessage = null // Avoid showing red error popup
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingInitial = true, errorMessage = null)
            getConjuntoCodeUseCase(complexId).fold(
                onSuccess = { conjuntoCode ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingInitial = false,
                        code = conjuntoCode.code,
                        expiresAt = conjuntoCode.expiresAt,
                        conjuntoName = "" // Se cargará del ConjuntoConfig en UG-45/UG-46
                    )
                },
                onFailure = { error ->
                    // 404 = aún no hay código generado → estado vacío (no es error real)
                    // Cualquier otro error sí se muestra al usuario
                    val appError = AppError.from(error)
                    val isNoCodeYet = appError is AppError.NotFound
                    _uiState.value = _uiState.value.copy(
                        isLoadingInitial = false,
                        code = if (isNoCodeYet) null else _uiState.value.code,
                        errorMessage = if (isNoCodeYet) null else appError.message
                    )
                }
            )
        }
    }

    fun onGenerateClick() {
        val complexId = getTargetComplexId()
        if (complexId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = null
            )
            return
        }
        if (_uiState.value.isLoadingAction) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAction = true, errorMessage = null)
            generateConjuntoCodeUseCase(complexId).fold(
                onSuccess = { conjuntoCode ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        code = conjuntoCode.code,
                        expiresAt = conjuntoCode.expiresAt,
                        conjuntoName = "" // Se cargará del ConjuntoConfig en UG-45/UG-46
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        errorMessage = AppError.from(error).message
                    )
                }
            )
        }
    }

    fun onRegenerateClick() {
        // Solo muestra el modal de confirmación, no regenera todavía
        _uiState.value = _uiState.value.copy(showRegenerateConfirm = true)
    }

    fun onRegenerateConfirmed() {
        val complexId = getTargetComplexId()
        if (complexId == null) {
            _uiState.value = _uiState.value.copy(
                showRegenerateConfirm = false,
                errorMessage = null
            )
            return
        }
        if (_uiState.value.isLoadingAction) return
        _uiState.value = _uiState.value.copy(
            showRegenerateConfirm = false,
            isLoadingAction = true,
            errorMessage = null
        )
        viewModelScope.launch {
            regenerateConjuntoCodeUseCase(complexId).fold(
                onSuccess = { conjuntoCode ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        code = conjuntoCode.code,
                        expiresAt = conjuntoCode.expiresAt,
                        conjuntoName = "" // Se cargará del ConjuntoConfig en UG-45/UG-46
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        errorMessage = AppError.from(error).message
                    )
                }
            )
        }
    }

    fun onRegenerateDismissed() {
        _uiState.value = _uiState.value.copy(showRegenerateConfirm = false)
    }

    fun onCopyCode(code: String, copyToClipboard: (String) -> Unit) {
        copyToClipboard(code)
        _uiState.value = _uiState.value.copy(copySuccess = true)
        // Resetea el feedback de copia después de 2 segundos
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(copySuccess = false)
        }
    }

    fun onErrorDismissed() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}