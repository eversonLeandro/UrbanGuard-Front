package co.edu.unicauca.urbanguard.ui.features.auth

import co.edu.unicauca.urbanguard.core.push.PushRegistrar
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
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
import org.junit.Assert.assertNull
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
        val viewModel = LoginViewModel(
            LoginUseCase(FakeAuthRepository()),
            NoOpPushRegistrar
        )

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
        val viewModel = LoginViewModel(
            LoginUseCase(superadminRepo),
            NoOpPushRegistrar
        )

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
        val viewModel = LoginViewModel(
            LoginUseCase(noRoleRepo),
            NoOpPushRegistrar
        )

        viewModel.onEmailChange("nuevo@urbanguard.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.loginSuccess)
        assertEquals(UserRole.USER, viewModel.uiState.value.userRole)
    }

    @Test
    fun `login with invalid email sets emailError and does not invoke login`() = runTest {
        var loginCalled = false
        val repo = object : FakeAuthRepository() {
            override suspend fun login(email: String, password: String): Result<UserSession> {
                loginCalled = true
                return super.login(email, password)
            }
        }
        val viewModel = LoginViewModel(LoginUseCase(repo), NoOpPushRegistrar)

        viewModel.onEmailChange("correo-invalido")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()

        advanceUntilIdle()
        assertEquals("Ingresa un correo electrónico válido", viewModel.uiState.value.emailError)
        assertFalse(loginCalled)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login with blank password sets passwordError and does not invoke login`() = runTest {
        var loginCalled = false
        val repo = object : FakeAuthRepository() {
            override suspend fun login(email: String, password: String): Result<UserSession> {
                loginCalled = true
                return super.login(email, password)
            }
        }
        val viewModel = LoginViewModel(LoginUseCase(repo), NoOpPushRegistrar)

        viewModel.onEmailChange("admin@urbanguard.co")
        viewModel.onPasswordChange("   ")
        viewModel.onLoginClick()

        advanceUntilIdle()
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.passwordError)
        assertFalse(loginCalled)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login failure updates uiState with error message`() = runTest {
        val failureRepo = object : FakeAuthRepository() {
            override suspend fun login(email: String, password: String): Result<UserSession> {
                return Result.failure(
                    AppException(AppError.InvalidCredentials(AppError.InvalidCredentials.MESSAGE))
                )
            }
        }
        val viewModel = LoginViewModel(LoginUseCase(failureRepo), NoOpPushRegistrar)

        viewModel.onEmailChange("admin@urbanguard.co")
        viewModel.onPasswordChange("wrongpassword")
        viewModel.onLoginClick()

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.loginSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.userRole)
        assertEquals(AppError.InvalidCredentials.MESSAGE, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `togglePasswordVisibility toggles isPasswordVisible`() {
        val viewModel = LoginViewModel(LoginUseCase(FakeAuthRepository()), NoOpPushRegistrar)

        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    private open class FakeAuthRepository(private val role: UserRole = UserRole.ADMIN) : AuthRepository {

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

        override suspend fun requestPasswordReset(email: String): Result<Unit> = Result.success(Unit)

        override suspend fun restoreSession(): UserSession? = null

        override suspend fun logout() = Unit
    }

    private object NoOpPushRegistrar : PushRegistrar {
        override fun registerActiveConjunto() = Unit
        override fun registerForConjunto(conjuntoId: String) = Unit
        override fun onTokenRotated(token: String) = Unit
        override fun unregisterCurrent(onComplete: () -> Unit) = onComplete()
    }
}
