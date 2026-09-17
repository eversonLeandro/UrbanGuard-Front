package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toRegisterConjuntoRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.OnboardingApi
import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode
import co.edu.unicauca.urbanguard.domain.model.CreatedComplex
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

    override suspend fun createConjunto(
        activationCode: String,
        nombre: String,
        direccion: String,
        ciudad: String
    ): Result<CreatedComplex> {
        // DESCOMENTAR CUANDO ESTÉ EL BACKEND
        // Endpoint real: POST /api/complexes → 201 Created.
        // return try {
        //     val response = onboardingApi.createConjunto(
        //         request = toRegisterConjuntoRequestDto(
        //             activationCode = activationCode,
        //             name = nombre,
        //             address = direccion,
        //             city = ciudad
        //         )
        //     )
        //     Result.success(response.toDomain())
        // } catch (e: Exception) {
        //     Result.failure(Exception(NetworkErrorMapper.map(e)))
        // }
        return Result.failure(Exception("Endpoint de creación de conjunto no implementado todavía"))
    }
}