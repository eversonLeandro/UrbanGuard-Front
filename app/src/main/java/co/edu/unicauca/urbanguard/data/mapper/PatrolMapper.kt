package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolPointRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolPointResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolScheduleDto
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Mapeo entre el modelo de dominio y los DTOs de puntos de control.
 *
 * Hoy lo usa el almacén local; cuando se encienda el backend, el mismo
 * mapeo sirve para `PatrolApi` sin cambios.
 */
fun PatrolControlPoint.toRequestDto(): PatrolPointRequestDto = PatrolPointRequestDto(
    name = name.trim(),
    location = location.trim(),
    nfcTagId = nfcTagId.trim().uppercase(),
    order = order,
    isActive = isActive
)

fun PatrolPointResponseDto.toDomain(complexId: String): PatrolControlPoint =
    PatrolControlPoint(
        id = id,
        complexId = complexId,
        name = name,
        location = location,
        nfcTagId = nfcTagId,
        order = order,
        isActive = isActive
    )

fun PatrolControlPoint.toResponseDto(): PatrolPointResponseDto = PatrolPointResponseDto(
    id = id,
    name = name,
    location = location,
    nfcTagId = nfcTagId,
    order = order,
    isActive = isActive
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
    pointIds = pointIds,
    schedules = schedules.map { it.toDto() },
    isActive = isActive
)

fun PatrolRouteDto.toDomain(complexId: String): PatrolRoute = PatrolRoute(
    id = id,
    complexId = complexId,
    name = name,
    pointIds = pointIds,
    schedules = schedules.map { it.toDomain() },
    isActive = isActive
)

fun PatrolRoute.toDto(): PatrolRouteDto = PatrolRouteDto(
    id = id,
    name = name,
    pointIds = pointIds,
    schedules = schedules.map { it.toDto() },
    isActive = isActive
)
