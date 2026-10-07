package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrItemDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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
    // ENDPOINT PROVISIONAL: Historial de pases QR del conjunto
    // GET /api/visitors/authorizations
    // Éxito esperado: 200 OK con
    // [ { id, visitorName, guestCount, qrCode, date } ]
    // Errores: 401/403/500
    // NOTA: el endpoint aún NO existe en el backend; mientras
    // VisitorConstants.VISITORS_BACKEND_ENABLED esté en false el repositorio
    // devuelve una lista vacía sin invocar este método, de modo que la
    // pantalla de visitors muestra su estado vacío y no un error de red.
    // =========================================================================
    @GET("api/visitors/authorizations")
    suspend fun getVisitorAuthorizations(): List<VisitorQrItemDto>
}
