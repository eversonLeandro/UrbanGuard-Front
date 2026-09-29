package co.edu.unicauca.urbanguard.domain.usecase.visitors

import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import javax.inject.Inject

/**
 * Obtiene el historial de pases QR del conjunto activo para la pantalla de
 * visitantes. Delega tal cual en el repositorio; el filtrado por conjunto ya
 * lo hace el backend a partir del token de la sesión.
 */
class GetVisitorAuthorizationsUseCase @Inject constructor(
    private val repository: VisitorRepository
) {
    suspend operator fun invoke(): Result<List<VisitorAuthorization>> {
        return repository.getVisitorAuthorizations()
    }
}
