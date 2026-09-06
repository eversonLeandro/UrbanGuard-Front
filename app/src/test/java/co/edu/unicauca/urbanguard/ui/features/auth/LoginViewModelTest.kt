package co.edu.unicauca.urbanguard.ui.features.auth

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
        val viewModel = LoginViewModel(LoginUseCase(FakeAuthRepository()))

        viewModel.onEmailChange("admin@urbanguard.co")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.loginSuccess)

        viewModel.onLoginHandled()

        assertFalse(viewModel.uiState.value.loginSuccess)
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String): Result<UserSession> {
            return Result.success(
                UserSession(
                    token = "token-123",
                    userId = "user-123",
                    name = "Admin",
                    role = UserRole.ADMIN
                )
            )
        }

        override suspend fun signUp(email: String, password: String, name: String): Result<UserSession> {
            return Result.success(
                UserSession(
                    token = "token-123",
                    userId = "user-123",
                    name = "Admin",
                    role = UserRole.ADMIN
                )
            )
        }

        override suspend fun logout() = Unit
    }
}
