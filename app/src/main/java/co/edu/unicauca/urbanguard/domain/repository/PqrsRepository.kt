package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.PqrsDetail
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsSummary

interface PqrsRepository {
    suspend fun getPqrs(
        complexId: String,
        status: PqrsStatus? = null,
        type: String? = null
    ): Result<List<PqrsSummary>>

    suspend fun getPqrsDetail(complexId: String, pqrsId: String): Result<PqrsDetail>

    suspend fun updateStatus(
        complexId: String,
        pqrsId: String,
        status: PqrsStatus,
        response: String
    ): Result<PqrsDetail>
}

