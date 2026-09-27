package co.edu.unicauca.urbanguard.ui.features.home

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows the real conjunto name after a cold start`() = runTest {
        // Regresión del bug reportado: al reabrir la app, la lista de conjuntos aún no
        // está cargada (solo vive en memoria y llega por red) pero el id y el nombre sí
        // se restauran del disco. Antes se pintaba un conjunto inventado.
        val sessionManager = SessionManager(NoOpRolePersistence)
        sessionManager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|1",
                name = "Ana Pérez",
                role = UserRole.ADMIN,
                conjuntosAsociados = emptyList(),
                conjuntoActivoId = "c1",
                conjuntoActivoNombre = "Torres del Prado"
            )
        )

        val viewModel = HomeViewModel(sessionManager)
        advanceUntilIdle()

        assertEquals("Torres del Prado", viewModel.uiState.value.conjuntoName)
        assertEquals("Ana Pérez", viewModel.uiState.value.userName)
        assertEquals("ADMINISTRADOR", viewModel.uiState.value.userRole)
        assertEquals(UserRole.ADMIN, viewModel.uiState.value.role)
    }

    @Test
    fun `never invents a conjunto name when there is none`() = runTest {
        val sessionManager = SessionManager(NoOpRolePersistence)
        sessionManager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|1",
                name = "Ana",
                role = UserRole.USER
            )
        )

        val viewModel = HomeViewModel(sessionManager)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.conjuntoName)
    }

    @Test
    fun `updates the conjunto when the session changes`() = runTest {
        val sessionManager = SessionManager(NoOpRolePersistence)
        sessionManager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|1",
                name = "Ana",
                role = UserRole.USER,
                conjuntosAsociados = listOf(
                    ConjuntoUserAssociation("c1", "El Pinar", UserRole.RESIDENT),
                    ConjuntoUserAssociation("c2", "La Aurora", UserRole.ADMIN)
                )
            )
        )
        val viewModel = HomeViewModel(sessionManager)
        advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.conjuntoName)

        // Cambia de conjunto desde Inicio
        sessionManager.setActiveConjunto("c2")
        advanceUntilIdle()

        assertEquals("La Aurora", viewModel.uiState.value.conjuntoName)
        assertEquals(UserRole.ADMIN, viewModel.uiState.value.role)
    }

    @Test
    fun `refreshes the name when the real associations arrive`() = runTest {
        val sessionManager = SessionManager(NoOpRolePersistence)
        sessionManager.saveSession(
            UserSession(
                token = "jwt_token",
                userId = "auth0|1",
                name = "Ana",
                role = UserRole.RESIDENT,
                conjuntoActivoId = "c1",
                conjuntoActivoNombre = "Nombre Cacheado"
            )
        )
        val viewModel = HomeViewModel(sessionManager)
        advanceUntilIdle()
        assertEquals("Nombre Cacheado", viewModel.uiState.value.conjuntoName)

        // Llega la lista autoritativa del backend
        sessionManager.setAssociations(
            listOf(ConjuntoUserAssociation("c1", "Torres del Prado", UserRole.RESIDENT))
        )
        advanceUntilIdle()

        assertEquals("Torres del Prado", viewModel.uiState.value.conjuntoName)
        assertEquals("c1", sessionManager.session.value?.conjuntoActivoId)
    }
}
