package co.edu.unicauca.urbanguard.domain.usecase.residential

import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import javax.inject.Inject

class SetOwnerTenantActiveUseCase @Inject constructor(
    private val repository: ResidentialRepository
) {
    suspend operator fun invoke(
        complexId: String,
        unitId: String,
        residentId: String,
        active: Boolean
    ): Result<Unit> = repository.setOwnerTenantActive(complexId, unitId, residentId, active)
}
