package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
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
            Result.failure(Exception(NetworkErrorMapper.map(e)))
        }
    }

    override suspend fun validateActivationCode(codigoActivacion: String): Result<String> {
        // DESCOMENTAR CUANDO ESTÉ EL BACKEND
        // Endpoint real: POST onboarding/activation-code/validate
        // return try {
        //     val response = onboardingApi.validateActivationCode(
        //         ActivationValidationRequestDto(codigoActivacion = codigoActivacion)
        //     )
        //     Result.success(response.conjuntoId)
        // } catch (e: Exception) {
        //     Result.failure(Exception(NetworkErrorMapper.map(e)))
        // }
        // Mientras el flag OnboardingBypass.ENABLED esté activo, los UseCases
        // no llegan aquí. Este método solo se ejecutará al descomentar todo.
        return Result.failure(Exception("Endpoint de validación de activación no implementado todavía"))
    }
}