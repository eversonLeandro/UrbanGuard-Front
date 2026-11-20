package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import co.edu.unicauca.urbanguard.ui.components.ErrorView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoundActiveScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `wrong NFC shows one explanatory dialog that can be dismissed`() {
        val repository = FakeRoundRepository()
        val session = SessionManager(NoOpRolePersistence)
        session.saveSession(UserSession(
            token = "jwt", userId = "guard1", name = "Vigilante", role = UserRole.GUARD,
            conjuntosAsociados = listOf(ConjuntoUserAssociation("c1", "Conjunto", UserRole.GUARD)),
            conjuntoActivoId = "c1", conjuntoActivoNombre = "Conjunto"
        ))
        val vm = RoundActiveViewModel(RoundActions(
            GetActiveRoundUseCase(repository), RegisterScanUseCase(repository),
            FinishRoundUseCase(repository), CloseOverdueRoundsUseCase(repository)
        ), session)

        compose.setContent { RoundActiveScreen(onFinished = {}, onNavigateBack = {}, viewModel = vm) }
        compose.waitUntil(10_000) { vm.uiState.value.round != null }
        compose.onNodeWithText("SIGUIENTE").assertIsDisplayed()

        compose.runOnIdle { vm.onTagScanned("AA:BB:CC:DD") }
        compose.waitUntil(10_000) { vm.uiState.value.showUnexpectedPointDialog }
        compose.onNodeWithText("Este no es el siguiente punto").assertIsDisplayed()
        compose.onNodeWithText("Escanea el tag del punto marcado como SIGUIENTE en la ruta.")
            .assertIsDisplayed()
        compose.onNodeWithText("Entendido").performClick()
        compose.waitUntil(10_000) { !vm.uiState.value.showUnexpectedPointDialog }
        compose.onNodeWithText("Este no es el siguiente punto").assertDoesNotExist()
        compose.onNodeWithText("SIGUIENTE").assertIsDisplayed()
    }

    @Test
    fun `typed point error has a readable fallback title`() {
        compose.setContent { ErrorView(AppError.UnexpectedPatrolPoint()) }
        compose.onNodeWithText("Punto no esperado").assertIsDisplayed()
    }

    private class FakeRoundRepository : PatrolRoundRepository {
        private val point = PatrolControlPoint(
            "p1", "c1", "Entrada", "Portería", nfcTagId = "01020304", isActive = true
        )
        private val round = PatrolRound("r1", "c1", "route1", "Ronda A", Instant.EPOCH, points = listOf(point))

        override suspend fun activeRound(complexId: String): PatrolRound = round
        override suspend fun closeOverdueRounds(complexId: String): PatrolRound? = null
        override suspend fun registerScan(complexId: String, tagId: String): Result<Pair<PatrolRound, PatrolControlPoint?>> =
            Result.failure(AppException(AppError.UnexpectedPatrolPoint()))
        override suspend fun finishRound(complexId: String): Result<PatrolRound> = Result.success(round)
        override suspend fun getRounds(complexId: String): Result<List<PatrolRound>> = Result.success(listOf(round))
        override suspend fun getFinishedRound(complexId: String, executionId: String): Result<PatrolRound> =
            Result.success(round)
        override fun lastFinishedRound(): PatrolRound? = null
        override suspend fun startRound(
            complexId: String, routeId: String, routeName: String, points: List<PatrolControlPoint>,
            scheduledAt: Instant?, dueAt: Instant?, occurrenceId: String?
        ): PatrolRound = round
    }
}
