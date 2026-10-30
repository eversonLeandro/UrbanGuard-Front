package co.edu.unicauca.urbanguard.domain.usecase.pqrs

import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDetail
import co.edu.unicauca.urbanguard.domain.repository.ResidentPqrsRepository
import javax.inject.Inject

class GetOwnPqrsDetailUseCase @Inject constructor(private val repository: ResidentPqrsRepository) {
    suspend operator fun invoke(complexId: String, pqrsId: String): Result<ResidentPqrsDetail> =
        repository.getOwnDetail(complexId, pqrsId)
}
