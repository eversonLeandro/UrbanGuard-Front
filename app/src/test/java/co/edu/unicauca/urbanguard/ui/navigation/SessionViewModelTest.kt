package co.edu.unicauca.urbanguard.ui.navigation

import co.edu.unicauca.urbanguard.core.push.PushRegistrar
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import co.edu.unicauca.urbanguard.domain.usecase.auth.LogoutUseCase
import co.edu.unicauca.urbanguard.domain.usecase.auth.RestoreSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialRoute with null session returns LOGIN destination`() = runTest {
        val repo = FakeAuthRepository(restoredSession = null)
        val pushRegistrar = FakePushRegistrar()
        val viewModel = SessionViewModel(
            RestoreSessionUseCase(repo),
            LogoutUseCase(repo),
            pushRegistrar
        )

        val destination = viewModel.initialRoute()

        assertEquals(Routes.LOGIN, destination)
        assertEquals(0, pushRegistrar.registerActiveConjuntoCallCount)
    }

    @Test
    fun `initialRoute with valid session registers push and returns role destination`() = runTest {
        val session = UserSession("token", "user1", "Admin", UserRole.ADMIN)
        val repo = FakeAuthRepository(restoredSession = session)
        val pushRegistrar = FakePushRegistrar()
        val viewModel = SessionViewModel(
            RestoreSessionUseCase(repo),
            LogoutUseCase(repo),
            pushRegistrar
        )

        val destination = viewModel.initialRoute()

        assertEquals(Routes.HOME, destination)
        assertEquals(1, pushRegistrar.registerActiveConjuntoCallCount)
    }

    @Test
    fun `logout unregisters push token and delegates to LogoutUseCase before completing`() = runTest {
        val repo = FakeAuthRepository()
        val pushRegistrar = FakePushRegistrar()
        val viewModel = SessionViewModel(
            RestoreSessionUseCase(repo),
            LogoutUseCase(repo),
            pushRegistrar
        )

        var completed = false
        viewModel.logout {
            completed = true
        }

        advanceUntilIdle()

        assertEquals(1, pushRegistrar.unregisterCurrentCallCount)
        assertEquals(1, repo.logoutCallCount)
        assertTrue(completed)
    }

    private class FakeAuthRepository(
        private val restoredSession: UserSession? = null
    ) : AuthRepository {
        var logoutCallCount = 0

        override suspend fun login(email: String, password: String): Result<UserSession> =
            Result.failure(UnsupportedOperationException())

        override suspend fun signUp(email: String, password: String, name: String): Result<UserSession> =
            Result.failure(UnsupportedOperationException())

        override suspend fun requestPasswordReset(email: String): Result<Unit> = Result.success(Unit)

        override suspend fun restoreSession(): UserSession? = restoredSession

        override suspend fun logout() {
            logoutCallCount++
        }
    }

    private class FakePushRegistrar : PushRegistrar {
        var registerActiveConjuntoCallCount = 0
        var unregisterCurrentCallCount = 0

        override fun registerActiveConjunto() {
            registerActiveConjuntoCallCount++
        }

        override fun registerForConjunto(conjuntoId: String) = Unit
        override fun onTokenRotated(token: String) = Unit

        override fun unregisterCurrent(onComplete: () -> Unit) {
            unregisterCurrentCallCount++
            onComplete()
        }
    }
}
