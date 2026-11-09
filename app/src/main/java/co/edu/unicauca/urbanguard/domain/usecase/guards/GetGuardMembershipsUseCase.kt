package co.edu.unicauca.urbanguard.domain.usecase.guards

import co.edu.unicauca.urbanguard.domain.model.GuardMembership
import co.edu.unicauca.urbanguard.domain.repository.GuardRepository
import javax.inject.Inject

class GetGuardMembershipsUseCase @Inject constructor(
    private val repository: GuardRepository
) {
    suspend operator fun invoke(complexId: String, status: String): Result<List<GuardMembership>> =
        repository.getGuards(complexId, status)
}
