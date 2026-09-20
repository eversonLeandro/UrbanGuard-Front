package co.edu.unicauca.urbanguard.ui.features.profile

import androidx.lifecycle.ViewModel
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val session = sessionManager.session.value
        _uiState.update {
            it.copy(
                userName = session?.name ?: "",
                userRole = when (session?.role) {
                    UserRole.ADMIN -> "ADMIN"
                    UserRole.GUARD -> "GUARD"
                    UserRole.RESIDENT -> "RESIDENT"
                    UserRole.USER -> "USER"
                    UserRole.SUPERADMIN -> "SUPERADMIN"
                    null -> ""
                },
                role = session?.role
            )
        }
    }

    fun onLogout() {
        sessionManager.clearSession()
    }
}