package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.LinkRequest

// Ducto de lectura y revisión de solicitudes de vinculación (UG-49).
interface LinkRequestRepository {
    suspend fun getRequests(): Result<List<LinkRequest>>
    suspend fun getRequestById(id: String): Result<LinkRequest>
    suspend fun reviewRequest(id: String, approve: Boolean): Result<LinkRequest>
}
