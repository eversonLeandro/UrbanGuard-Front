package co.edu.unicauca.urbanguard.domain.usecase.users

import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Listado administrativo de cuentas por estado (HU-006).
 *
 * [estado] va genérico a propósito: las constantes del catálogo viven en
 * [UserRepository] y el caso de uso no decide la semántica del filtro (activos,
 * inactivos o todos), solo transporta la consulta.
 */
class ListUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(estado: String = UserRepository.ESTADO_ACTIVOS): Result<List<User>> {
        return repository.listUsers(estado)
    }
}
