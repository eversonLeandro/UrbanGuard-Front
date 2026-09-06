package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private fun loadExistingCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingInitial = true, errorMessage = null)
            getConjuntoCodeUseCase().fold(
                onSuccess = { conjuntoCode ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingInitial = false,
                        code = conjuntoCode.code,
                        isCodeActive = conjuntoCode.isActive,
                        // TODO: reemplazar por conjuntoCode.conjuntoName cuando
                        // venga del ConjuntoConfig real (UG-45/UG-46)
                        conjuntoName = conjuntoCode.conjuntoName
                    )
                },
                onFailure = { error ->
                    // 404 = aún no hay código generado → estado vacío (no es error real)
                    // Cualquier otro error sí se muestra al usuario
                    val isNoCodeYet = error.message?.contains("404") == true ||
                            error.message?.contains("no existe") == true
                    _uiState.value = _uiState.value.copy(
                        isLoadingInitial = false,
                        code = if (isNoCodeYet) null else _uiState.value.code,
                        errorMessage = if (isNoCodeYet) null else error.message
                    )
                }
            )
        }
    }

    fun onGenerateClick() {
        if (_uiState.value.isLoadingAction) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAction = true, errorMessage = null)
            generateConjuntoCodeUseCase().fold(
                onSuccess = { conjuntoCode ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        code = conjuntoCode.code,
                        isCodeActive = conjuntoCode.isActive,
                        conjuntoName = conjuntoCode.conjuntoName
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        errorMessage = error.message
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
        if (_uiState.value.isLoadingAction) return
        _uiState.value = _uiState.value.copy(
            showRegenerateConfirm = false,
            isLoadingAction = true,
            errorMessage = null
        )
        viewModelScope.launch {
            regenerateConjuntoCodeUseCase().fold(
                onSuccess = { conjuntoCode ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        code = conjuntoCode.code,
                        isCodeActive = conjuntoCode.isActive,
                        conjuntoName = conjuntoCode.conjuntoName
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        errorMessage = error.message
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