package co.edu.unicauca.urbanguard.ui.navigation

import androidx.lifecycle.ViewModel
import co.edu.unicauca.urbanguard.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    // Post-login: carga las asociaciones (falsas por ahora) en la sesión
    fun resolvePostLogin() {
        sessionManager.loadMockAssociations()
    }
}