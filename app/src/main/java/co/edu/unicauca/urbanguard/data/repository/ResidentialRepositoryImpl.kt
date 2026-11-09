package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toRequestDto
import co.edu.unicauca.urbanguard.data.mapper.toUpdatedIdentifier
import co.edu.unicauca.urbanguard.data.remote.api.dto.UpdateUnitRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.ResidentialApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.OwnerTenant
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.RegisteredResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import javax.inject.Inject

class ResidentialRepositoryImpl @Inject constructor(
    private val api: ResidentialApi
) : ResidentialRepository {

    override suspend fun getCensus(complexId: String): Result<List<ResidentialUnit>> =
        getCensus(complexId, "activos")

    override suspend fun getCensus(complexId: String, status: String): Result<List<ResidentialUnit>> {
        if (complexId.isBlank()) {
            return noActiveComplexFailure()
        }
        return apiCall { api.getCensus(complexId, status).map { it.toDomain() } }
    }

    override suspend fun setOwnerTenantActive(
        complexId: String,
        unitId: String,
        residentId: String,
        active: Boolean
    ): Result<Unit> {
        if (complexId.isBlank()) return noActiveComplexFailure()
        if (unitId.isBlank() || residentId.isBlank()) return invalidUnitFailure()
        return apiCall {
            if (active) {
                api.reactivateOwnerTenant(complexId, unitId, residentId)
            } else {
                api.deactivateOwnerTenant(complexId, unitId, residentId)
            }
        }
    }

    override suspend fun registerUnit(complexId: String): Result<RegisteredResidentialUnit> {
        if (complexId.isBlank()) {
            return noActiveComplexFailure()
        }
        return apiCall { api.createUnit(complexId).toDomain() }
    }

    override suspend fun updateUnit(
        complexId: String,
        unitId: String,
        identifier: String
    ): Result<String> {
        if (complexId.isBlank()) {
            return noActiveComplexFailure()
        }
        if (unitId.isBlank()) {
            return invalidUnitFailure()
        }
        return apiCall {
            api.updateUnit(
                complexId = complexId,
                unitId = unitId,
                request = UpdateUnitRequestDto(identifier)
            ).toUpdatedIdentifier()
        }
    }

    override suspend fun registerOwnerTenant(
        complexId: String,
        unitId: String,
        draft: OwnerTenantDraft
    ): Result<OwnerTenant> {
        if (complexId.isBlank()) {
            return noActiveComplexFailure()
        }
        if (unitId.isBlank()) {
            return invalidUnitFailure()
        }
        return apiCall {
            api.createOwnerTenant(complexId, unitId, draft.toRequestDto()).toDomain()
        }
    }

    private fun <T> noActiveComplexFailure(): Result<T> = Result.failure(
        AppException(AppError.Validation(NO_ACTIVE_COMPLEX_MESSAGE))
    )

    private fun <T> invalidUnitFailure(): Result<T> = Result.failure(
        AppException(AppError.Validation(NO_UNIT_MESSAGE))
    )

    private companion object {
        const val NO_ACTIVE_COMPLEX_MESSAGE = "No hay un conjunto activo seleccionado."
        const val NO_UNIT_MESSAGE = "No se seleccionó un apartamento."
    }
}
