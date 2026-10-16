package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.PatrolGuardSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftDetail
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.PatrolShiftRepository
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * El ViewModel del historial de turnos solo orquesta: resuelve conjunto activo,
 * valida el rango (sin alterarlo) y delega al caso de uso. Estos tests fijan ese
 * comportamiento sin depender del backend, con un reloj fijo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PatrolHistoryViewModelTest {

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
    fun `una consulta valida carga los turnos`() = runTest {
        val repo = FakePatrolShiftRepository(summaries = listOf(sampleSummary()))
        val viewModel = createViewModel(repo)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(!state.isLoading)
        assertEquals(1, state.shifts.size)
        assertNull(state.errorMessage)
        assertTrue(state.isEmpty.not())
    }

    @Test
    fun `al cambiar a solo incumplimientos se envian soloIncomplete true`() = runTest {
        val repo = FakePatrolShiftRepository(summaries = listOf(sampleSummary()))
        val viewModel = createViewModel(repo)

        advanceUntilIdle()
        viewModel.onOnlyIncompleteChange(true)
        advanceUntilIdle()

        assertEquals(true, repo.lastOnlyIncomplete)
        assertEquals(2, repo.calls)
    }

    @Test
    fun `un rango valido recarga los turnos`() = runTest {
        val repo = FakePatrolShiftRepository(summaries = listOf(sampleSummary()))
        val viewModel = createViewModel(repo)

        advanceUntilIdle()
        viewModel.onFromSelected(LocalDate.of(2026, 10, 10))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.rangeError)
        assertEquals(LocalDate.of(2026, 10, 10), state.from)
        assertEquals(2, repo.calls)
    }

    @Test
    fun `desde posterior a hasta no consulta y muestra validacion`() = runTest {
        val repo = FakePatrolShiftRepository(summaries = listOf(sampleSummary()))
        val viewModel = createViewModel(repo)

        advanceUntilIdle()
        viewModel.onFromSelected(LocalDate.of(2026, 10, 20))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PatrolHistoryConstants.MSG_RANGE_INVALID, state.rangeError)
        assertEquals(LocalDate.of(2026, 10, 20), state.from)
        assertEquals(1, repo.calls)
        assertTrue(state.shifts.isEmpty())
    }

    @Test
    fun `un rango de mas de 31 dias no consulta y muestra validacion`() = runTest {
        val repo = FakePatrolShiftRepository(summaries = listOf(sampleSummary()))
        val viewModel = createViewModel(repo)

        advanceUntilIdle()
        viewModel.onFromSelected(LocalDate.of(2025, 1, 1))
        viewModel.onToSelected(LocalDate.of(2025, 2, 15))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PatrolHistoryConstants.MSG_RANGE_TOO_LONG, state.rangeError)
        assertEquals(1, repo.calls)
    }

    @Test
    fun `sin conjunto activo muestra error y no llama al repo`() = runTest {
        val repo = FakePatrolShiftRepository(summaries = listOf(sampleSummary()))
        val viewModel = createViewModel(repo, sessionManager = SessionManager(NoOpRolePersistence))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX, state.errorMessage)
        assertEquals(0, repo.calls)
    }

    @Test
    fun `un 403 del backend se muestra tal cual`() = runTest {
        val repo = FakePatrolShiftRepository(
            error = AppException(AppError.Forbidden("Acceso denegado"))
        )
        val viewModel = createViewModel(repo)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Acceso denegado", state.errorMessage)
        assertTrue(state.shifts.isEmpty())
    }

    private fun sampleSummary() = PatrolShiftSummary(
        shiftType = PatrolShiftType.NIGHT,
        date = LocalDate.of(2026, 10, 11),
        status = PatrolShiftStatus.CLOSED,
        expectedRounds = 3,
        completedRounds = 2,
        pointsExpected = 12,
        pointsScanned = 9,
        goalMet = true
    )

    private fun createViewModel(
        repository: PatrolShiftRepository,
        sessionManager: SessionManager = sessionWithComplex()
    ) = PatrolHistoryViewModel(
        getPatrolHistoryUseCase = GetPatrolHistoryUseCase(repository),
        sessionManager = sessionManager,
        clock = clock
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
        private val summaries: List<PatrolShiftSummary> = emptyList(),
        private val error: Exception? = null
    ) : PatrolShiftRepository {

        var calls = 0
            private set
        var lastOnlyIncomplete: Boolean? = null
            private set

        override suspend fun getShiftSummaries(
            complexId: String,
            from: LocalDate,
            to: LocalDate,
            guardId: String?,
            onlyIncomplete: Boolean
        ): Result<List<PatrolShiftSummary>> {
            calls++
            lastOnlyIncomplete = onlyIncomplete
            return if (error != null) Result.failure(error) else Result.success(summaries)
        }

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
        ): Result<PatrolGuardSummary> = Result.failure(IllegalStateException("not used"))
    }

    private companion object {
        const val COMPLEX_ID = "6f1c9a70-0000-4000-8000-000000000001"
    }
}
