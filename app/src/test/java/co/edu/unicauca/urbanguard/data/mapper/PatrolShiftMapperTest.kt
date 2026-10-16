package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolGuardRefDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolGuardSummaryDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftDetailDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftRoundDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftSummaryDto
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundReason
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * El contrato del backend es la fuente de verdad: estos tests fijan que el
 * mapeo traduzca los enums y fechas esperados y que un valor desconocido o un
 * campo obligatorio ausente hagan fallar toda la respuesta (no se descarta un
 * elemento en silencio).
 */
class PatrolShiftMapperTest {

    private fun summaryDto(
        shiftType: String? = "NIGHT",
        date: String? = "2026-10-11",
        status: String? = "CLOSED",
        goalMet: Boolean? = true
    ) = PatrolShiftSummaryDto(
        shiftType = shiftType,
        date = date,
        status = status,
        expectedRounds = 3,
        completedRounds = 2,
        pointsExpected = 12,
        pointsScanned = 9,
        goalMet = goalMet
    )

    @Test
    fun summaryDto_valido_seMapeaConEnumsYFecha() {
        val domain = summaryDto().toDomain()

        assertEquals(PatrolShiftType.NIGHT, domain.shiftType)
        assertEquals(LocalDate.of(2026, 10, 11), domain.date)
        assertEquals(PatrolShiftStatus.CLOSED, domain.status)
        assertEquals(3, domain.expectedRounds)
        assertEquals(2, domain.completedRounds)
        assertTrue(domain.goalMet == true)
    }

    @Test
    fun summaryDto_conTipoDesconocido_fallaLaRespuesta() {
        assertInvalidResponse { summaryDto(shiftType = "DAWN").toDomain() }
    }

    @Test
    fun summaryDto_conFechaInvalida_fallaLaRespuesta() {
        assertInvalidResponse { summaryDto(date = "11/10/2026").toDomain() }
    }

    @Test
    fun summaryDto_conEstadoDesconocido_fallaLaRespuesta() {
        assertInvalidResponse { summaryDto(status = "UNKNOWN").toDomain() }
    }

    @Test
    fun summaryDto_conCampoObligatorioAusente_fallaLaRespuesta() {
        assertInvalidResponse { summaryDto(shiftType = null).toDomain() }
    }

    @Test
    fun detailDto_mapeaRondasYVeredictoNuloDePendiente() {
        val dto = PatrolShiftDetailDto(
            status = "PENDING",
            expectedRounds = 1,
            completedRounds = 0,
            pointsExpected = 4,
            pointsScanned = 0,
            goalMet = null,
            rounds = listOf(roundDto())
        )

        val domain = dto.toDomain()

        assertEquals(PatrolShiftStatus.PENDING, domain.status)
        assertNull(domain.goalMet)
        assertEquals(1, domain.rounds.size)
    }

    @Test
    fun detailDto_conRondaInvalida_fallaTodaLaRespuesta() {
        val dto = PatrolShiftDetailDto(
            status = "CLOSED",
            expectedRounds = 1,
            completedRounds = 1,
            pointsExpected = 4,
            pointsScanned = 4,
            goalMet = true,
            rounds = listOf(roundDto(), roundDto(status = "RARE", reason = null))
        )

        assertInvalidResponse { dto.toDomain() }
    }

    @Test
    fun roundDto_parseaInstanteConOffset() {
        val domain = roundDto(scheduledAt = "2026-10-11T20:05:00-05:00").toDomain()

        assertEquals(
            OffsetDateTime.parse("2026-10-11T20:05:00-05:00").toInstant(),
            domain.scheduledAt
        )
    }

    @Test
    fun roundDto_noProgramada_tieneEstadoYMotivoNulos() {
        val domain = roundDto(
            scheduledAt = null,
            status = null,
            reason = null,
            guard = null
        ).toDomain()

        assertTrue(domain.isUnscheduled)
        assertNull(domain.status)
        assertNull(domain.reason)
        assertNull(domain.guard)
    }

    @Test
    fun roundDto_conEstadoYMotivoValidos_seMapean() {
        val domain = roundDto(
            status = "INCOMPLETE",
            reason = "OUT_OF_SCHEDULE"
        ).toDomain()

        assertEquals(PatrolRoundStatus.INCOMPLETE, domain.status)
        assertEquals(PatrolRoundReason.OUT_OF_SCHEDULE, domain.reason)
        assertFalse(domain.isUnscheduled)
    }

    @Test
    fun roundDto_conEstadoDesconocido_fallaLaRespuesta() {
        assertInvalidResponse { roundDto(status = "RARE", reason = null).toDomain() }
    }

    @Test
    fun guardSummaryDto_mapeaAgregadosYRutas() {
        val dto = PatrolGuardSummaryDto(
            guardName = "Carlos Pérez",
            roundsPerformed = 8,
            shiftsWithParticipation = 3,
            averageRoundsPerShift = 2.67,
            routes = listOf("Nocturna", "Perimetral")
        )

        val domain = dto.toDomain()

        assertEquals("Carlos Pérez", domain.guardName)
        assertEquals(8, domain.roundsPerformed)
        assertEquals(3, domain.shiftsWithParticipation)
        assertEquals(2.67, domain.averageRoundsPerShift, 0.001)
        assertEquals(listOf("Nocturna", "Perimetral"), domain.routes)
    }

    @Test
    fun guardSummaryDto_sinNombre_fallaLaRespuesta() {
        val dto = PatrolGuardSummaryDto(
            guardName = null,
            roundsPerformed = 0,
            shiftsWithParticipation = 0,
            averageRoundsPerShift = 0.0,
            routes = emptyList()
        )

        assertInvalidResponse { dto.toDomain() }
    }

    private fun assertInvalidResponse(block: () -> Unit) {
        val exception = assertThrows(AppException::class.java) { block() }
        assertEquals(PatrolHistoryConstants.MSG_INVALID_SERVER_RESPONSE, exception.message)
    }

    private fun roundDto(
        scheduledAt: String? = "2026-10-11T22:00:00-05:00",
        status: String? = "COMPLETED",
        reason: String? = null,
        guard: PatrolGuardRefDto? = PatrolGuardRefDto(id = "g1", name = "Carlos")
    ) = PatrolShiftRoundDto(
        routeName = "Nocturna",
        guard = guard,
        scheduledAt = scheduledAt,
        startedAt = "2026-10-11T22:03:00-05:00",
        status = status,
        reason = reason,
        pointsScanned = 4,
        pointsExpected = 4
    )
}
