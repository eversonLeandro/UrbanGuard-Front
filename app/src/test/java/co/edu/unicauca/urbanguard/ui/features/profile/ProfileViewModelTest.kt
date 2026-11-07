package co.edu.unicauca.urbanguard.ui.features.profile

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession

import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.repository.ReauthLoginProvider
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import co.edu.unicauca.urbanguard.domain.usecase.users.ChangePasswordUseCase
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
 * Flujo de cambio de contraseña: ticket directo, reautenticación cuando el
 * backend responde REAUTH_REQUIRED y reintento automático tras el re-login.
 * La URL del ticket nunca se transforma en el ViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `requestPasswordChange stores the ticket url on success`() = runTest {
        val viewModel = viewModelWith(ticketResult = Result.success("https://ticket/abc"))

        viewModel.requestPasswordChange()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRequestingTicket)
        assertEquals("https://ticket/abc", viewModel.uiState.value.ticketUrl)
        assertNull(viewModel.uiState.value.passwordChangeError)
    }

    @Test
    fun `requestPasswordChange raises reauth flag on ReauthRequired`() = runTest {
        val reauth = Result.failure<String>(AppException(AppError.ReauthRequired(AppError.ReauthRequired.MESSAGE)))
        val viewModel = viewModelWith(ticketResult = reauth)

        viewModel.requestPasswordChange()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRequestingTicket)
        assertTrue(viewModel.uiState.value.needsReauth)
        assertNull(viewModel.uiState.value.ticketUrl)
    }

    @Test
    fun `requestPasswordChange shows banner on other errors`() = runTest {
        val failure = Result.failure<String>(AppException(AppError.RateLimited(AppError.RateLimited.MESSAGE)))
        val viewModel = viewModelWith(ticketResult = failure)

        viewModel.requestPasswordChange()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.needsReauth)
        assertEquals(AppError.RateLimited.MESSAGE, viewModel.uiState.value.passwordChangeError)
    }

    @Test
    fun `onReauthSubmit retries the ticket after a successful login`() = runTest {
        val session = UserSession(token = "nuevo-token", userId = "auth0|1", name = "Ana", role = UserRole.RESIDENT)
        val sessionManager = SessionManager(NoOpRolePersistence)
        val viewModel = ProfileViewModel(
            sessionManager,
            FakeReauthLoginProvider(loginResult = Result.success(session)),
            ChangePasswordUseCase(FakeUserRepository(Result.success("https://ticket/reintento")))
        )

        viewModel.onReauthSubmit("ana@urbanguard.co", "secreta")
        advanceUntilIdle()

        assertEquals("https://ticket/reintento", viewModel.uiState.value.ticketUrl)
        assertEquals("nuevo-token", sessionManager.token)
    }

    @Test
    fun `onReauthSubmit shows error when login fails`() = runTest {
        val loginFailure = Result.failure<UserSession>(
            AppException(AppError.InvalidCredentials(AppError.InvalidCredentials.MESSAGE))
        )
        val viewModel = viewModelWith(loginResult = loginFailure)

        viewModel.onReauthSubmit("ana@urbanguard.co", "mala")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.ticketUrl)
        assertEquals(AppError.InvalidCredentials.MESSAGE, viewModel.uiState.value.passwordChangeError)
    }

    @Test
    fun `onTicketConsumed and dismiss handlers clear the state`() = runTest {
        val viewModel = viewModelWith(ticketResult = Result.success("https://ticket/abc"))
        viewModel.requestPasswordChange()
        advanceUntilIdle()

        viewModel.onTicketConsumed()
        assertNull(viewModel.uiState.value.ticketUrl)

        viewModel.onPasswordChangeErrorDismissed()
        viewModel.onReauthDismissed()
        assertNull(viewModel.uiState.value.passwordChangeError)
        assertFalse(viewModel.uiState.value.needsReauth)
    }

    private fun viewModelWith(
        ticketResult: Result<String> = Result.success("https://ticket/abc"),
        loginResult: Result<UserSession> = Result.success(
            UserSession(token = "token", userId = "auth0|1", name = "Ana", role = UserRole.RESIDENT)
        )
    ): ProfileViewModel {
        return ProfileViewModel(
            SessionManager(NoOpRolePersistence),
            FakeReauthLoginProvider(loginResult),
            ChangePasswordUseCase(FakeUserRepository(ticketResult))
        )
    }

    private class FakeUserRepository(
        private val ticketResult: Result<String>
    ) : UserRepository {
        override suspend fun createPasswordChangeTicket(): Result<String> = ticketResult

        // No se usan en este flujo; la interfaz creció con la HU-006.
        override suspend fun listUsers(estado: String): Result<List<User>> =
            Result.success(emptyList())

        override suspend fun deactivateUser(userId: String): Result<Unit> = Result.success(Unit)

        override suspend fun reactivateUser(userId: String): Result<User> =
            Result.failure(IllegalStateException("No soportado en este test"))
    }

    private class FakeReauthLoginProvider(
        private val loginResult: Result<UserSession>
    ) : ReauthLoginProvider {
        override suspend fun login(email: String, password: String): UserSession =
            loginResult.getOrThrow()
    }
}
