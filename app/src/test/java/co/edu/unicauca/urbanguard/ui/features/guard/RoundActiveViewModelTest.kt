package co.edu.unicauca.urbanguard.ui.features.guard

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import co.edu.unicauca.urbanguard.domain.usecase.patrol.CloseOverdueRoundsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.FinishRoundUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetActiveRoundUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.RegisterScanUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.RoundActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class RoundActiveViewModelTest {
    private val point = PatrolControlPoint("p1", "c1", "Entrada", "Portería", nfcTagId = "01020304", isActive = true)
    private val round = PatrolRound("r1", "c1", "route1", "Ronda A", Instant.EPOCH, points = listOf(point))

    @Before fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `rejected point opens one dialog and resumes scanning after dismissal`() = runTest {
        val repository = FakePatrolRepository(round)
        val viewModel = viewModel(repository)
        viewModel.onRefresh()
        advanceUntilIdle()

        viewModel.onTagScanned("AA:BB:CC:DD")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showUnexpectedPointDialog)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(1, repository.scanCalls)

        viewModel.onTagScanned("11:22:33:44")
        advanceUntilIdle()
        assertEquals(1, repository.scanCalls)

        viewModel.onDismissUnexpectedPointDialog()
        repository.scanResult = Result.success(round to point)
        viewModel.onTagScanned("01:02:03:04")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showUnexpectedPointDialog)
        assertEquals("Entrada", viewModel.uiState.value.lastScanName)
        assertEquals(2, repository.scanCalls)
    }

    @Test
    fun `other scan failure keeps a readable inline message`() = runTest {
        val repository = FakePatrolRepository(round)
        repository.scanResult = Result.failure(AppException(AppError.NoConnection(AppError.NoConnection.MESSAGE)))
        val viewModel = viewModel(repository)
        viewModel.onRefresh()
        advanceUntilIdle()

        viewModel.onTagScanned("AA:BB:CC:DD")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showUnexpectedPointDialog)
        assertEquals(AppError.NoConnection.MESSAGE, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `second tag is ignored while first scan is pending`() = runTest {
        val repository = FakePatrolRepository(round)
        repository.scanGate = CompletableDeferred()
        val viewModel = viewModel(repository)
        viewModel.onRefresh()
        advanceUntilIdle()

        viewModel.onTagScanned("AA:BB:CC:DD")
        viewModel.onTagScanned("11:22:33:44")
        advanceUntilIdle()
        assertEquals(1, repository.scanCalls)

        repository.scanGate!!.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showUnexpectedPointDialog)
    }

    @Test
    fun `empty and repeated NFC reads do not create extra scan requests`() = runTest {
        val repository = FakePatrolRepository(round)
        repository.scanResult = Result.success(round to point)
        val viewModel = viewModel(repository)
        viewModel.onRefresh()
        advanceUntilIdle()

        viewModel.onTagScanned("  ")
        advanceUntilIdle()
        assertEquals(0, repository.scanCalls)

        viewModel.onTagScanned("01:02:03:04")
        advanceUntilIdle()
        viewModel.onTagScanned("01-02-03-04")
        advanceUntilIdle()
        assertEquals(1, repository.scanCalls)
    }

    private fun viewModel(repository: PatrolRoundRepository): RoundActiveViewModel {
        val session = SessionManager(NoOpRolePersistence)
        session.saveSession(UserSession(
            token = "jwt", userId = "guard1", name = "Vigilante", role = UserRole.GUARD,
            conjuntosAsociados = listOf(ConjuntoUserAssociation("c1", "Conjunto", UserRole.GUARD)),
            conjuntoActivoId = "c1", conjuntoActivoNombre = "Conjunto"
        ))
        return RoundActiveViewModel(
            RoundActions(
                GetActiveRoundUseCase(repository), RegisterScanUseCase(repository),
                FinishRoundUseCase(repository), CloseOverdueRoundsUseCase(repository)
            ),
            session
        )
    }

    private class FakePatrolRepository(private val round: PatrolRound) : PatrolRoundRepository {
        var scanCalls = 0
        var scanGate: CompletableDeferred<Unit>? = null
        var scanResult: Result<Pair<PatrolRound, PatrolControlPoint?>> =
            Result.failure(AppException(AppError.UnexpectedPatrolPoint()))

        override suspend fun activeRound(complexId: String): PatrolRound = round
        override suspend fun closeOverdueRounds(complexId: String): PatrolRound? = null
        override suspend fun registerScan(complexId: String, tagId: String): Result<Pair<PatrolRound, PatrolControlPoint?>> {
            scanCalls++
            scanGate?.await()
            return scanResult
        }
        override suspend fun finishRound(complexId: String): Result<PatrolRound> = Result.success(round)
        override suspend fun getRounds(complexId: String): Result<List<PatrolRound>> = Result.success(listOf(round))
        override fun lastFinishedRound(): PatrolRound? = null
        override suspend fun startRound(
            complexId: String, routeId: String, routeName: String, points: List<PatrolControlPoint>,
            scheduledAt: Instant?, dueAt: Instant?, occurrenceId: String?
        ): PatrolRound = round
    }
}
