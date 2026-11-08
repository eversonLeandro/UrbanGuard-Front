package co.edu.unicauca.urbanguard.ui.features.users

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import co.edu.unicauca.urbanguard.domain.usecase.users.DeactivateUserUseCase
import co.edu.unicauca.urbanguard.domain.usecase.users.ListUsersUseCase
import co.edu.unicauca.urbanguard.domain.usecase.users.ReactivateUserUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Fronteras de la pantalla de gestión de cuentas (HU-006): qué filtro viaja al
 * backend, cómo queda la fila propia sin acción de desactivación, el mensaje
 * del 409/403 del servidor intacto, y el doble toque que no dispara dos acciones.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserListViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `la carga inicial lista activos con el sub propio en el estado`() = runTest {
        val repo = FakeUserRepository(activeUsers = listOf(OTRO, ACTOR))
        val viewModel = createViewModel(repo)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UserListFilter.ACTIVE, state.filter)
        assertEquals(listOf(OTRO.userId, ACTOR.userId), state.users.map { it.userId })
        assertEquals(ACTOR.userId, state.myUserId)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `el filtro de inactivos pide estado inactivos al backend`() = runTest {
        val repo = FakeUserRepository(inactiveUsers = listOf(DEACTIVATED))
        val viewModel = createViewModel(repo)

        viewModel.onSelectFilter(UserListFilter.INACTIVE)
        advanceUntilIdle()

        assertEquals("inactivos", repo.lastEstado)
        assertEquals(listOf(DEACTIVATED.userId), viewModel.uiState.value.users.map { it.userId })
    }

    @Test
    fun `la busqueda filtra por nombre y correo sin importar mayusculas ni acentos`() = runTest {
        val repo = FakeUserRepository(activeUsers = listOf(OTRO, ACTOR))
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onQueryChange("LUCA")
        assertEquals(listOf(OTRO.userId), viewModel.uiState.value.filteredUsers.map { it.userId })

        viewModel.onQueryChange("actor@")
        assertEquals(listOf(ACTOR.userId), viewModel.uiState.value.filteredUsers.map { it.userId })
    }

    @Test
    fun `el sub propio persiste tras cambiar de filtro`() = runTest {
        val repo = FakeUserRepository(
            activeUsers = listOf(OTRO, ACTOR),
            inactiveUsers = listOf(DEACTIVATED)
        )
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onSelectFilter(UserListFilter.INACTIVE)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UserListFilter.INACTIVE, state.filter)
        assertEquals(ACTOR.userId, state.myUserId)
    }

    @Test
    fun `desactivar con exito muestra mensaje conserva el historial y recarga`() = runTest {
        val repo = FakeUserRepository(activeUsers = listOf(OTRO, ACTOR))
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onRequestDeactivate(OTRO)
        assertEquals(OTRO, viewModel.uiState.value.pendingDeactivation)

        viewModel.onConfirmDeactivate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.pendingDeactivation)
        assertNull(state.actionInProgressUserId)
        assertTrue(
            "El mensaje debe confirmar que se conservó el historial",
            state.successMessage!!.contains("historial")
        )
        assertEquals(1, repo.deactivateCalls)
        assertEquals(listOf(ACTOR.userId), state.users.map { it.userId })
    }

    @Test
    fun `un 409 de autodesactivacion conserva el mensaje del backend`() = runTest {
        val repo = FakeUserRepository(
            activeUsers = listOf(OTRO, ACTOR),
            deactivateError = AppException(
                AppError.Conflict(
                    "No puedes desactivar tu propia cuenta; solicita a otro superadministrador que lo haga"
                )
            )
        )
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onRequestDeactivate(OTRO)
        viewModel.onConfirmDeactivate()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(
            state.errorMessage!!.contains("No puedes desactivar tu propia cuenta")
        )
        assertNull(state.successMessage)
        assertEquals(1, repo.deactivateCalls)
    }

    @Test
    fun `reactivar con exito saca la cuenta del filtro de inactivos`() = runTest {
        val repo = FakeUserRepository(inactiveUsers = listOf(DEACTIVATED))
        val viewModel = superadminViewModel(repo, filter = UserListFilter.INACTIVE)
        advanceUntilIdle()

        viewModel.onReactivate(DEACTIVATED)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Acceso restaurado.", state.successMessage)
        assertTrue(state.users.map { it.userId }.isEmpty())
        assertEquals(1, repo.reactivateCalls)
    }

    @Test
    fun `un fallo de sesion con 403 se muestra tal cual y no rompe la lista`() = runTest {
        val repo = FakeUserRepository(
            listError = AppException(AppError.Forbidden("Acceso denegado para el recurso solicitado"))
        )
        val viewModel = createViewModel(repo)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Acceso denegado para el recurso solicitado", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `el doble toque no dispara dos desactivaciones`() = runTest {
        val repo = FakeUserRepository(activeUsers = listOf(OTRO, ACTOR))
        val viewModel = createViewModel(repo)
        advanceUntilIdle()

        viewModel.onRequestDeactivate(OTRO)
        viewModel.onConfirmDeactivate()
        // Segundo toque mientras la primera sigue en vuelo (sin advanceUntilIdle).
        viewModel.onConfirmDeactivate()
        advanceUntilIdle()

        assertEquals(1, repo.deactivateCalls)
    }

    private fun createViewModel(repository: UserRepository) =
        UserListViewModel(
            listUsers = ListUsersUseCase(repository),
            deactivateUser = DeactivateUserUseCase(repository),
            reactivateUser = ReactivateUserUseCase(repository),
            sessionManager = sessionWithSubject(ACTOR.userId)
        )

    private fun superadminViewModel(
        repository: UserRepository,
        filter: UserListFilter
    ): UserListViewModel {
        val viewModel = createViewModel(repository)
        viewModel.onSelectFilter(filter)
        return viewModel
    }

    private fun sessionWithSubject(sub: String): SessionManager {
        val manager = SessionManager(NoOpRolePersistence)
        manager.saveSession(
            UserSession(
                token = "token",
                userId = sub,
                name = "Super",
                role = UserRole.SUPERADMIN
            )
        )
        return manager
    }

    private class FakeUserRepository(
        activeUsers: List<User> = emptyList(),
        inactiveUsers: List<User> = emptyList(),
        private val listError: Exception? = null,
        private val deactivateError: Exception? = null,
        private val reactivateError: Exception? = null
    ) : UserRepository {
        override suspend fun getOwnProfile() =
            Result.failure<co.edu.unicauca.urbanguard.domain.model.OwnProfile>(UnsupportedOperationException())
        override suspend fun updateProfilePhoto(image: okhttp3.MultipartBody.Part, token: String) =
            Result.failure<co.edu.unicauca.urbanguard.domain.model.OwnProfile>(UnsupportedOperationException())

        // Estado mutable: refleja el comportamiento real del backend, donde
        // desactivar mueve la cuenta a inactivos y reactivar la devuelve.
        private val active = activeUsers.toMutableList()
        private val inactive = inactiveUsers.toMutableList()

        var lastEstado: String? = null
            private set
        var deactivateCalls: Int = 0
            private set
        var reactivateCalls: Int = 0
            private set

        override suspend fun listUsers(estado: String): Result<List<User>> {
            lastEstado = estado
            if (listError != null) return Result.failure(listError)
            return when (estado) {
                UserRepository.ESTADO_INACTIVOS -> Result.success(inactive.toList())
                UserRepository.ESTADO_TODOS -> Result.success(inactive + active)
                else -> Result.success(active.toList())
            }
        }

        override suspend fun deactivateUser(userId: String): Result<Unit> {
            deactivateCalls++
            if (deactivateError != null) return Result.failure(deactivateError)
            val target = active.firstOrNull { it.userId == userId }
                ?: throw java.lang.IllegalStateException("La cuenta ya estaba inactiva")
            active.removeAll { it.userId == userId }
            inactive.add(target.copy(activo = false))
            return Result.success(Unit)
        }

        override suspend fun reactivateUser(userId: String): Result<User> {
            reactivateCalls++
            if (reactivateError != null) return Result.failure(reactivateError)
            val target = inactive.firstOrNull { it.userId == userId }
                ?: throw java.lang.IllegalStateException("La cuenta ya está activa")
            inactive.removeAll { it.userId == userId }
            active.add(target.copy(activo = true))
            return Result.success(target.copy(activo = true))
        }

        override suspend fun createPasswordChangeTicket(): Result<String> =
            Result.success("https://urbanguard-test.example/password-change")
    }

    private companion object {
        val ACTOR = User(
            publicId = "6f1c9a70-0001-4000-8000-000000000001",
            userId = "auth0|super-actor",
            email = "actor@urbanguard.com",
            nombres = "Lucía",
            apellidos = "Ávila",
            activo = true,
            createdAt = null,
            updatedAt = null
        )
        val OTRO = User(
            publicId = "6f1c9a70-0001-4000-8000-000000000002",
            userId = "auth0|residente",
            email = "residente@urbanguard.com",
            nombres = "Camilo Lucas",
            apellidos = "Ruiz",
            activo = true,
            createdAt = null,
            updatedAt = null
        )
        val DEACTIVATED = OTRO.copy(activo = false)
    }
}
