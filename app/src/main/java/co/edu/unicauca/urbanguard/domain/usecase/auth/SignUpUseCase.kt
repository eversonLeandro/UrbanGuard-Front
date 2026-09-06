package co.edu.unicauca.urbanguard.domain.usecase.auth

import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String = ""
    ): Result<UserSession> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Ingresa un correo electrónico válido"))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 8 caracteres"))
        }
        return authRepository.signUp(email.trim(), password, name.trim())
    }
}
