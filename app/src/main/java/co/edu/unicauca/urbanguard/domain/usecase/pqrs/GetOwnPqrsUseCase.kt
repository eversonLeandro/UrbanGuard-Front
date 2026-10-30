package co.edu.unicauca.urbanguard.domain.usecase.pqrs

import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsSummary
import co.edu.unicauca.urbanguard.domain.repository.ResidentPqrsRepository
import javax.inject.Inject

class GetOwnPqrsUseCase @Inject constructor(private val repository: ResidentPqrsRepository) {
    suspend operator fun invoke(
        complexId: String,
        status: PqrsStatus? = null,
        type: PqrsType? = null
    ): Result<List<ResidentPqrsSummary>> = repository.getMine(complexId, status, type)
}

