package co.edu.unicauca.urbanguard.ui.features.patrol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
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
}
