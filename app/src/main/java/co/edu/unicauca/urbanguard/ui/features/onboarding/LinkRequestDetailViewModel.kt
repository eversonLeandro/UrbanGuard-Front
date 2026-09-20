package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoAssociationRepository
import co.edu.unicauca.urbanguard.data.remote.api.service.CommunityApi
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GetLinkRequestDetailUseCase
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.ReviewLinkRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkRequestDetailViewModel @Inject constructor(
    private val getLinkRequestDetailUseCase: GetLinkRequestDetailUseCase,
    private val reviewLinkRequestUseCase: ReviewLinkRequestUseCase,
    private val conjuntoAssociationRepository: ConjuntoAssociationRepository,
    private val sessionManager: SessionManager,
    private val communityApi: CommunityApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val requestId: String? = savedStateHandle.get<String>("requestId")

    private val _uiState = MutableStateFlow(LinkRequestDetailUiState())
    val uiState: StateFlow<LinkRequestDetailUiState> = _uiState.asStateFlow()

    init {
        loadRequest()
    }

    private fun loadRequest() {
        if (_uiState.value.isLoading) _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val id = requestId
            if (id == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No se pudo identificar la solicitud"
                )
                return@launch
            }
            getLinkRequestDetailUseCase(id).fold(
                onSuccess = { request ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        request = request,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        request = null,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    fun onApproveClick() {
        if (_uiState.value.isActionEnabled) {
            _uiState.value = _uiState.value.copy(showApproveDialog = true)
        }
    }

    fun onRejectClick() {
        if (_uiState.value.isActionEnabled) {
            _uiState.value = _uiState.value.copy(showRejectDialog = true)
        }
    }

    fun onApproveConfirmed() {
        _uiState.value = _uiState.value.copy(showApproveDialog = false, isLoadingAction = true, errorMessage = null)
        reviewRequest(approve = true)
    }

    fun onRejectConfirmed() {
        _uiState.value = _uiState.value.copy(showRejectDialog = false, isLoadingAction = true, errorMessage = null)
        reviewRequest(approve = false)
    }

    fun onDialogDismissed() {
        _uiState.value = _uiState.value.copy(showApproveDialog = false, showRejectDialog = false)
    }

    fun onErrorDismissed() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun reviewRequest(approve: Boolean) {
        viewModelScope.launch {
            val id = requestId
            if (id == null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingAction = false,
                    errorMessage = "No se pudo identificar la solicitud"
                )
                return@launch
            }
            reviewLinkRequestUseCase(id, approve).fold(
                onSuccess = { reviewed: LinkRequest ->
                    if (approve) {
                        communityApi.refreshCommunityContext()
                        conjuntoAssociationRepository.getMyConjuntos()
                            .onSuccess(sessionManager::setAssociations)
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoadingAction = false,
                        request = reviewed,
                        errorMessage = null,
                        successMessage = if (approve) {
                            if (reviewed.roleSyncPending || reviewed.organizationSyncPending) {
                                "Solicitud aprobada localmente, pero la sincronización con Auth0 quedó pendiente."
                            } else {
                                "Solicitud aceptada correctamente. El usuario fue agregado como residente."
                            }
                        } else {
                            "Solicitud rechazada correctamente."
                        },
                        reviewResult = if (approve) "APROBADA" else "RECHAZADA"
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

    fun onSuccessDismissed() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun consumeReviewResult() {
        _uiState.value = _uiState.value.copy(reviewResult = null)
    }
}