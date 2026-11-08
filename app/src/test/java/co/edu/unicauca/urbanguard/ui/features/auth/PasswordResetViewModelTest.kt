package co.edu.unicauca.urbanguard.ui.features.auth

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import co.edu.unicauca.urbanguard.domain.usecase.auth.RequestPasswordResetUseCase
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
class PasswordResetViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validRequestShowsGenericConfirmation() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = PasswordResetViewModel(RequestPasswordResetUseCase(repository))
        viewModel.onEmailChange(" user@example.com ")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.requestAccepted)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("user@example.com", repository.requestedEmail)
    }

    @Test
    fun invalidEmailIsRejectedWithoutRequest() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = PasswordResetViewModel(RequestPasswordResetUseCase(repository))
        viewModel.onEmailChange("not-an-email")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals("Ingresa un correo electrónico válido.", viewModel.uiState.value.emailError)
        assertEquals(null, repository.requestedEmail)
    }

    @Test
    fun rateLimitErrorIsShownAndDoesNotConfirmRequest() = runTest {
        val repository = FakeAuthRepository(
            Result.failure(AppException(AppError.RateLimited(AppError.RateLimited.MESSAGE)))
        )
        val viewModel = PasswordResetViewModel(RequestPasswordResetUseCase(repository))
        viewModel.onEmailChange("user@example.com")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AppError.RateLimited.MESSAGE, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.requestAccepted)
    }

    @Test
    fun timeoutUsesRecoverySpecificMessage() = runTest {
        val repository = FakeAuthRepository(
            Result.failure(AppException(AppError.Timeout(AppError.Timeout.MESSAGE)))
        )
        val viewModel = PasswordResetViewModel(RequestPasswordResetUseCase(repository))
        viewModel.onEmailChange("user@example.com")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals("La solicitud tardó demasiado. Intenta de nuevo.", viewModel.uiState.value.errorMessage)
    }

    private class FakeAuthRepository(
        private val resetResult: Result<Unit> = Result.success(Unit)
    ) : AuthRepository {
        var requestedEmail: String? = null

        override suspend fun login(email: String, password: String): Result<UserSession> =
            Result.failure(UnsupportedOperationException())

        override suspend fun signUp(
            email: String,
            password: String,
            name: String
        ): Result<UserSession> = Result.failure(UnsupportedOperationException())

        override suspend fun requestPasswordReset(email: String): Result<Unit> {
            requestedEmail = email
            return resetResult
        }

        override suspend fun restoreSession(): UserSession? = null

        override suspend fun logout() = Unit
    }
}
