package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.OnboardingApi
import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoCodeRepository
import javax.inject.Inject

class ConjuntoCodeRepositoryImpl @Inject constructor(
    private val onboardingApi: OnboardingApi
) : ConjuntoCodeRepository {

    override suspend fun getCode(): Result<ConjuntoCode> {
        return try {
            val response = onboardingApi.getCode()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorMapper.map(e)))
        }
    }

    override suspend fun generateCode(): Result<ConjuntoCode> {
        return try {
            val response = onboardingApi.generateCode()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorMapper.map(e)))
        }
    }

    override suspend fun regenerateCode(): Result<ConjuntoCode> {
        return try {
            val response = onboardingApi.regenerateCode()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorMapper.map(e)))
        }
    }
}