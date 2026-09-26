package co.edu.unicauca.urbanguard.ui.features.onboarding

import co.edu.unicauca.urbanguard.domain.model.AppError

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.ConfigureConjuntoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterConjuntoViewModel @Inject constructor(
    private val configureConjuntoUseCase: ConfigureConjuntoUseCase,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // activationCode recibido como argumento de navegación desde ActivateConjunto.
    private val activationCode: String? = savedStateHandle.get<String>("activationCode")

    private val _uiState = MutableStateFlow(RegisterConjuntoUiState())
    val uiState: StateFlow<RegisterConjuntoUiState> = _uiState.asStateFlow()

    init {
        if (activationCode == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No se recibió el código de activación. Intenta de nuevo."
            )
        }
    }

    fun onCreateClick(
        nombre: String,
        direccion: String,
        ciudad: String,
        totalUnidades: Int
    ) {
        val currentActivationCode = activationCode
        if (currentActivationCode == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No se recibió el código de activación. Intenta de nuevo."
            )
            return
        }
        if (_uiState.value.isLoadingAction) return
        _uiState.value = _uiState.value.copy(isLoadingAction = true, errorMessage = null)
        viewModelScope.launch {
            configureConjuntoUseCase(
                activationCode = currentActivationCode,
                nombre = nombre,
                direccion = direccion,
                ciudad = ciudad,
                totalUnidades = totalUnidades
            ).fold(
                onSuccess = { createdComplex ->
                    // El usuario pasa a ser ADMIN del conjunto recién creado.
                    // Se agrega a su lista de conjuntos y se marca como activo.
                    sessionManager.addConjunto(
                        ConjuntoUserAssociation(
                            conjuntoId = createdComplex.complexId,
                            nombre = nombre,
                            roleEnConjunto = UserRole.ADMIN
                        )
                    )
                    sessionManager.setActiveConjunto(createdComplex.complexId)
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        isUpdateSuccess = true
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
}