package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolScanDto
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.PatrolScan
import co.edu.unicauca.urbanguard.domain.model.RoundStatus
import co.edu.unicauca.urbanguard.domain.model.donePointIds
import co.edu.unicauca.urbanguard.domain.model.forDay
import co.edu.unicauca.urbanguard.domain.model.isFinished
import co.edu.unicauca.urbanguard.domain.model.matchScan
import co.edu.unicauca.urbanguard.domain.model.status
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import co.edu.unicauca.urbanguard.domain.model.progressOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

/**
 * Tests de rondas: matcheo de tags, progreso, filtro del día y mapeo.
 */
class PatrolRoundMapperTest {

    private fun point(id: String = "p1", tag: String = "04A3F2") = PatrolControlPoint(
        id = id,
        complexId = "c1",
        name = "Portería",
        location = "",
        nfcTagId = tag,
        order = 0,
        isActive = true
    )

    private fun roundWith(vararg scans: PatrolScan) = PatrolRound(
        id = "r1",
        complexId = "c1",
        routeId = "rt1",
        routeName = "Nocturna",
        startedAt = Instant.parse("2026-02-10T22:00:00Z"),
        scans = scans.toList()
    )

    @Test
    fun matchScan_caseInsensitive_findsPoint() {
        assertEquals(point(), matchScan("04a3f2", listOf(point())))
    }

    @Test
    fun matchScan_unknownTag_returnsNull() {
        assertNull(matchScan("FFFF", listOf(point())))
    }

    @Test
    fun matchScan_blankTag_returnsNull() {
        assertNull(matchScan("  ", listOf(point())))
    }

    @Test
    fun donePointIds_dedupesAndIgnoresUnknown() {
        val round = roundWith(
            PatrolScan("p1", "04A3", Instant.EPOCH, true),
            PatrolScan("p1", "04A3", Instant.EPOCH, true),
            PatrolScan(null, "FFFF", Instant.EPOCH, false)
        )

        assertEquals(setOf("p1"), round.donePointIds())
        assertEquals(0.5f, round.progressOf(2))
        assertFalse(round.isFinished())
    }

    @Test
    fun progressOf_zeroTotal_isZero() {
        assertEquals(0f, roundWith().progressOf(0))
    }

    @Test
    fun status_openRound_isInProgress() {
        assertEquals(RoundStatus.IN_PROGRESS, roundWith().status())
    }

    @Test
    fun status_finishedAllPoints_isCompleted() {
        val round = roundWith(
            PatrolScan("p1", "A", Instant.EPOCH, true),
            PatrolScan("p2", "B", Instant.EPOCH, true)
        ).copy(finishedAt = Instant.parse("2026-02-10T23:00:00Z"), totalPoints = 2)

        assertEquals(RoundStatus.COMPLETED, round.status())
    }

    @Test
    fun status_finishedMissingPoints_isIncomplete() {
        val round = roundWith(
            PatrolScan("p1", "A", Instant.EPOCH, true)
        ).copy(finishedAt = Instant.parse("2026-02-10T23:00:00Z"), totalPoints = 3)

        assertEquals(RoundStatus.INCOMPLETE, round.status())
    }

    @Test
    fun forDay_onlyRoutesWithScheduleToday() {
        val monday = setOf(DayOfWeek.MONDAY)
        val routes = listOf(
            PatrolRoute("r1", "c1", "A", listOf("p1"), listOf(schedule(monday)), true),
            PatrolRoute("r2", "c1", "B", listOf("p1"), listOf(schedule(monday)), false),
            PatrolRoute(
                "r3", "c1", "C", listOf("p1"),
                listOf(schedule(setOf(DayOfWeek.SUNDAY))), true
            )
        )

        val filtered = routes.forDay(DayOfWeek.MONDAY).map { it.id }

        assertEquals(listOf("r1"), filtered)
    }

    @Test
    fun roundDto_roundTripKeepsFields() {
        val dto = PatrolRoundDto(
            id = "r1",
            routeId = "rt1",
            routeName = "Nocturna",
            startedAt = "2026-02-10T22:00:00Z",
            finishedAt = null,
            scans = listOf(
                PatrolScanDto("p1", "04A3", "2026-02-10T22:05:00Z", true)
            )
        )

        val domain = dto.toDomain("c1")

        assertEquals("r1", domain.id)
        assertEquals("Nocturna", domain.routeName)
        assertEquals(1, domain.scans.size)
        assertEquals("p1", domain.scans.first().pointId)
        assertTrue(domain.scans.first().known)
        assertEquals(dto, domain.toDto())
    }

    private fun schedule(days: Set<DayOfWeek>) =
        PatrolSchedule(time = LocalTime.of(22, 0), days = days)
}
