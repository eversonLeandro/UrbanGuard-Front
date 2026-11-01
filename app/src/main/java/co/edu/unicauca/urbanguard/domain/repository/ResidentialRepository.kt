package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit

interface ResidentialRepository {
    suspend fun getCensus(complexId: String): Result<List<ResidentialUnit>>
}
