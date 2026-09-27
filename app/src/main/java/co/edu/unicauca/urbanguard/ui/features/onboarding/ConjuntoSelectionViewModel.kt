package co.edu.unicauca.urbanguard.ui.features.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GetMyConjuntosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConjuntoSelectionViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val getMyConjuntosUseCase: GetMyConjuntosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConjuntoSelectionUiState())
    val uiState: StateFlow<ConjuntoSelectionUiState> = _uiState.asStateFlow()

    init {
        loadConjuntos()
    }

    fun loadConjuntos() {
        Log.d("ConjuntoSelVM", "loadConjuntos()")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            getMyConjuntosUseCase().fold(
                onSuccess = { conjuntos ->
                    Log.d("ConjuntoSelVM", "Éxito: ${conjuntos.size} conjuntos")
                    // SessionManager ya conserva el conjunto activo cuando sigue vigente
                    // en la lista nueva, asi que consultar la lista (por ejemplo al
                    // cambiar de conjunto desde Inicio) no deja al usuario sin conjunto.
                    sessionManager.setAssociations(conjuntos)
                    val session = sessionManager.session.value
                    _uiState.value = ConjuntoSelectionUiState(
                        isLoading = false,
                        conjuntos = conjuntos,
                        conjuntoActivoId = session?.conjuntoActivoId,
                        userName = session?.name.orEmpty()
                    )
                },
                onFailure = { error ->
                    Log.e("ConjuntoSelVM", "Fallo: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = AppError.from(error).message
                            ?: "Ocurrió un error al cargar tus conjuntos."
                    )
                }
            )
        }
    }

    fun onConjuntoSelected(conjuntoId: String): UserRole? {
        sessionManager.setActiveConjunto(conjuntoId)
        _uiState.value = _uiState.value.copy(conjuntoActivoId = conjuntoId)
        return currentRole()
    }

    fun currentRole(): UserRole? = sessionManager.currentRole()
}
