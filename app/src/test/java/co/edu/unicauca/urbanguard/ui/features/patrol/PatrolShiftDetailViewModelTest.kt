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
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.PatrolShiftRepository
import co.edu.unicauca.urbanguard.domain.usecase.patrol.CheckShiftComplianceUseCase
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
 * El ViewModel del detalle pide el cumplimiento al backend. Si los argumentos de
 * navegación faltan o son inválidos, no consulta y muestra el error.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PatrolShiftDetailViewModelTest {

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
    fun `con argumentos validos carga el detalle`() = runTest {
        val repo = FakePatrolShiftRepository(detail = sampleDetail())
        val viewModel = createViewModel(repo, args("NIGHT", "2026-10-11"))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.detail)
        assertEquals(PatrolShiftType.NIGHT, state.shiftType)
        assertEquals(LocalDate.of(2026, 10, 11), state.date)
        assertEquals(1, repo.detailCalls)
    }

    @Test
    fun `con tipo invalido no consulta y muestra error de argumentos`() = runTest {
        val repo = FakePatrolShiftRepository(detail = sampleDetail())
        val viewModel = createViewModel(repo, args("DAWN", "2026-10-11"))

        advanceUntilIdle()

        assertEquals(PatrolHistoryConstants.MSG_DETAIL_ARGS, viewModel.uiState.value.errorMessage)
        assertEquals(0, repo.detailCalls)
    }

    @Test
    fun `sin fecha no consulta y muestra error de argumentos`() = runTest {
        val repo = FakePatrolShiftRepository(detail = sampleDetail())
        val viewModel = createViewModel(repo, args("NIGHT", null))

        advanceUntilIdle()

        assertEquals(PatrolHistoryConstants.MSG_DETAIL_ARGS, viewModel.uiState.value.errorMessage)
        assertEquals(0, repo.detailCalls)
    }

    @Test
    fun `sin conjunto activo muestra error y no consulta`() = runTest {
        val repo = FakePatrolShiftRepository(detail = sampleDetail())
        val viewModel = createViewModel(
            repo,
            args("NIGHT", "2026-10-11"),
            sessionManager = SessionManager(NoOpRolePersistence)
        )

        advanceUntilIdle()

        assertEquals(
            PatrolHistoryConstants.MSG_NO_ACTIVE_COMPLEX,
            viewModel.uiState.value.errorMessage
        )
        assertEquals(0, repo.detailCalls)
    }

    @Test
    fun `un error del backend se muestra tal cual`() = runTest {
        val repo = FakePatrolShiftRepository(
            error = AppException(AppError.Forbidden("Acceso denegado"))
        )
        val viewModel = createViewModel(repo, args("NIGHT", "2026-10-11"))

        advanceUntilIdle()

        assertEquals("Acceso denegado", viewModel.uiState.value.errorMessage)
    }

    private fun args(shiftType: String?, date: String?): SavedStateHandle {
        val map = mutableMapOf<String, Any>()
        shiftType?.let { map[ARG_SHIFT_TYPE] = it }
        date?.let { map[ARG_DATE] = it }
        return SavedStateHandle(map)
    }

    private fun createViewModel(
        repository: PatrolShiftRepository,
        handle: SavedStateHandle,
        sessionManager: SessionManager = sessionWithComplex()
    ) = PatrolShiftDetailViewModel(
        savedStateHandle = handle,
        checkShiftComplianceUseCase = CheckShiftComplianceUseCase(repository),
        sessionManager = sessionManager,
        clock = clock
    )

    private fun sampleDetail() = PatrolShiftDetail(
        status = PatrolShiftStatus.CLOSED,
        expectedRounds = 3,
        completedRounds = 2,
        pointsExpected = 12,
        pointsScanned = 9,
        goalMet = true,
        rounds = emptyList()
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
        private val detail: PatrolShiftDetail? = null,
        private val error: Exception? = null
    ) : PatrolShiftRepository {

        var detailCalls = 0
            private set

        override suspend fun getShiftSummaries(
            complexId: String,
            from: LocalDate,
            to: LocalDate,
            guardId: String?,
            onlyIncomplete: Boolean
        ): Result<List<PatrolShiftSummary>> = Result.success(emptyList())

        override suspend fun getShiftDetail(
            complexId: String,
            shiftType: PatrolShiftType,
            date: LocalDate
        ): Result<PatrolShiftDetail> {
            detailCalls++
            return when {
                error != null -> Result.failure(error)
                detail != null -> Result.success(detail)
                else -> Result.failure(IllegalStateException("sin detalle"))
            }
        }

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
