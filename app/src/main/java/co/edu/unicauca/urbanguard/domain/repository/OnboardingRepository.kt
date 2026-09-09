package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.OnboardingStatus

interface OnboardingRepository {
    suspend fun getOnboardingStatus(): Result<OnboardingStatus>

    // Valida el código de activación ACT-XXXX-YYYY del conjunto.
    // El backend crea el conjunto y retorna su PK autogenerada (conjuntoId).
    suspend fun validateActivationCode(codigoActivacion: String): Result<String>
}