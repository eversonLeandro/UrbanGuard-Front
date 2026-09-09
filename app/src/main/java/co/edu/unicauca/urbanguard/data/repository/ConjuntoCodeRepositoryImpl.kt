package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterConjuntoRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.OnboardingApi
import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoCodeRepository
import javax.inject.Inject

class ConjuntoCodeRepositoryImpl @Inject constructor(
    private val onboardingApi: OnboardingApi
) : ConjuntoCodeRepository {

    override suspend fun getCode(conjuntoId: String?): Result<ConjuntoCode> {
        return try {
            val response = onboardingApi.getCode(conjuntoId)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorMapper.map(e)))
        }
    }

    override suspend fun generateCode(conjuntoId: String?): Result<ConjuntoCode> {
        return try {
            val response = onboardingApi.generateCode(conjuntoId)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorMapper.map(e)))
        }
    }

    override suspend fun regenerateCode(conjuntoId: String?): Result<ConjuntoCode> {
        return try {
            val response = onboardingApi.regenerateCode(conjuntoId)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorMapper.map(e)))
        }
    }

    override suspend fun updateConjunto(
        conjuntoId: String,
        nombre: String,
        direccion: String,
        ciudad: String,
        totalUnidades: Int
    ): Result<Unit> {
        return try {
            onboardingApi.updateConjunto(
                conjuntoId = conjuntoId,
                request = RegisterConjuntoRequestDto(
                    nombre = nombre,
                    direccion = direccion,
                    ciudad = ciudad,
                    totalUnidades = totalUnidades
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorMapper.map(e)))
        }
    }
}
