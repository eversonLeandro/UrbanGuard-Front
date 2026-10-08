package co.edu.unicauca.urbanguard.domain.usecase.visitors

import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import javax.inject.Inject

/**
 * Recupera el código QR de una invitación activa o programada para volver a
 * compartirla. Delega tal cual en el repositorio.
 */
class GetInvitationCodeUseCase @Inject constructor(
    private val repository: VisitorRepository
) {
    suspend operator fun invoke(
        complexId: String,
        invitationId: String
    ): Result<String> {
        return repository.getInvitationCode(complexId, invitationId)
    }
}
