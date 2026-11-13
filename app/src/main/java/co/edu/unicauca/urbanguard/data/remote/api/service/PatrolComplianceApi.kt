package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundComplianceDetailDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Detalle de cumplimiento de una ronda (nivel 3 del historial), vía API Gateway.
 * El gateway enruta `patrol-compliance` al `security-service`; la BASE_URL ya
 * apunta al gateway y el `AuthInterceptor` inyecta el JWT de admin.
 */
interface PatrolComplianceApi {

    @GET(PatrolApiPaths.COMPLIANCE_DETAIL)
    suspend fun getRoundDetail(
        @Path("complexId") complexId: String,
        @Path("occurrenceId") occurrenceId: String
    ): PatrolRoundComplianceDetailDto
}
