package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Cuerpo para crear un checkpoint NFC.
 *
 * POST `api/complexes/{complexId}/checkpoints`.
 */
data class CreateCheckpointRequestDto(
    val name: String,
    val description: String? = null,
    val location: String? = null,
    val uid: String
)

/** Cuerpo para reemplazar el tag activo de un checkpoint. */
data class ReplaceCheckpointTagRequestDto(
    val uid: String
)

/** Tag NFC vigente de un checkpoint. */
data class CurrentTagDto(
    val assignmentId: String = "",
    val uid: String = "",
    val assignedAt: String? = null
)

/**
 * Checkpoint devuelto por `security-service`.
 *
 * GET `api/complexes/{complexId}/checkpoints` → 200 con la lista,
 * o `200 []` si el conjunto aún no tiene puntos.
 */
data class CheckpointResponseDto(
    val checkpointId: String = "",
    val name: String = "",
    val description: String? = null,
    val location: String? = null,
    val active: Boolean = true,
    val currentTag: CurrentTagDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Un horario de una ruta (contrato futuro para UG-93).
 *
 * `time` en "HH:mm" (24h) y `days` con los días ISO 1-7 (lunes-domingo).
 */
data class PatrolScheduleDto(
    val time: String,
    val days: List<Int>
)

/**
 * Ruta de patrullaje (contrato futuro para UG-93).
 *
 * Mientras no exista el endpoint, el frontend la guarda en local.
 */
data class RouteCheckpointItemDto(
    val checkpointId: String,
    val position: Int,
    val name: String
)

data class PatrolRouteDto(
    val routeId: String? = null,
    val id: String? = null,
    val name: String? = null,
    val pointIds: List<String>? = null,
    val checkpoints: List<RouteCheckpointItemDto>? = null,
    val schedules: List<PatrolScheduleDto>? = null,
    val active: Boolean? = null,
    val isActive: Boolean? = null
)

/** Cuerpo para crear o actualizar una ruta en el backend. */
data class PatrolRouteRequestDto(
    val name: String,
    val checkpointIds: List<String>,
    val description: String? = null,
    val schedules: List<PatrolScheduleDto>? = null,
    val active: Boolean = true
)
