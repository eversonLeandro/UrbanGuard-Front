package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.ActivationRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoCodeResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateComplexRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateComplexResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.OnboardingStatusDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OnboardingApi {
    // Consulta si el usuario Admin ya tiene un conjunto vinculado (UG-45)
    @GET("onboarding/status")
    suspend fun getStatus(): OnboardingStatusDto

    // Obtiene el código actual del conjunto (si ya fue generado)
    @GET("onboarding/code")
    suspend fun getCode(@Query("conjuntoId") conjuntoId: String? = null): ConjuntoCodeResponseDto

    // Primera generación del código de vinculación para un conjunto
    // POST /api/complexes/{complexId}/join-codes
    @POST("api/complexes/{complexId}/join-codes")
    suspend fun generateCode(@Path("complexId") complexId: String): ConjuntoCodeResponseDto

    // Regenera el código invalidando el anterior (mismo endpoint, el backend rota)
    // POST /api/complexes/{complexId}/join-codes
    @POST("api/complexes/{complexId}/join-codes")
    suspend fun regenerateCode(@Path("complexId") complexId: String): ConjuntoCodeResponseDto

    // =========================================================================
    // ENDPOINT: Validar código de activación
    // POST /api/activations/verifications
    // Body: { "code": "ACT-XXXX-YYYY" }
    // Éxito: 204 No Content
    // Errores: 400/401/403/422/500
    // =========================================================================
    @POST("api/activations/verifications")
    suspend fun verifyActivation(@Body request: ActivationRequestDto): retrofit2.Response<Unit>

    // =========================================================================
    // ENDPOINT: Crear conjunto (complex) tras validar código
    // POST /api/complexes
    // Body: { activationCode, name, address, city, numberOfUnits }
    // Éxito: 201 Created con { complexId, membershipId, role }
    // Errores: 400/401/403/422/405/502/503/504
    // Nota: numberOfUnits es requerido por el backend (@NotNull @Positive)
    // =========================================================================
    @POST("api/complexes")
    suspend fun createConjunto(@Body request: CreateComplexRequestDto): CreateComplexResponseDto
}
