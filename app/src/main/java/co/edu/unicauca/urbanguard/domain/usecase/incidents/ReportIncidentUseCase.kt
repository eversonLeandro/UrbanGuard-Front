package co.edu.unicauca.urbanguard.domain.usecase.incidents

import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.repository.IncidentRepository
import javax.inject.Inject

/** Crea una novedad en el backend. */
class ReportIncidentUseCase @Inject constructor(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(
        complexId: String,
        category: IncidentCategory,
        description: String,
        photoPath: String?,
        pointId: String?,
        executionId: String?,
        idempotencyKey: String
    ): Result<SecurityIncident> {
        return repository.reportIncident(
            complexId = complexId,
            category = category,
            description = description,
            photoPath = photoPath,
            pointId = pointId,
            executionId = executionId,
            idempotencyKey = idempotencyKey
        )
    }
}
