package co.edu.unicauca.urbanguard.domain.usecase.guards

import co.edu.unicauca.urbanguard.domain.repository.GuardRepository
import javax.inject.Inject

class SetGuardMembershipActiveUseCase @Inject constructor(
    private val repository: GuardRepository
) {
    suspend operator fun invoke(
        complexId: String,
        membershipId: String,
        active: Boolean
    ): Result<Unit> = repository.setGuardActive(complexId, membershipId, active)
}
