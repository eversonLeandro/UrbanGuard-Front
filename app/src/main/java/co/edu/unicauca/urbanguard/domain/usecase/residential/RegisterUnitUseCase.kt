package co.edu.unicauca.urbanguard.domain.usecase.residential

import co.edu.unicauca.urbanguard.domain.model.RegisteredResidentialUnit
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import javax.inject.Inject

class RegisterUnitUseCase @Inject constructor(
    private val repository: ResidentialRepository
) {
    suspend operator fun invoke(complexId: String): Result<RegisteredResidentialUnit> =
        repository.registerUnit(complexId)
}
