package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrItemDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorQrResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface VisitorApi {

    // =========================================================================
    // ENDPOINT PROVISIONAL: Crear pase QR de visitante
    // POST /api/visitors/authorizations
    // Body: { visitorName, guestCount, date, complexId }
    // Éxito esperado: 201 Created con { qrCode, date }
    // Errores: 400/401/403/404/422/500
    // NOTA: el endpoint aún NO existe en el backend; mientras
    // VisitorConstants.VISITORS_BACKEND_ENABLED esté en false el repositorio
    // devuelve un pase simulado sin invocar este método.
    // =========================================================================
    @POST("api/visitors/authorizations")
    suspend fun generateVisitorQr(@Body request: VisitorQrRequestDto): VisitorQrResponseDto

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
