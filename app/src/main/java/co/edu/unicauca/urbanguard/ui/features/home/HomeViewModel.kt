package co.edu.unicauca.urbanguard.ui.features.home

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
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val session = sessionManager.session.value
        val activeConjunto = sessionManager.getActiveConjunto()
        _uiState.update {
            it.copy(
                userName = session?.name ?: "",
                userRole = when (session?.role) {
                    UserRole.ADMIN -> "ADMINISTRADOR"
                    UserRole.GUARD -> "GUARDIA"
                    UserRole.RESIDENT -> "RESIDENTE"
                    UserRole.USER -> "USUARIO"
                    UserRole.SUPERADMIN -> "SUPERADMIN"
                    null -> ""
                },
                role = session?.role,
                conjuntoName = activeConjunto?.nombre ?: "Bosques del Refugio"
            )
        }
    }

    fun onLogout() {
        sessionManager.clearSession()
    }
}
