package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.ConfigureConjuntoUseCase
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GenerateConjuntoCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterConjuntoViewModel @Inject constructor(
    private val generateConjuntoCodeUseCase: GenerateConjuntoCodeUseCase,
    private val configureConjuntoUseCase: ConfigureConjuntoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // conjuntoId recibido como argumento de navegación desde ActivateConjunto.
    private val conjuntoId: String? = savedStateHandle.get<String>("conjuntoId")

    private val _uiState = MutableStateFlow(RegisterConjuntoUiState())
    val uiState: StateFlow<RegisterConjuntoUiState> = _uiState.asStateFlow()

    init {
        // Al abrir la pantalla, se genera el código de activación una sola vez
        // mientras el usuario llena el formulario, usando el conjuntoId recibido.
        if (conjuntoId == null) {
            _uiState.value = _uiState.value.copy(
                isLoadingInitial = false,
                errorMessage = "No se recibió el conjunto. Intenta de nuevo."
            )
        } else {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoadingInitial = true, errorMessage = null)
                generateConjuntoCodeUseCase(conjuntoId).fold(
                    onSuccess = { conjuntoCode ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingInitial = false,
                            code = conjuntoCode.code,
                            isCodeActive = conjuntoCode.isActive
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingInitial = false,
                            errorMessage = error.message
                        )
                    }
                )
            }
        }
    }

    fun onCopyClick() {
        _uiState.value = _uiState.value.copy(copySuccess = true)
        // Resetea el feedback de copia después de 2 segundos
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(copySuccess = false)
        }
    }

    fun onCreateClick(
        nombre: String,
        direccion: String,
        ciudad: String,
        totalUnidades: Int
    ) {
        val currentConjuntoId = conjuntoId
        if (currentConjuntoId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No se recibió el conjunto. Intenta de nuevo."
            )
            return
        }
        if (_uiState.value.isLoadingAction) return
        _uiState.value = _uiState.value.copy(isLoadingAction = true, errorMessage = null)
        viewModelScope.launch {
            configureConjuntoUseCase(
                conjuntoId = currentConjuntoId,
                nombre = nombre,
                direccion = direccion,
                ciudad = ciudad,
                totalUnidades = totalUnidades
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        isUpdateSuccess = true
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
}
