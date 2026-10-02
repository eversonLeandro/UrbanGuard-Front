package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GetMyConjuntosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuardHomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val getMyConjuntosUseCase: GetMyConjuntosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuardHomeUiState())
    val uiState: StateFlow<GuardHomeUiState> = _uiState.asStateFlow()

    init {
        loadConjuntoAndObserve()
    }

    /**
     * El guardia pertenece a un unico conjunto, asi que se resuelve solo al entrar: se
     * piden las asociaciones al backend, se activa la que corresponda y la UI queda
     * escuchando la sesion.
     *
     * Sin esto, tras un arranque en frio la sesion llega sin conjunto resuelto (aun
     * faltan setAssociations + setActiveConjunto) y la pantalla tendria que inventar
     * los datos del usuario.
     */
    private fun loadConjuntoAndObserve() {
        // La observacion arranca antes que la red: al navegar, la sesion ya fue
        // restaurada, asi que el nombre se muestra de inmediato y la carga solo lo refresca.
        viewModelScope.launch {
            sessionManager.session.collect { session ->
                _uiState.update { current ->
                    current.copy(
                        userName = session?.name?.takeIf { it.isNotBlank() }
                            ?: session?.userId?.takeIf { it.isNotBlank() }
                            .orEmpty(),
                        conjuntoName = sessionManager.getActiveConjunto()?.nombre.orEmpty()
                    )
                }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMyConjuntosUseCase().onSuccess { conjuntos ->
                sessionManager.setAssociations(conjuntos)
                // El guardia pertenece a un solo conjunto: si la sesion aun no tiene
                // ninguno resuelto (la lista persistida no lo incluia), se activa el
                // primero. SessionManager ya conserva el activo cuando sigue vigente.
                if (sessionManager.session.value?.conjuntoActivoId == null && conjuntos.isNotEmpty()) {
                    sessionManager.setActiveConjunto(conjuntos.first().conjuntoId)
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
