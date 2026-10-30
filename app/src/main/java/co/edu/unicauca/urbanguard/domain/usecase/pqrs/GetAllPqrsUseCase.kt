package co.edu.unicauca.urbanguard.domain.usecase.pqrs

import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsSummary
import co.edu.unicauca.urbanguard.domain.repository.PqrsRepository
import javax.inject.Inject

class GetAllPqrsUseCase @Inject constructor(private val repository: PqrsRepository) {
    suspend operator fun invoke(
        complexId: String,
        status: PqrsStatus? = null,
        type: String? = null
    ): Result<List<PqrsSummary>> = repository.getPqrs(complexId, status, type)
}

