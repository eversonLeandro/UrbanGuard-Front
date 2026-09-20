package co.edu.unicauca.urbanguard.ui.features.auth

import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import co.edu.unicauca.urbanguard.domain.usecase.auth.LoginUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onLoginHandled clears the success flag after a successful login`() = runTest {
        val viewModel = LoginViewModel(LoginUseCase(FakeAuthRepository()), SessionManager())

        viewModel.onEmailChange("admin@urbanguard.co")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.loginSuccess)

        viewModel.onLoginHandled()

        assertFalse(viewModel.uiState.value.loginSuccess)
    }

    @Test
    fun `login with superadmin returns SUPERADMIN userRole in uiState`() = runTest {
        val superadminRepo = FakeAuthRepository(role = UserRole.SUPERADMIN)
        val viewModel = LoginViewModel(LoginUseCase(superadminRepo), SessionManager())

        viewModel.onEmailChange("superadmin@urbanguard.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.loginSuccess)
        assertEquals(UserRole.SUPERADMIN, viewModel.uiState.value.userRole)
    }

    /**
     * Cuando Auth0 retorna staff=false y sin claim rol (usuario nuevo sin conjunto),
     * el sistema asigna el centinela [UserRole.USER] — el NavGraph lo dirige a
     * ConjuntoSelectionScreen. USER no es un rol de negocio.
     */
    @Test
    fun `login with no business role returns USER sentinel in uiState`() = runTest {
        val noRoleRepo = FakeAuthRepository(role = UserRole.USER)
        val viewModel = LoginViewModel(LoginUseCase(noRoleRepo), SessionManager())

        viewModel.onEmailChange("nuevo@urbanguard.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.loginSuccess)
        assertEquals(UserRole.USER, viewModel.uiState.value.userRole)
    }

    private class FakeAuthRepository(private val role: UserRole = UserRole.ADMIN) : AuthRepository {
        override suspend fun login(email: String, password: String): Result<UserSession> {
            return Result.success(
                UserSession(
                    token = "token-123",
                    userId = "user-123",
                    name = "User",
                    role = role
                )
            )
        }

        override suspend fun signUp(email: String, password: String, name: String): Result<UserSession> {
            return Result.success(
                UserSession(
                    token = "token-123",
                    userId = "user-123",
                    name = "User",
                    role = role
                )
            )
        }

        override suspend fun logout() = Unit
    }
}
