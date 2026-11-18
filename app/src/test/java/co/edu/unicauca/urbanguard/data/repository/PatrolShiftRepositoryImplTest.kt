package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolGuardRefDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolGuardSummaryDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftDetailDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftRoundDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolShiftSummaryDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolShiftApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

/**
 * Tests del repositorio de historial de patrullaje. Se verifica que se respete
 * el contrato, que `onlyIncomplete` y `guardId` se manden solo cuando corresponda,
 * que una respuesta con un elemento inválido falle completa y que el 404 use el
 * mensaje propio de cada endpoint.
 */
class PatrolShiftRepositoryImplTest {

    @Test
    fun getShiftSummaries_conComplexIdVacio_retornaValidation() = runTest {
        val api = FakePatrolShiftApi()
        val repo = PatrolShiftRepositoryImpl(api)

        val result = repo.getShiftSummaries(
            complexId = "",
            from = LocalDate.of(2026, 10, 10),
            to = LocalDate.of(2026, 10, 11),
            guardId = null,
            onlyIncomplete = false
        )

        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as? AppException)?.error
        assertTrue(error is AppError.Validation)
        assertEquals(PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX, (error as AppError.Validation).message)
        assertEquals(0, api.shiftsCalls)
    }

    @Test
    fun getShiftSummaries_enviaParams() = runTest {
        val api = FakePatrolShiftApi(summaries = listOf(validSummary()))
        val repo = PatrolShiftRepositoryImpl(api)

        val result = repo.getShiftSummaries(
            complexId = "c1",
            from = LocalDate.of(2026, 10, 10),
            to = LocalDate.of(2026, 10, 11),
            guardId = "g1",
            onlyIncomplete = true
        )

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals(1, api.shiftsCalls)
        assertEquals("c1", api.lastComplexId)
        assertEquals("2026-10-10", api.lastFrom)
        assertEquals("2026-10-11", api.lastTo)
        assertEquals("g1", api.lastGuardId)
        assertTrue(api.lastOnlyIncomplete == true)
    }

    @Test
    fun getShiftSummaries_guardIdYOnlyIncompleteSoloCuandoValen() = runTest {
        val api = FakePatrolShiftApi(summaries = listOf(validSummary()))
        val repo = PatrolShiftRepositoryImpl(api)

        repo.getShiftSummaries(
            complexId = "c1",
            from = LocalDate.of(2026, 10, 10),
            to = LocalDate.of(2026, 10, 11),
            guardId = "",
            onlyIncomplete = false
        )

        assertEquals(null, api.lastGuardId)
        assertEquals(null, api.lastOnlyIncomplete)
        assertEquals(null, api.lastStatus)
    }

    @Test
    fun getShiftSummaries_enviaEstadoSeleccionado() = runTest {
        val api = FakePatrolShiftApi(summaries = listOf(validSummary()))
        val repo = PatrolShiftRepositoryImpl(api)

        repo.getShiftSummaries(
            complexId = "c1",
            from = LocalDate.of(2026, 10, 10),
            to = LocalDate.of(2026, 10, 11),
            guardId = null,
            onlyIncomplete = false,
            status = PatrolShiftStatus.PENDING
        )

        assertEquals("PENDING", api.lastStatus)
    }

    @Test
    fun getShiftSummaries_conElementoInvalido_fallaTodaLaRespuesta() = runTest {
        val api = FakePatrolShiftApi(
            summaries = listOf(validSummary(), invalidSummaryShiftType())
        )
        val repo = PatrolShiftRepositoryImpl(api)

        val result = repo.getShiftSummaries(
            complexId = "c1",
            from = LocalDate.of(2026, 10, 10),
            to = LocalDate.of(2026, 10, 11),
            guardId = null,
            onlyIncomplete = false
        )

        assertTrue(result.isFailure)
        assertEquals(
            PatrolHistoryConstants.MSG_INVALID_SERVER_RESPONSE,
            (result.exceptionOrNull() as? AppException)?.message
        )
    }

    @Test
    fun getShiftDetail_mapeaYConvierteErrorAEspanol() = runTest {
        val api = FakePatrolShiftApi(detail = validDetail())
        val repo = PatrolShiftRepositoryImpl(api)

        val result = repo.getShiftDetail(
            complexId = "c1",
            shiftType = PatrolShiftType.DAY,
            date = LocalDate.of(2026, 10, 10)
        )

        assertTrue(result.isSuccess)
        assertEquals(1, api.detailCalls)
        assertEquals("DAY", api.lastShiftType)
        assertEquals("2026-10-10", api.lastDateDetail)
    }

    @Test
    fun getShiftDetail_siApiLanzaIOException_viajaComoAppException() = runTest {
        val api = FakePatrolShiftApi(detailThrows = IOException("boom"))
        val repo = PatrolShiftRepositoryImpl(api)

        val result = repo.getShiftDetail(
            complexId = "c1",
            shiftType = PatrolShiftType.NIGHT,
            date = LocalDate.of(2026, 10, 10)
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppException)
    }

    @Test
    fun getShiftDetail_con404_usaMensajeDeTurnoNoEncontrado() = runTest {
        val api = FakePatrolShiftApi(
            detailThrows = AppException(AppError.NotFound(AppError.NotFound.MESSAGE))
        )
        val repo = PatrolShiftRepositoryImpl(api)

        val result = repo.getShiftDetail(
            complexId = "c1",
            shiftType = PatrolShiftType.NIGHT,
            date = LocalDate.of(2026, 10, 10)
        )

        assertEquals(
            PatrolHistoryConstants.MSG_SHIFT_NOT_FOUND,
            (result.exceptionOrNull() as? AppException)?.message
        )
    }

    @Test
    fun getShiftDetail_conErrorNo404_conservaElMensaje() = runTest {
        val api = FakePatrolShiftApi(
            detailThrows = AppException(AppError.Forbidden("Acceso denegado"))
        )
        val repo = PatrolShiftRepositoryImpl(api)

        val result = repo.getShiftDetail(
            complexId = "c1",
            shiftType = PatrolShiftType.NIGHT,
            date = LocalDate.of(2026, 10, 10)
        )

        assertEquals(
            "Acceso denegado",
            (result.exceptionOrNull() as? AppException)?.message
        )
    }

    @Test
    fun getGuardSummary_ok() = runTest {
        val api = FakePatrolShiftApi(guardSummary = validGuardSummary())
        val repo = PatrolShiftRepositoryImpl(api)

        val result = repo.getGuardSummary(
            complexId = "c1",
            guardId = "g1",
            from = LocalDate.of(2026, 10, 10),
            to = LocalDate.of(2026, 10, 11)
        )

        assertTrue(result.isSuccess)
        assertEquals(1, api.guardCalls)
        assertEquals("g1", api.lastGuardIdPath)
    }

    @Test
    fun getGuardSummary_con404_usaMensajeDeVigilanteNoEncontrado() = runTest {
        val api = FakePatrolShiftApi(
            guardThrows = AppException(AppError.NotFound(AppError.NotFound.MESSAGE))
        )
        val repo = PatrolShiftRepositoryImpl(api)

        val result = repo.getGuardSummary(
            complexId = "c1",
            guardId = "g1",
            from = LocalDate.of(2026, 10, 10),
            to = LocalDate.of(2026, 10, 11)
        )

        assertEquals(
            PatrolHistoryConstants.MSG_GUARD_NOT_FOUND,
            (result.exceptionOrNull() as? AppException)?.message
        )
    }

    private class FakePatrolShiftApi(
        val summaries: List<PatrolShiftSummaryDto> = emptyList(),
        val detail: PatrolShiftDetailDto = validDetail(),
        val guardSummary: PatrolGuardSummaryDto = validGuardSummary(),
        val detailThrows: Exception? = null,
        val guardThrows: Exception? = null
    ) : PatrolShiftApi {

        var shiftsCalls = 0
        var detailCalls = 0
        var guardCalls = 0

        var lastComplexId: String? = null
        var lastFrom: String? = null
        var lastTo: String? = null
        var lastGuardId: String? = null
        var lastOnlyIncomplete: Boolean? = null
        var lastStatus: String? = null

        var lastShiftType: String? = null
        var lastDateDetail: String? = null
        var lastGuardIdPath: String? = null

        override suspend fun getShifts(
            complexId: String,
            from: String,
            to: String,
            guardId: String?,
            onlyIncomplete: Boolean?,
            status: String?
        ): List<PatrolShiftSummaryDto> {
            shiftsCalls++
            lastComplexId = complexId
            lastFrom = from
            lastTo = to
            lastGuardId = guardId
            lastOnlyIncomplete = onlyIncomplete
            lastStatus = status
            return summaries
        }

        override suspend fun getShiftDetail(
            complexId: String,
            shiftType: String,
            date: String
        ): PatrolShiftDetailDto {
            detailCalls++
            lastShiftType = shiftType
            lastDateDetail = date
            if (detailThrows != null) throw detailThrows
            return detail
        }

        override suspend fun getGuardSummary(
            complexId: String,
            guardId: String,
            from: String,
            to: String
        ): PatrolGuardSummaryDto {
            guardCalls++
            lastGuardIdPath = guardId
            if (guardThrows != null) throw guardThrows
            return guardSummary
        }
    }

    companion object {
        private fun validSummary() = PatrolShiftSummaryDto(
            shiftType = "NIGHT",
            date = "2026-10-11",
            status = "CLOSED",
            expectedRounds = 3,
            completedRounds = 2,
            pointsExpected = 12,
            pointsScanned = 9,
            goalMet = true
        )

        private fun invalidSummaryShiftType() = PatrolShiftSummaryDto(
            shiftType = "BAD",
            date = "2026-10-11",
            status = "CLOSED",
            expectedRounds = 3,
            completedRounds = 2,
            pointsExpected = 12,
            pointsScanned = 9,
            goalMet = true
        )

        private fun validDetail() = PatrolShiftDetailDto(
            status = "CLOSED",
            expectedRounds = 1,
            completedRounds = 1,
            pointsExpected = 4,
            pointsScanned = 4,
            goalMet = true,
            rounds = listOf(
                PatrolShiftRoundDto(
                    routeName = "Nocturna",
                    guard = PatrolGuardRefDto(id = "g1", name = "Carlos"),
                    scheduledAt = "2026-10-11T22:00:00-05:00",
                    startedAt = "2026-10-11T22:03:00-05:00",
                    status = "COMPLETED",
                    reason = null,
                    pointsScanned = 4,
                    pointsExpected = 4
                )
            )
        )

        private fun validGuardSummary() = PatrolGuardSummaryDto(
            guardName = "Carlos",
            roundsPerformed = 4,
            shiftsWithParticipation = 2,
            averageRoundsPerShift = 2.0,
            routes = listOf("Nocturna")
        )
    }
}
