package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.ComplexSummaryDto
import retrofit2.http.GET

/**
 * Interfaz Retrofit para el Community Service (vía API Gateway).
 *
 * La BASE_URL ya apunta al gateway (NetworkConfig.BASE_URL), por lo que el path
 * "api/complexes" coincide directamente con la ruta que el gateway enruta al
 * community-service (application.yaml:146-177, ruta community-service-complex-listing).
 *
 * El token JWT se inyecta automáticamente por [AuthInterceptor].
 */
interface CommunityApi {

    /**
     * Devuelve la lista de conjuntos residenciales a los que el usuario
     * autenticado tiene membresía (ADMIN, RESIDENT o GUARD).
     *
     * El Community Service resuelve el usuario internamente llamando a
     * Identity GET /api/users/me con el JWT. El frontend no necesita
     * llamar a Identity directamente.
     *
     * Respuesta: List<ComplexSummaryDto>
     * Posibles errores: 401 (token inválido/expirado), 403 (sin permiso),
     * 502/503/504 (gateway/servicio no disponible).
     */
    @GET("api/complexes")
    suspend fun getMyComplexes(): List<ComplexSummaryDto>
}
