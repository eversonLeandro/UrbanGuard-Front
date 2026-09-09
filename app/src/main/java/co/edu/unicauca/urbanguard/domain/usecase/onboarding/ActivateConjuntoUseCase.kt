package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.core.common.OnboardingBypass
import co.edu.unicauca.urbanguard.domain.repository.OnboardingRepository
import javax.inject.Inject

/**
 * Valida el código de activación ACT-XXXX-YYYY del conjunto.
 *
 * El backend, al validar, crea el registro del Conjunto con PK autogenerada
 * y la retorna (NO retorna el código de activación de vuelta). La firma
 * esperada: sin parámetros implícitos, recibe el código y retorna el conjuntoId.
 */
class ActivateConjuntoUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(codigoActivacion: String): Result<String> {
        if (OnboardingBypass.ENABLED) {
            // Valor quemado mientras no exista el endpoint real.
            return Result.success(OnboardingBypass.BYPASS_CONJUNTO_ID)
        } else {
            // DESCOMENTAR CUANDO ESTÉ EL BACKEND
            // Llama al endpoint real `POST onboarding/activation-code/validate`
            // que recibe el código ACT-XXXX-YYYY y retorna el conjuntoId.
            // return repository.validateActivationCode(codigoActivacion)
            return Result.failure(Exception("Bypass desactivado y endpoint real no implementado todavía"))
        }
    }
}
