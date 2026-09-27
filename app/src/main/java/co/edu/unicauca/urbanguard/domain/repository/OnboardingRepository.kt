package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.OnboardingStatus

interface OnboardingRepository {
    suspend fun getOnboardingStatus(): Result<OnboardingStatus>

    // Verifica el código de activación ACT-XXXX-YYYY (POST activations/verifications).
    // Éxito = 204 No Content sin body → se modela como Unit.
    suspend fun validateActivationCode(code: String): Result<Unit>
}
