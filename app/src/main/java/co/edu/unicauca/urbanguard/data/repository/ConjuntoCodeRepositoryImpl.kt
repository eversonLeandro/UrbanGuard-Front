package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toCreateComplexRequestDto
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.OnboardingApi
import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode
import co.edu.unicauca.urbanguard.domain.model.CreatedComplex
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoCodeRepository
import javax.inject.Inject

class ConjuntoCodeRepositoryImpl @Inject constructor(
    private val onboardingApi: OnboardingApi
) : ConjuntoCodeRepository {

    override suspend fun getCode(conjuntoId: String?): Result<ConjuntoCode> = apiCall {
        onboardingApi.getCode(conjuntoId).toDomain()
    }

    override suspend fun generateCode(conjuntoId: String): Result<ConjuntoCode> = apiCall {
        onboardingApi.generateCode(conjuntoId).toDomain()
    }

    override suspend fun regenerateCode(conjuntoId: String): Result<ConjuntoCode> = apiCall {
        onboardingApi.regenerateCode(conjuntoId).toDomain()
    }

    override suspend fun createConjunto(
        activationCode: String,
        nombre: String,
        direccion: String,
        ciudad: String,
        totalUnidades: Int
    ): Result<CreatedComplex> = apiCall {
        val request = toCreateComplexRequestDto(
            activationCode = activationCode,
            name = nombre,
            address = direccion,
            city = ciudad,
            numberOfUnits = totalUnidades
        )
        onboardingApi.createConjunto(request).toDomain()
    }
}
