package co.edu.unicauca.urbanguard.ui.features.patrol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PatrolRouteFormUiStateTest {

    private val bogota = ZoneId.of("America/Bogota")

    @Test
    fun defaultPatrolSchedule_usesTodayAndSoonForImmediateTesting() {
        val now = ZonedDateTime.of(2026, 10, 29, 9, 41, 37, 0, bogota)

        val schedule = defaultPatrolSchedule(now)

        assertEquals(LocalTime.of(9, 51), schedule.time)
        assertTrue(DayOfWeek.THURSDAY in schedule.days)
        assertTrue(DayOfWeek.MONDAY in schedule.days)
        assertTrue(DayOfWeek.FRIDAY in schedule.days)
    }

    @Test
    fun defaultPatrolSchedule_rollsToTomorrowWhenNearMidnight() {
        val now = ZonedDateTime.of(2026, 10, 30, 23, 55, 0, 0, bogota)

        val schedule = defaultPatrolSchedule(now)

        assertEquals(LocalTime.of(0, 5), schedule.time)
        assertTrue(DayOfWeek.SATURDAY in schedule.days)
    }

    @Test
    fun withSharedDays_appliesTheSameSelectionToEverySchedule() {
        val schedules = listOf(
            PatrolSchedule(LocalTime.of(8, 0), setOf(DayOfWeek.MONDAY)),
            PatrolSchedule(LocalTime.of(22, 0), setOf(DayOfWeek.FRIDAY))
        )
        val selectedDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)

        val updated = schedules.withSharedDays(selectedDays)

        assertEquals(listOf(selectedDays, selectedDays), updated.map { it.days })
        assertEquals(listOf(LocalTime.of(8, 0), LocalTime.of(22, 0)), updated.map { it.time })
    }

    @Test
    fun togglingSharedDay_updatesSelectionWithoutChangingOtherDays() {
        val selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)

        val updated = selectedDays.withToggledDay(DayOfWeek.TUESDAY, selected = true)

        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY), updated)
    }
}
