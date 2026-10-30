package co.edu.unicauca.urbanguard.domain.usecase.pqrs

import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDetail
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDraft
import co.edu.unicauca.urbanguard.domain.repository.ResidentPqrsRepository
import javax.inject.Inject

class CreatePqrsUseCase @Inject constructor(private val repository: ResidentPqrsRepository) {
    suspend operator fun invoke(
        complexId: String,
        key: String,
        draft: ResidentPqrsDraft
    ): Result<ResidentPqrsDetail> = repository.create(complexId, key, draft)
}

