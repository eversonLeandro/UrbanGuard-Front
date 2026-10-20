package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolGuardSummaryDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftDetailDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftSummaryDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Historial de patrullaje por turno (HU-034 / HU-032), vía API Gateway.
 * El gateway enruta estas rutas al `security-service`.
 *
 * La BASE_URL ya apunta al gateway y el `AuthInterceptor` inyecta el JWT; el
 * rol ADMIN y el `complexId` viajan en la ruta, nunca la identidad del usuario.
 *
 * `from` y `to` siempre se envían explícitos. `guardId` y `onlyIncomplete` son
 * opcionales: Retrofit omite los `@Query` nulos, así que el frontend solo los
 * manda cuando tienen valor (para `onlyIncomplete`, solo cuando es `true`).
 */
interface PatrolShiftApi {

    @GET(PatrolApiPaths.SHIFTS)
    suspend fun getShifts(
        @Path("complexId") complexId: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("guardId") guardId: String? = null,
        @Query("onlyIncomplete") onlyIncomplete: Boolean? = null
    ): List<PatrolShiftSummaryDto>

    @GET(PatrolApiPaths.SHIFT_DETAIL)
    suspend fun getShiftDetail(
        @Path("complexId") complexId: String,
        @Query("shiftType") shiftType: String,
        @Query("date") date: String
    ): PatrolShiftDetailDto

    @GET(PatrolApiPaths.GUARD_SUMMARY)
    suspend fun getGuardSummary(
        @Path("complexId") complexId: String,
        @Path("guardId") guardId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): PatrolGuardSummaryDto
}
