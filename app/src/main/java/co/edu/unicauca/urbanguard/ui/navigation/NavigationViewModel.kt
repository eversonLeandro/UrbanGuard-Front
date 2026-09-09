package co.edu.unicauca.urbanguard.ui.navigation

import androidx.lifecycle.ViewModel
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GetOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    suspend fun resolveAdminPostLogin(): Boolean {
        val status = getOnboardingStatusUseCase().getOrNull()
        val hasConjunto = status?.hasConjunto ?: false
        sessionManager.updateSession(hasConjunto)
        return hasConjunto
    }
}