package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.repository.PatrolRouteRepository
import javax.inject.Inject

/** Rutas de patrullaje del conjunto con sus horarios. */
class GetPatrolRoutesUseCase @Inject constructor(
    private val repository: PatrolRouteRepository
) {
    suspend operator fun invoke(complexId: String): Result<List<PatrolRoute>> {
        return repository.getRoutes(complexId)
    }
}
