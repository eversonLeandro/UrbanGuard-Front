package co.edu.unicauca.urbanguard.ui.features.guard

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoAssociationRepository
import co.edu.unicauca.urbanguard.domain.usecase.onboarding.GetMyConjuntosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuardHomeViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `activates the guard conjunto and shows its real name`() = runTest {
        val sessionManager = SessionManager(NoOpRolePersistence)
        sessionManager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|1",
                name = "Luis Torres",
                role = UserRole.GUARD
            )
        )
        val useCase = GetMyConjuntosUseCase(
            FakeConjuntoAssociationRepository(
                listOf(ConjuntoUserAssociation("c1", "Torres del Prado", UserRole.GUARD))
            )
        )

        val viewModel = GuardHomeViewModel(sessionManager, useCase)
        advanceUntilIdle()

        assertEquals("c1", sessionManager.session.value?.conjuntoActivoId)
        assertEquals("Torres del Prado", viewModel.uiState.value.conjuntoName)
        assertEquals("Luis Torres", viewModel.uiState.value.userName)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `keeps the conjunto the guard already had`() = runTest {
        val sessionManager = SessionManager(NoOpRolePersistence)
        sessionManager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|1",
                name = "Luis Torres",
                role = UserRole.GUARD,
                conjuntosAsociados = listOf(
                    ConjuntoUserAssociation("c1", "El Pinar", UserRole.GUARD)
                ),
                conjuntoActivoId = "c1",
                conjuntoActivoNombre = "El Pinar"
            )
        )
        val useCase = GetMyConjuntosUseCase(
            FakeConjuntoAssociationRepository(
                listOf(ConjuntoUserAssociation("c1", "El Pinar", UserRole.GUARD))
            )
        )

        val viewModel = GuardHomeViewModel(sessionManager, useCase)
        advanceUntilIdle()

        assertEquals("c1", sessionManager.session.value?.conjuntoActivoId)
        assertEquals("El Pinar", viewModel.uiState.value.conjuntoName)
    }

    @Test
    fun `does not invent a conjunto when the request fails`() = runTest {
        val sessionManager = SessionManager(NoOpRolePersistence)
        sessionManager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|1",
                name = "Luis Torres",
                role = UserRole.GUARD
            )
        )
        val useCase = GetMyConjuntosUseCase(FakeConjuntoAssociationRepository(failure = true))

        val viewModel = GuardHomeViewModel(sessionManager, useCase)
        advanceUntilIdle()

        // Sin datos reales la pantalla queda honesta: nombre de la sesion y nada más
        assertEquals("Luis Torres", viewModel.uiState.value.userName)
        assertEquals("", viewModel.uiState.value.conjuntoName)
        assertEquals(null, sessionManager.session.value?.conjuntoActivoId)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private class FakeConjuntoAssociationRepository(
        private val conjuntos: List<ConjuntoUserAssociation> = emptyList(),
        private val failure: Boolean = false
    ) : ConjuntoAssociationRepository {
        override suspend fun getMyConjuntos(): Result<List<ConjuntoUserAssociation>> =
            if (failure) Result.failure(RuntimeException("sin red")) else Result.success(conjuntos)
    }
}
