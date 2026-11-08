package co.edu.unicauca.urbanguard.domain.usecase.auth

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import co.edu.unicauca.urbanguard.domain.validation.InputValidators
import javax.inject.Inject

class RequestPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val normalizedEmail = email.trim()
        if (!InputValidators.isValidEmail(normalizedEmail)) {
            return Result.failure(
                AppException(AppError.Validation("Ingresa un correo electrónico válido."))
            )
        }
        return authRepository.requestPasswordReset(normalizedEmail)
    }
}
