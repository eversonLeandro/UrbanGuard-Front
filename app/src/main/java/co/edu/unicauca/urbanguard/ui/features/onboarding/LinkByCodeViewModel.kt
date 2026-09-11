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

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRequesting = true,
                requestError = null
            )
            requestLinkUseCase(conjuntoId).fold(
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

    fun onErrorDismissed() {
        _uiState.value = _uiState.value.copy(
            searchError = null,
            requestError = null
        )
    }
}