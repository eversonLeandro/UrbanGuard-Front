package co.edu.unicauca.urbanguard.domain.usecase.users

import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Reactiva una cuenta desactivada (restaura el acceso bloqueado en Auth0 y
 * el estado `activo=true` local) y devuelve su estado actualizado.
 */
class ReactivateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return repository.reactivateUser(userId)
    }
}
