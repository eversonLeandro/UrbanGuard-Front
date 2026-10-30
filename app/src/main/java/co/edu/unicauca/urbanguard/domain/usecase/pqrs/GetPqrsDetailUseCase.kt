package co.edu.unicauca.urbanguard.domain.usecase.pqrs

import co.edu.unicauca.urbanguard.domain.model.PqrsDetail
import co.edu.unicauca.urbanguard.domain.repository.PqrsRepository
import javax.inject.Inject

class GetPqrsDetailUseCase @Inject constructor(private val repository: PqrsRepository) {
    suspend operator fun invoke(complexId: String, pqrsId: String): Result<PqrsDetail> =
        repository.getPqrsDetail(complexId, pqrsId)
}
