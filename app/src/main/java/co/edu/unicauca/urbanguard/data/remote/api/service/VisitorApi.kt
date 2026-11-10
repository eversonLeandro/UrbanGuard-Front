package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorHistoryPageDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorInvitationCodeDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorInvitationListItemDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorValidationRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorValidationResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VisitorApi {

    // =========================================================================
    // Crear pase QR de visitante
    // POST api/complexes/{complexId}/visitor-invitations
    // Body: { visitorName, guestCount, visitDate }  (parseo estricto, sin extras)
    // Éxito: 201 Created con { invitationId, code, visitDate }
    // Errores: 400/401/403/404/429/5xx
    // `code` es el contenido del QR. La llamada se hace solo cuando
    // VisitorConstants.VISITORS_BACKEND_ENABLED está en true.
    // =========================================================================
    @POST("api/complexes/{complexId}/visitor-invitations")
    suspend fun generateVisitorQr(
        @Path("complexId") complexId: String,
        @Body request: VisitorQrRequestDto
    ): VisitorQrResponseDto

    // =========================================================================
    // Historial de invitaciones del residente
    // GET api/complexes/{complexId}/visitor-invitations
    // Éxito: 200 OK con [{ invitationId, visitorName, guestCount, visitDate,
    // status, createdAt, usedAt }]. Solo trae las creadas por el residente.
    // Errores: 401/403/404/429/5xx
    // =========================================================================
    @GET("api/complexes/{complexId}/visitor-invitations")
    suspend fun getInvitations(
        @Path("complexId") complexId: String
    ): List<VisitorInvitationListItemDto>

    @GET("api/complexes/{complexId}/visitor-invitations/history")
    suspend fun getVisitorHistory(
        @Path("complexId") complexId: String,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): VisitorHistoryPageDto

    // =========================================================================
    // Recuperar el código de una invitación activa o programada (recompartir)
    // GET api/complexes/{complexId}/visitor-invitations/{invitationId}/code
    // Éxito: 200 OK con { code }. Si ya no está disponible: 410.
    // =========================================================================
    @GET("api/complexes/{complexId}/visitor-invitations/{invitationId}/code")
    suspend fun getInvitationCode(
        @Path("complexId") complexId: String,
        @Path("invitationId") invitationId: String
    ): VisitorInvitationCodeDto

    // =========================================================================
    // Validar pase QR de visitante (flujo guardia)
    // POST api/complexes/{complexId}/visitor-invitations/validation
    // Body: { code }  (22 caracteres base64url, parseo estricto)
    // Éxito: 200 OK con { invitationId, visitorName, guestCount, visitDate, status, usedAt }
    // Errores con { reason, message }: 404 INVALID_TOKEN, 409 USED/NOT_YET_VALID,
    // 410 EXPIRED, 400 formato, 401/403 auth, 429 rate limit, 5xx.
    // Se devuelve Response<> para poder leer el `reason` del error.
    // =========================================================================
    @POST("api/complexes/{complexId}/visitor-invitations/validation")
    suspend fun validateVisitorQr(
        @Path("complexId") complexId: String,
        @Body request: VisitorValidationRequestDto
    ): Response<VisitorValidationResponseDto>
}
