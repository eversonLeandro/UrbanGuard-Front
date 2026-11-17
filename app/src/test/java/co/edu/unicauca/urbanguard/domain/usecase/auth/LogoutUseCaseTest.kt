package co.edu.unicauca.urbanguard.domain.usecase.auth

import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var logoutUseCase: LogoutUseCase

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        logoutUseCase = LogoutUseCase(fakeAuthRepository)
    }

    @Test
    fun logout_delegatesToRepositoryLogout() = runBlocking {
        assertEquals(0, fakeAuthRepository.logoutCallCount)
        logoutUseCase()
        assertEquals(1, fakeAuthRepository.logoutCallCount)
    }

    @Test
    fun logout_whenRepositoryThrows_propagatesException() = runBlocking {
        fakeAuthRepository.shouldThrow = true
        var exceptionCaught = false
        try {
            logoutUseCase()
        } catch (e: IllegalStateException) {
            exceptionCaught = true
            assertEquals("Logout failed", e.message)
        }
        assertTrue(exceptionCaught)
    }

    private class FakeAuthRepository : AuthRepository {
        var logoutCallCount = 0
        var shouldThrow = false

        override suspend fun login(email: String, password: String): Result<UserSession> =
            Result.failure(UnsupportedOperationException())

        override suspend fun signUp(email: String, password: String, name: String): Result<UserSession> =
            Result.failure(UnsupportedOperationException())

        override suspend fun requestPasswordReset(email: String): Result<Unit> = Result.success(Unit)

        override suspend fun restoreSession(): UserSession? = null

        override suspend fun logout() {
            if (shouldThrow) {
                error("Logout failed")
            }
            logoutCallCount++
        }
    }
}
