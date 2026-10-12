package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Cuerpo para crear o actualizar un punto de control NFC.
 *
 * POST `api/complexes/{complexId}/patrol-points` y
 * PUT `api/complexes/{complexId}/patrol-points/{pointId}` (contrato propuesto
 * para UG-93; hoy el flag [PATROL_BACKEND_ENABLED] está apagado y no se usa).
 */
data class PatrolPointRequestDto(
    val name: String,
    val location: String,
    val nfcTagId: String,
    val order: Int,
    val isActive: Boolean
)

/**
 * Punto de control devuelto por el backend (contrato propuesto).
 *
 * GET `api/complexes/{complexId}/patrol-points` → 200 OK con la lista.
 */
data class PatrolPointResponseDto(
    val id: String,
    val name: String,
    val location: String,
    val nfcTagId: String,
    val order: Int,
    val isActive: Boolean
)

/**
 * Nuevo orden de la ruta tras reordenar.
 *
 * PUT `api/complexes/{complexId}/patrol-points/order`
 * Body: { "pointIds": ["uuid-1", "uuid-2", ...] } en el orden deseado.
 */
data class PatrolRouteOrderDto(
    val pointIds: List<String>
)

/**
 * Un horario de una ruta (contrato propuesto para UG-93).
 *
 * `time` en "HH:mm" (24h) y `days` con los días ISO 1-7 (lunes-domingo).
 */
data class PatrolScheduleDto(
    val time: String,
    val days: List<Int>
)

/**
 * Ruta de patrullaje (contrato propuesto para UG-93).
 *
 * GET `api/complexes/{complexId}/patrol-routes` → 200 OK con la lista.
 */
data class PatrolRouteDto(
    val id: String,
    val name: String,
    val pointIds: List<String>,
    val schedules: List<PatrolScheduleDto>,
    val isActive: Boolean
)

/**
 * Cuerpo para crear o actualizar una ruta (contrato propuesto).
 *
 * POST `api/complexes/{complexId}/patrol-routes` y
 * PUT `api/complexes/{complexId}/patrol-routes/{routeId}`.
 */
data class PatrolRouteRequestDto(
    val name: String,
    val pointIds: List<String>,
    val schedules: List<PatrolScheduleDto>,
    val isActive: Boolean
)
