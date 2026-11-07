package co.edu.unicauca.urbanguard.domain.usecase.users

import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Desactiva la cuenta (revoca el acceso sin borrar el historial, HU-006).
 *
 * La operación definitiva vive en el backend (Auth0 bloqueado primero, fila
 * local `activo=false` después); aquí solo se transporta el `sub` de Auth0.
 * La autodesactivación se bloquea a niveles anteriores (UI sin acción y 409
 * del servidor); este caso de uso no agrega otra capa.
 */
class DeactivateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return repository.deactivateUser(userId)
    }
}
