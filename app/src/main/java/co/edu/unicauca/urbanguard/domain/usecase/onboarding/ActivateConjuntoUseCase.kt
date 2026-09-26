package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.core.common.OnboardingBypass
import co.edu.unicauca.urbanguard.domain.repository.OnboardingRepository
import javax.inject.Inject

/**
 * Verifica el código de activación ACT-XXXX-YYYY del conjunto.
 *
 * El backend, al validar, CREA el conjunto y responde 204 No Content:
 * no hay body que devolver, por eso la firma es Result<Unit>.
 */
class ActivateConjuntoUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(activationCode: String): Result<Unit> {
        if (OnboardingBypass.ENABLED) {
            // Éxito simulado: el código se acepta sin backend.
            return Result.success(Unit)
        } else {
            // DESCOMENTAR CUANDO ESTÉ EL BACKEND
            // Llama al endpoint real `POST activations/verifications`
            // que valida el código ACT-XXXX-YYYY y responde 204 No Content.
            return repository.validateActivationCode(activationCode)
            //return Result.failure(Exception("Bypass desactivado y endpoint real no implementado todavía"))
        }
    }
}
