package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundPointDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolScanDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolExecutionDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolPointViewDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolOccurrenceDto
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.PatrolScan
import co.edu.unicauca.urbanguard.domain.model.RoundStatus
import co.edu.unicauca.urbanguard.domain.model.availableFrom
import co.edu.unicauca.urbanguard.domain.model.coveringOccurrence
import co.edu.unicauca.urbanguard.domain.model.donePointIds
import co.edu.unicauca.urbanguard.domain.model.forDay
import co.edu.unicauca.urbanguard.domain.model.isFinished
import co.edu.unicauca.urbanguard.domain.model.isOverdue
import co.edu.unicauca.urbanguard.domain.model.matchScan
import co.edu.unicauca.urbanguard.domain.model.nextOccurrenceAfter
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Tests de rondas: matcheo de tags, progreso, filtro del día y mapeo.
 */
class PatrolRoundMapperTest {

    private fun point(id: String = "p1", tag: String = "04A3F2B1") = PatrolControlPoint(
        id = id,
        complexId = "c1",
        name = "Portería",
        location = "",
        nfcTagId = tag,
        isActive = true
    )

    private fun roundWith(
        vararg scans: PatrolScan,
        points: List<PatrolControlPoint> = listOf(point())
    ) = PatrolRound(
        id = "r1",
        complexId = "c1",
        routeId = "rt1",
        routeName = "Nocturna",
        startedAt = Instant.parse("2026-02-10T22:00:00Z"),
        scans = scans.toList(),
        points = points
    )

    @Test
    fun matchScan_caseInsensitive_findsPoint() {
        assertEquals(point(), matchScan("04:a3:f2:b1", listOf(point())))
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
            PatrolScan("p2", "B", Instant.EPOCH, true),
            points = listOf(point("p1"), point("p2"))
        ).copy(finishedAt = Instant.parse("2026-02-10T23:00:00Z"))

