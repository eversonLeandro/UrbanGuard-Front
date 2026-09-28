package co.edu.unicauca.urbanguard.domain.usecase.guards

import co.edu.unicauca.urbanguard.domain.model.CreatedGuard
import co.edu.unicauca.urbanguard.domain.repository.GuardRepository
import javax.inject.Inject

class RegisterGuardUseCase @Inject constructor(
    private val repository: GuardRepository
) {
    suspend operator fun invoke(
        complexId: String,
        email: String,
        nombres: String,
        apellidos: String,
        telefono: String
    ): Result<CreatedGuard> {
        return repository.registerGuard(complexId, email, nombres, apellidos, telefono)
    }
}
