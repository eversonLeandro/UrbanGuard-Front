package co.edu.unicauca.urbanguard.domain.usecase.auth

import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Ingresa un correo válido"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("La contraseña es obligatoria"))
        }
        return authRepository.login(email.trim(), password)
    }
}

