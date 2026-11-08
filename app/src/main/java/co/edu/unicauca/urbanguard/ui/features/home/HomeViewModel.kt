package co.edu.unicauca.urbanguard.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.repository.ProfileStateHolder
import co.edu.unicauca.urbanguard.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val profileState: ProfileStateHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Se observa la sesion en lugar de leerla una unica vez en el init: el rol y el
        // conjunto activo se resuelven de forma asincrona (restauracion desde disco y
        // recarga de asociaciones) y una foto fija dejaba la pantalla desactualizada,
        // por ejemplo al cambiar de conjunto desde el Inicio.
        viewModelScope.launch {
            sessionManager.session.collect { session ->
                _uiState.value = HomeUiState(
                    userName = session?.name.orEmpty(),
                    userRole = session?.role?.label.orEmpty(),
                    role = session?.role,
                    conjuntoName = sessionManager.getActiveConjunto()?.nombre.orEmpty(),
                    profilePhotoUrl = profileState.currentProfile()?.profilePhotoUrl,
                    profilePhotoExpiresAt = profileState.currentProfile()?.profilePhotoExpiresAt
                )
                if (session != null) profileState.ensureFresh()
            }
        }
        viewModelScope.launch {
            profileState.profile.collect {
                val profile = profileState.currentProfile()
                _uiState.value = _uiState.value.copy(
                    profilePhotoUrl = profile?.profilePhotoUrl,
                    profilePhotoExpiresAt = profile?.profilePhotoExpiresAt,
                    userName = sessionManager.session.value?.name?.takeIf(String::isNotBlank)
                        ?: profile?.name.orEmpty()
                )
            }
        }
    }

    fun ensureFresh() { viewModelScope.launch { profileState.ensureFresh() } }
    fun onImageError(url: String) { viewModelScope.launch { profileState.refreshAfterImageError(url) } }

    private val UserRole.label: String
        get() = when (this) {
            UserRole.ADMIN -> "ADMINISTRADOR"
            UserRole.GUARD -> "GUARDIA"
            UserRole.RESIDENT -> "RESIDENTE"
            UserRole.USER -> "USUARIO"
            UserRole.SUPERADMIN -> "SUPERADMIN"
        }
}
