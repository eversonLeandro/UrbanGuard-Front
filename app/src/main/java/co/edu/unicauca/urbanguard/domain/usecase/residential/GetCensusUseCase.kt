package co.edu.unicauca.urbanguard.domain.usecase.residential

import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import javax.inject.Inject

class GetCensusUseCase @Inject constructor(
    private val repository: ResidentialRepository
) {
    suspend operator fun invoke(complexId: String): Result<List<ResidentialUnit>> =
        repository.getCensus(complexId)

    suspend operator fun invoke(
        complexId: String,
        status: String
    ): Result<List<ResidentialUnit>> =
        repository.getCensus(complexId, status)
}
