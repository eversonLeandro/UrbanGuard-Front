package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.repository.PatrolRouteRepository
import javax.inject.Inject

/** Crea (id en blanco) o actualiza una ruta con sus horarios. */
class SavePatrolRouteUseCase @Inject constructor(
    private val repository: PatrolRouteRepository
) {
    suspend operator fun invoke(route: PatrolRoute): Result<PatrolRoute> {
        return repository.saveRoute(route)
    }
}
