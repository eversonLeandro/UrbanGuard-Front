package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.RequestLinkUseCase
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.SearchConjuntoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkByCodeViewModel @Inject constructor(
    private val searchConjuntoUseCase: SearchConjuntoUseCase,
    private val requestLinkUseCase: RequestLinkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkByCodeUiState())
    val uiState: StateFlow<LinkByCodeUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(
            code = value,
            codeError = null,
            searchError = null,
            // Si el usuario edita el código, limpia el resultado anterior
            conjuntoFound = null
        )
    }

    // Máximo de dígitos permitidos para el número de apartamento
    fun onApartamentoChange(value: String) {
        // Solo dígitos (bloquea signos y negativos) + límite de longitud
        val filtered = value.filter { it.isDigit() }.take(APARTAMENTO_MAX_LENGTH)
        _uiState.value = _uiState.value.copy(
            apartamento = filtered,
            apartamentoError = null
        )
    }

    private fun validateApartamento(): String? {
        val apartamento = _uiState.value.apartamento
        if (apartamento.isBlank()) {
            return "Ingresa el número de apartamento"
        }
        if (apartamento.length < APARTAMENTO_MIN_LENGTH) {
            return "El apartamento debe tener al menos $APARTAMENTO_MIN_LENGTH dígitos"
        }
        return null
    }

    fun onSearchClick() {
        if (_uiState.value.isSearching) return
        val code = _uiState.value.code

        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(
                codeError = "Ingresa el código del conjunto"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearching = true,
                searchError = null,
                conjuntoFound = null
            )
            searchConjuntoUseCase(code).fold(
                onSuccess = { conjunto ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        conjuntoFound = conjunto
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchError = error.message
                    )
                }
            )
        }
    }

    fun onRequestLinkClick() {
        val conjuntoId = _uiState.value.conjuntoFound?.id ?: return
        if (_uiState.value.isRequesting) return

        val apartamentoError = validateApartamento()
        if (apartamentoError != null) {
            _uiState.value = _uiState.value.copy(apartamentoError = apartamentoError)
            return
        }
        val apartamento = _uiState.value.apartamento

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRequesting = true,
                requestError = null
            )
            requestLinkUseCase(conjuntoId, apartamento).fold(
                onSuccess = { request ->
                    _uiState.value = _uiState.value.copy(
                        isRequesting = false,
                        linkRequest = request,
                        requestSuccess = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isRequesting = false,
                        requestError = error.message
                    )
                }
            )
        }
    }

    companion object {
        private const val APARTAMENTO_MIN_LENGTH = 1
        private const val APARTAMENTO_MAX_LENGTH = 4
    }
}