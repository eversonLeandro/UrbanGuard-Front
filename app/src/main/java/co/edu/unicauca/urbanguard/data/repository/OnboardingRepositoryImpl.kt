package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.ActivationRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.OnboardingApi
import co.edu.unicauca.urbanguard.domain.model.OnboardingStatus
import co.edu.unicauca.urbanguard.domain.repository.OnboardingRepository
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val onboardingApi: OnboardingApi
) : OnboardingRepository {

    override suspend fun getOnboardingStatus(): Result<OnboardingStatus> {
        return try {
            val response = onboardingApi.getStatus()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(NetworkErrorMapper.toAppException(e))
        }
    }

    override suspend fun validateActivationCode(code: String): Result<Unit> {
        return try {
            val response = onboardingApi.verifyActivation(
                ActivationRequestDto(code = code)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(NetworkErrorMapper.toAppException(retrofit2.HttpException(response)))
            }
        } catch (e: Exception) {
            Result.failure(NetworkErrorMapper.toAppException(e))
        }
    }
}