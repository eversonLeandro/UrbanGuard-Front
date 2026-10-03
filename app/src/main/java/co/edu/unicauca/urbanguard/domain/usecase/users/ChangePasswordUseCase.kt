package co.edu.unicauca.urbanguard.domain.usecase.users

import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Solicita al backend un ticket de cambio de contraseña de Auth0 para el
 * usuario autenticado en curso.
 *
 * El backend aplica todas las validaciones de seguridad (usuario activo, cuenta
 * nativa, frescura del auth_time). La URL devuelta es una credencial temporal
 * que el llamador debe abrir en un Custom Tab y descartar de memoria.
 */
class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<String> = userRepository.createPasswordChangeTicket()
}
