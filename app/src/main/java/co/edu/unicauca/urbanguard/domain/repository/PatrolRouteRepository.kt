package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.PatrolRoute

/**
 * Rutas de patrullaje del conjunto (UG-40, segunda parte).
 *
 * Repositorio propio para no superar el máximo de funciones de detekt en
 * [PatrolRepository]. Misma estrategia offline-first: hoy resuelve local,
 * mañana sincroniza con el backend sin cambiar la firma.
 */
interface PatrolRouteRepository {

    /** Rutas del conjunto con sus horarios. */
    suspend fun getRoutes(complexId: String): Result<List<PatrolRoute>>

    /** Crea (id en blanco) o actualiza una ruta con sus horarios. */
    suspend fun saveRoute(route: PatrolRoute): Result<PatrolRoute>

    /** Elimina una ruta (los puntos de control no se tocan). */
    suspend fun deleteRoute(complexId: String, routeId: String): Result<Unit>
}
