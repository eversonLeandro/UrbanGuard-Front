package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.lifecycle.SavedStateHandle
import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.PatrolGuardSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftDetail
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.PatrolShiftRepository
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolGuardSummaryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * El ViewModel del resumen pide los agregados al backend. Si faltan el vigilante
 * o el conjunto activo, no consulta y muestra el error correspondiente.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PatrolGuardSummaryViewModelTest {

    private val clock: Clock =
        Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `con argumentos validos carga el resumen`() = runTest {
        val repo = FakePatrolShiftRepository(summary = sampleSummary())
        val viewModel = createViewModel(repo, args("g1", "2026-10-08", "2026-10-14"))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.summary)
        assertEquals("Carlos Pérez", state.summary?.guardName)
        assertEquals(1, repo.guardCalls)
        assertEquals("g1", repo.lastGuardId)
    }

    @Test
    fun `sin vigilante no consulta y muestra error de argumentos`() = runTest {
        val repo = FakePatrolShiftRepository(summary = sampleSummary())
        val viewModel = createViewModel(repo, args("", "2026-10-08", "2026-10-14"))

        advanceUntilIdle()

        assertEquals(
            PatrolHistoryConstants.MSG_GUARD_SUMMARY_ARGS,
            viewModel.uiState.value.errorMessage
        )
        assertEquals(0, repo.guardCalls)
    }

    @Test
    fun `sin conjunto activo muestra error y no consulta`() = runTest {
        val repo = FakePatrolShiftRepository(summary = sampleSummary())
        val viewModel = createViewModel(
            repo,
            args("g1", "2026-10-08", "2026-10-14"),
            sessionManager = SessionManager(NoOpRolePersistence)
        )

        advanceUntilIdle()

        assertEquals(
            PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX,
            viewModel.uiState.value.errorMessage
        )
        assertEquals(0, repo.guardCalls)
    }

    @Test
    fun `un 404 del backend se muestra con el mensaje de vigilante`() = runTest {
        val repo = FakePatrolShiftRepository(
            error = AppException(
                AppError.NotFound(PatrolHistoryConstants.MSG_GUARD_NOT_FOUND)
            )
        )
        val viewModel = createViewModel(repo, args("g1", "2026-10-08", "2026-10-14"))

        advanceUntilIdle()

        assertEquals(
            PatrolHistoryConstants.MSG_GUARD_NOT_FOUND,
            viewModel.uiState.value.errorMessage
        )
    }

    private fun args(guardId: String?, from: String?, to: String?): SavedStateHandle {
        val map = mutableMapOf<String, Any>()
        guardId?.let { map[ARG_GUARD_ID] = it }
        from?.let { map[ARG_FROM] = it }
        to?.let { map[ARG_TO] = it }
        return SavedStateHandle(map)
    }

    private fun createViewModel(
        repository: PatrolShiftRepository,
        handle: SavedStateHandle,
        sessionManager: SessionManager = sessionWithComplex()
    ) = PatrolGuardSummaryViewModel(
        savedStateHandle = handle,
        getPatrolGuardSummaryUseCase = GetPatrolGuardSummaryUseCase(repository),
        sessionManager = sessionManager,
        clock = clock
    )

    private fun sampleSummary() = PatrolGuardSummary(
        guardName = "Carlos Pérez",
        roundsPerformed = 8,
        shiftsWithParticipation = 3,
        averageRoundsPerShift = 2.67,
        routes = listOf("Nocturna", "Perimetral")
    )

    private fun sessionWithComplex(): SessionManager {
        val manager = SessionManager(NoOpRolePersistence)
        manager.saveSession(
            UserSession(
                token = "token",
                userId = "admin-1",
                name = "Admin",
                role = UserRole.ADMIN,
                conjuntosAsociados = listOf(
                    ConjuntoUserAssociation(
                        conjuntoId = COMPLEX_ID,
                        nombre = "Residencial",
                        roleEnConjunto = UserRole.ADMIN
                    )
                ),
                conjuntoActivoId = COMPLEX_ID
            )
        )
        return manager
    }

    private class FakePatrolShiftRepository(
        private val summary: PatrolGuardSummary? = null,
        private val error: Exception? = null
    ) : PatrolShiftRepository {

        var guardCalls = 0
            private set
        var lastGuardId: String? = null
            private set

        override suspend fun getShiftSummaries(
            complexId: String,
            from: LocalDate,
            to: LocalDate,
            guardId: String?,
            onlyIncomplete: Boolean,
            status: co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus?
        ): Result<List<PatrolShiftSummary>> = Result.success(emptyList())

        override suspend fun getShiftDetail(
            complexId: String,
            shiftType: PatrolShiftType,
            date: LocalDate
        ): Result<PatrolShiftDetail> = Result.failure(IllegalStateException("not used"))

        override suspend fun getGuardSummary(
            complexId: String,
            guardId: String,
            from: LocalDate,
            to: LocalDate
        ): Result<PatrolGuardSummary> {
            guardCalls++
            lastGuardId = guardId
            return when {
                error != null -> Result.failure(error)
                summary != null -> Result.success(summary)
                else -> Result.failure(IllegalStateException("sin resumen"))
            }
        }
    }

    private companion object {
        const val COMPLEX_ID = "6f1c9a70-0000-4000-8000-000000000001"
    }
}
