package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.OwnerTenant
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.RegisteredResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit

interface ResidentialRepository {
    suspend fun getCensus(complexId: String): Result<List<ResidentialUnit>>

    suspend fun getCensus(complexId: String, status: String): Result<List<ResidentialUnit>>

    suspend fun setOwnerTenantActive(
        complexId: String,
        unitId: String,
        residentId: String,
        active: Boolean
    ): Result<Unit>

    suspend fun registerUnit(complexId: String): Result<RegisteredResidentialUnit>

    suspend fun updateUnit(
        complexId: String,
        unitId: String,
        identifier: String
    ): Result<String>

    suspend fun registerOwnerTenant(
        complexId: String,
        unitId: String,
        draft: OwnerTenantDraft
    ): Result<OwnerTenant>
}
