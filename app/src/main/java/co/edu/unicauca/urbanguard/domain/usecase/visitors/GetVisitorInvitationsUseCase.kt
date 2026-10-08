package co.edu.unicauca.urbanguard.domain.usecase.visitors

import co.edu.unicauca.urbanguard.domain.model.VisitorInvitation
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import javax.inject.Inject

/**
 * Obtiene el historial de invitaciones del residente en el conjunto activo,
 * con su estado funcional. Delega tal cual en el repositorio.
 */
class GetVisitorInvitationsUseCase @Inject constructor(
    private val repository: VisitorRepository
) {
    suspend operator fun invoke(complexId: String): Result<List<VisitorInvitation>> {
        return repository.getVisitorInvitations(complexId)
    }
}
