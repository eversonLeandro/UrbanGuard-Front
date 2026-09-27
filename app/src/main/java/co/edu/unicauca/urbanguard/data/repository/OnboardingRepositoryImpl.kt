package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.ActivationRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.OnboardingApi
import co.edu.unicauca.urbanguard.domain.model.OnboardingStatus
import co.edu.unicauca.urbanguard.domain.repository.OnboardingRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val onboardingApi: OnboardingApi
) : OnboardingRepository {

    override suspend fun getOnboardingStatus(): Result<OnboardingStatus> = apiCall {
        onboardingApi.getStatus().toDomain()
    }

    /**
     * No usa [apiCall] porque el bloque ya devuelve un `Result` para el caso de
     * respuesta no exitosa, y envolverlo produciria un `Result` anidado. El catch
     * amplio conserva la justificacion de [apiCall].
     */
    @Suppress("TooGenericExceptionCaught")
    override suspend fun validateActivationCode(code: String): Result<Unit> = try {
        val response = onboardingApi.verifyActivation(
            ActivationRequestDto(code = code)
        )
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(NetworkErrorMapper.toAppException(retrofit2.HttpException(response)))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(NetworkErrorMapper.toAppException(e))
    }
}
