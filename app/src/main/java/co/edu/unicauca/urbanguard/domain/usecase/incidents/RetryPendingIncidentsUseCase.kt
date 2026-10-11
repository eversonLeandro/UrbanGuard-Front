package co.edu.unicauca.urbanguard.domain.usecase.incidents

import co.edu.unicauca.urbanguard.domain.repository.IncidentRepository
import javax.inject.Inject

/** Reintenta enviar las novedades pendientes del conjunto. */
class RetryPendingIncidentsUseCase @Inject constructor(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(complexId: String): Result<Unit> {
        return repository.retryPending(complexId)
    }
}