        assertEquals(2, round.totalPoints)
        assertEquals(RoundStatus.COMPLETED, round.status())
    }

    @Test
    fun status_finishedMissingPoints_isIncomplete() {
        val round = roundWith(
            PatrolScan("p1", "A", Instant.EPOCH, true),
            points = listOf(point("p1"), point("p2"), point("p3"))
        ).copy(finishedAt = Instant.parse("2026-02-10T23:00:00Z"))

        assertEquals(RoundStatus.INCOMPLETE, round.status())
    }

    @Test
    fun donePointIds_ignoresScansOutsideSnapshot() {
        val round = roundWith(
            PatrolScan("old-point", "A", Instant.EPOCH, true),
            points = listOf(point("p1"))
        )

        assertTrue(round.donePointIds().isEmpty())
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
                PatrolScanDto("p1", "04A3F2B1", "2026-02-10T22:05:00Z", true)
            ),
            points = listOf(
                PatrolRoundPointDto(
                    id = "p1",
                    name = "Portería",
                    description = "",
                    nfcTagId = "04A3F2B1"
                )
            )
        )

        val domain = dto.toDomain("c1")

        assertEquals("r1", domain.id)
        assertEquals("Nocturna", domain.routeName)
        assertEquals(1, domain.scans.size)
        assertEquals(1, domain.totalPoints)
        assertEquals("p1", domain.scans.first().pointId)
        assertTrue(domain.scans.first().known)
        assertEquals(dto, domain.toDto())
    }

    @Test
    fun timedOutExecution_isMappedAsIncompleteInHistory() {
        val execution = PatrolExecutionDto(
            executionId = "execution-1",
            occurrenceId = "occurrence-1",
            routeId = "route-1",
            routeName = "Ronda nocturna",
            scheduledAt = "2026-10-19T22:00:00Z",
            earliestStartAt = "2026-10-19T21:50:00Z",
            deadlineAt = "2026-10-20T01:00:00Z",
            startedAt = "2026-10-19T22:01:00Z",
            status = "INCOMPLETE",
            serverTime = "2026-10-20T01:00:00Z",
            pointsCompleted = 1,
            pointsExpected = 1,
            checkpoints = listOf(
                PatrolPointViewDto(
                    checkpointId = "point-1",
                    position = 1,
                    name = "Entrada",
                    scannedAt = "2026-10-19T22:05:00Z"
                )
            )
        )

        val round = execution.toDomain("complex-1")

        assertEquals(RoundStatus.INCOMPLETE, round.status())
    }

    @Test
    fun occurrenceRoute_keepsServerAvailabilityAndOccurrenceIdentity() {
        val occurrence = PatrolOccurrenceDto(
            occurrenceId = "occurrence-1",
            routeId = "route-1",
            routeName = "Ronda norte",
            scheduledAt = "2026-10-20T04:00:00Z",
            earliestStartAt = "2026-10-20T03:50:00Z",
            deadlineAt = "2026-10-20T07:00:00Z",
            pointsExpected = 2,
            status = "SCHEDULED",
            available = true
        )

        val route = occurrence.toRoute("complex-1")

        assertEquals("occurrence-1", route.occurrenceId)
        assertEquals(true, route.occurrenceAvailable)
        assertEquals(Instant.parse("2026-10-20T03:50:00Z"), route.earliestStartAt)
        assertEquals(Instant.parse("2026-10-20T07:00:00Z"), route.occurrenceDeadlineAt)
        assertEquals(2, route.pointIds.size)
    }

    private fun schedule(days: Set<DayOfWeek>) =
        PatrolSchedule(time = LocalTime.of(22, 0), days = days)

    private fun routeWith(vararg times: LocalTime) = PatrolRoute(
        id = "rt1",
        complexId = "c1",
        name = "Nocturna",
        pointIds = listOf("p1"),
        schedules = times.map {
            PatrolSchedule(time = it, days = DayOfWeek.values().toSet())
        },
        isActive = true
    )

    @Test
    fun coveringOccurrence_earlyStart_coversUpcoming() {
        val route = routeWith(LocalTime.of(22, 0))
        val now = LocalDateTime.of(2026, 2, 10, 21, 35)

        val covered = coveringOccurrence(route, now, 30)

        assertEquals(LocalDateTime.of(2026, 2, 10, 22, 0), covered)
    }

    @Test
    fun coveringOccurrence_tooEarly_returnsNull() {
        val sundayOnly = PatrolRoute(
            id = "rt1",
            complexId = "c1",
            name = "Dominical",
            pointIds = listOf("p1"),
            schedules = listOf(
                PatrolSchedule(time = LocalTime.of(10, 0), days = setOf(DayOfWeek.SUNDAY))
            ),
            isActive = true
        )
        // Martes 09:00: sin cita cercana en la ventana de búsqueda.
        val now = LocalDateTime.of(2026, 2, 10, 9, 0)

        assertEquals(null, coveringOccurrence(sundayOnly, now, 30))
    }

    @Test
    fun nextOccurrenceAfter_jumpsToNextSchedule() {
        val routes = listOf(
            routeWith(LocalTime.of(10, 0)),
            routeWith(LocalTime.of(22, 0))
        )
        val after = LocalDateTime.of(2026, 2, 10, 10, 30)

        val next = nextOccurrenceAfter(routes, after)

        assertEquals(LocalDateTime.of(2026, 2, 10, 22, 0), next)
    }

    @Test
    fun availableFrom_isThirtyMinutesBefore() {
        val route = routeWith(LocalTime.of(22, 0))
        val monday = LocalDate.of(2026, 2, 9)

        assertEquals(LocalTime.of(21, 30), route.availableFrom(monday, 30))
    }

    @Test
    fun isOverdue_openPastDue_isTrue() {
        val round = roundWith().copy(
            dueAt = Instant.parse("2026-02-10T22:00:00Z")
        )

        assertTrue(round.isOverdue(Instant.parse("2026-02-10T23:00:00Z")))
        assertFalse(round.isOverdue(Instant.parse("2026-02-10T21:00:00Z")))
    }
}
