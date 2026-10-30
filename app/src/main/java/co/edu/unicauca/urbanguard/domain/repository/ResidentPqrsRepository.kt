package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDetail
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDraft
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsSummary

interface ResidentPqrsRepository {
    suspend fun create(
        complexId: String,
        idempotencyKey: String,
        draft: ResidentPqrsDraft
    ): Result<ResidentPqrsDetail>

    suspend fun getMine(
        complexId: String,
        status: PqrsStatus? = null,
        type: PqrsType? = null
    ): Result<List<ResidentPqrsSummary>>

    suspend fun getOwnDetail(complexId: String, pqrsId: String): Result<ResidentPqrsDetail>
}
