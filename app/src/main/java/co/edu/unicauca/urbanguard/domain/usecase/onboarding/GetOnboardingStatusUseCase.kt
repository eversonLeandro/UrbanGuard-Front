package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.domain.model.OnboardingStatus
import co.edu.unicauca.urbanguard.domain.repository.OnboardingRepository
import javax.inject.Inject

class GetOnboardingStatusUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(): Result<OnboardingStatus> {
        return repository.getOnboardingStatus()
    }
}
