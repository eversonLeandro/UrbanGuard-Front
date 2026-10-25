package co.edu.unicauca.urbanguard.domain.usecase.incidents

import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.repository.IncidentRepository
import javax.inject.Inject

/** Novedades del conjunto, más recientes primero. */
class GetIncidentsUseCase @Inject constructor(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(complexId: String, isAdmin: Boolean): Result<List<SecurityIncident>> {
        return repository.getIncidents(complexId, isAdmin)
    }
}
