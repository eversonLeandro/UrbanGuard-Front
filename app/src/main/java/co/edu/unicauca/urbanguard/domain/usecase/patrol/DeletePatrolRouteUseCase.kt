package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.repository.PatrolRouteRepository
import javax.inject.Inject

/** Elimina una ruta (los puntos de control no se tocan). */
class DeletePatrolRouteUseCase @Inject constructor(
    private val repository: PatrolRouteRepository
) {
    suspend operator fun invoke(complexId: String, routeId: String): Result<Unit> {
        return repository.deleteRoute(complexId, routeId)
    }
}
