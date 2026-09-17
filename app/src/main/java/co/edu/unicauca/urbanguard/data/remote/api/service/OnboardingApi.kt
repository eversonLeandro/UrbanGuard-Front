package co.edu.unicauca.urbanguard.data.remote.api.service
import co.edu.unicauca.urbanguard.data.remote.api.dto.ActivationRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ConjuntoCodeResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateComplexResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.OnboardingStatusDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterConjuntoRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OnboardingApi {
    // Consulta si el usuario Admin ya tiene un conjunto vinculado (UG-45)
    @GET("onboarding/status")
    suspend fun getStatus(): OnboardingStatusDto

    // Obtiene el código actual del conjunto (si ya fue generado)
    @GET("onboarding/code")
    suspend fun getCode(@Query("conjuntoId") conjuntoId: String? = null): ConjuntoCodeResponseDto

    // Primera generación del código para un conjunto específico
    @POST("onboarding/code/generate")
    suspend fun generateCode(@Query("conjuntoId") conjuntoId: String? = null): ConjuntoCodeResponseDto

    // Regenera el código invalidando el anterior
    @POST("onboarding/code/regenerate")
    suspend fun regenerateCode(@Query("conjuntoId") conjuntoId: String? = null): ConjuntoCodeResponseDto

    // =========================================================================
    // ENDPOINT NUEVO SOLICITADO AL BACKEND (NO EXISTE AÚN):
    //   POST /api/activations/verifications
    //   Body:  { "code": "ACT-XXXX-YYYY" }
    //   Éxito: 204 No Content (sin body de respuesta)
    //   Errores: 400/401/403/422/500 con DTO ApiError.
    // El backend, al validar el código, CREA el conjunto. NO retorna ningún
    // cuerpo: se usa Response<Unit> para no intentar deserializar el 204.
    // =========================================================================
    // DESCOMENTAR CUANDO ESTÉ EL BACKEND
    // @POST("activations/verifications")
    // suspend fun verifyActivation(@Body request: ActivationRequestDto): retrofit2.Response<Unit>

    // Crea un conjunto (complex) tras validar el código de activación.
    // POST /api/complexes
    //   Body:  { activationCode, name, address, city }
    //   Éxito: 201 Created con { complexId, membershipId, roles }
    //   Errores: 400/401/403/422/405/502/503/504 con DTO ApiError.
    // Ya NO se usa PUT por ID ni el path conjuntoId; totalUnidades queda fuera
    // del body (el backend lo rechaza con 400 por campo desconocido).
    // DESCOMENTAR CUANDO ESTÉ EL BACKEND
    // @POST("complexes")
    // suspend fun createConjunto(
    //     @Body request: RegisterConjuntoRequestDto
    // ): CreateComplexResponseDto
}