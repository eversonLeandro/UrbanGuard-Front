package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateCheckpointRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CurrentTagDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolScheduleDto
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import co.edu.unicauca.urbanguard.domain.model.normalizeNfcUid
import com.google.gson.JsonObject
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Mapeo entre el modelo de dominio y los DTOs de checkpoints.
 *
 * El backend es la fuente de verdad de los puntos; esta capa también
 * construye el PATCH parcial para no borrar campos que el formulario no tocó.
 */
fun PatrolControlPoint.toCreateDto(): CreateCheckpointRequestDto =
    CreateCheckpointRequestDto(
        name = name.trim(),
        description = description.trim().takeIf { it.isNotEmpty() },
        location = location.trim().takeIf { it.isNotEmpty() },
        uid = normalizeNfcUid(nfcTagId)
    )

fun PatrolControlPoint.toPatchBody(current: PatrolControlPoint): JsonObject =
    JsonObject().apply {
        if (name.trim() != current.name) addProperty("name", name.trim())
        if (description.trim() != current.description) {
            addProperty("description", description.trim().takeIf { it.isNotEmpty() })
        }
        if (location.trim() != current.location) {
            addProperty("location", location.trim().takeIf { it.isNotEmpty() })
        }
        if (isActive != current.isActive) addProperty("active", isActive)
    }

fun CheckpointResponseDto.toDomain(complexId: String): PatrolControlPoint =
    PatrolControlPoint(
        id = checkpointId,
        complexId = complexId,
        name = name,
        location = location.orEmpty(),
        description = description.orEmpty(),
        nfcTagId = normalizeNfcUid(currentTag?.uid.orEmpty()),
        isActive = active
    )

/** DTO equivalente para guardar la caché local con la misma forma del backend. */
fun PatrolControlPoint.toResponseDto(): CheckpointResponseDto = CheckpointResponseDto(
    checkpointId = id,
    name = name,
    description = description.takeIf { it.isNotBlank() },
    location = location.takeIf { it.isNotBlank() },
    active = isActive,
    currentTag = CurrentTagDto(uid = normalizeNfcUid(nfcTagId))
)

private val scheduleTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** "HH:mm" ↔ LocalTime; los días viajan como ISO 1-7. */
fun PatrolSchedule.toDto(): PatrolScheduleDto = PatrolScheduleDto(
    time = time.format(scheduleTimeFormat),
    days = days.map { it.value }.sorted()
)

fun PatrolScheduleDto.toDomain(): PatrolSchedule = PatrolSchedule(
    time = LocalTime.parse(time, scheduleTimeFormat),
    days = days.mapNotNull { runCatching { DayOfWeek.of(it) }.getOrNull() }.toSet()
)

fun PatrolRoute.toRequestDto(): PatrolRouteRequestDto = PatrolRouteRequestDto(
    name = name.trim(),
    checkpointIds = pointIds,
    description = null,
    schedules = schedules.map { it.toDto() }
)

fun PatrolRouteDto.toDomain(complexId: String): PatrolRoute {
    val realId = routeId ?: id.orEmpty()
    val extractedPointIds = checkpoints?.map { it.checkpointId } ?: pointIds.orEmpty()
    val extractedSchedules = schedules.orEmpty().map { it.toDomain() }
    val activeState = active ?: isActive ?: true
    return PatrolRoute(
        id = realId,
        complexId = complexId,
        name = name.orEmpty(),
        pointIds = extractedPointIds,
        schedules = extractedSchedules,
        isActive = activeState
    )
}

fun PatrolRoute.toDto(): PatrolRouteDto = PatrolRouteDto(
    id = id,
    routeId = id,
    name = name,
    pointIds = pointIds,
    schedules = schedules.map { it.toDto() },
    isActive = isActive,
    active = isActive
)
