package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolScheduleDto
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.PatrolRouteValidation
import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import co.edu.unicauca.urbanguard.domain.model.validatePatrolRoute
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Tests de rutas de patrullaje: validación del formulario y mapeo con los
 * DTOs (el mismo que usará el backend).
 */
class PatrolRouteMapperTest {

    private fun schedule(
        time: LocalTime = LocalTime.of(22, 0),
        days: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY)
    ) = PatrolSchedule(time = time, days = days)

    @Test
    fun validate_blankName_isRequired() {
        val result = validatePatrolRoute("  ", listOf("p1"), listOf(schedule()))
        assertEquals(PatrolRouteValidation.NAME_REQUIRED, result)
    }

    @Test
    fun validate_noPoints_isRequired() {
        val result = validatePatrolRoute("Nocturna", emptyList(), listOf(schedule()))
        assertEquals(PatrolRouteValidation.NO_POINTS, result)
    }

    @Test
    fun validate_noSchedules_isRequired() {
        val result = validatePatrolRoute("Nocturna", listOf("p1"), emptyList())
        assertEquals(PatrolRouteValidation.NO_SCHEDULES, result)
    }

    @Test
    fun validate_scheduleWithoutDays_isRequired() {
        val result = validatePatrolRoute(
            "Nocturna",
            listOf("p1"),
            listOf(schedule(days = emptySet()))
        )
        assertEquals(PatrolRouteValidation.SCHEDULE_WITHOUT_DAYS, result)
    }

    @Test
    fun validate_ok_isValid() {
        val result = validatePatrolRoute(
            "Nocturna",
            listOf("p1", "p2"),
            listOf(schedule(), schedule(LocalTime.of(10, 0)))
        )
        assertEquals(PatrolRouteValidation.VALID, result)
    }

    @Test
    fun dto_roundTripKeepsFields() {
        val dto = PatrolRouteDto(
            id = "r1",
            name = "Nocturna",
            pointIds = listOf("p1", "p2"),
            schedules = listOf(PatrolScheduleDto(time = "22:00", days = listOf(1, 3, 5))),
            isActive = true
        )

        val domain: PatrolRoute = dto.toDomain("c1")

        assertEquals("r1", domain.id)
        assertEquals("c1", domain.complexId)
        assertEquals(listOf("p1", "p2"), domain.pointIds)
        assertEquals(LocalTime.of(22, 0), domain.schedules.first().time)
        assertEquals(
            setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            domain.schedules.first().days
        )
        assertEquals(dto, domain.toDto())
    }
}
