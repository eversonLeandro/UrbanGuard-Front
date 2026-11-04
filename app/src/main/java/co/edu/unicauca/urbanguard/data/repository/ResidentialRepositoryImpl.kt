package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.ResidentialApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import javax.inject.Inject

class ResidentialRepositoryImpl @Inject constructor(
    private val api: ResidentialApi
) : ResidentialRepository {

    override suspend fun getCensus(complexId: String): Result<List<ResidentialUnit>> {
        if (complexId.isBlank()) {
            return Result.failure(
                AppException(AppError.Validation("No hay un conjunto activo seleccionado."))
            )
        }
        return apiCall { api.getCensus(complexId).map { it.toDomain() } }
    }
}
