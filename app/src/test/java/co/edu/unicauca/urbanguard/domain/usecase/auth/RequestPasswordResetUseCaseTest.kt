package co.edu.unicauca.urbanguard.domain.usecase.auth

import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RequestPasswordResetUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: RequestPasswordResetUseCase

    @Before
    fun setUp() {
        repository = FakeAuthRepository()
        useCase = RequestPasswordResetUseCase(repository)
    }

    @Test
    fun requestPasswordResetTrimsValidEmailBeforeCallingRepository() = runBlocking {
        val result = useCase("  user@example.com  ")

        assertTrue(result.isSuccess)
        assertEquals("user@example.com", repository.requestedEmail)
        assertEquals(1, repository.requestCount)
    }

    @Test
    fun requestPasswordResetRejectsInvalidEmailWithoutCallingRepository() = runBlocking {
        val result = useCase("invalid-email")

        assertTrue(result.isFailure)
        assertEquals("Ingresa un correo electrónico válido.", result.exceptionOrNull()?.message)
        assertEquals(0, repository.requestCount)
    }

    @Test
    fun requestPasswordResetPropagatesRepositoryFailure() = runBlocking {
        val failure = IllegalStateException("service unavailable")
        repository.result = Result.failure(failure)

        val result = useCase("user@example.com")

        assertFalse(result.isSuccess)
        assertEquals(failure, result.exceptionOrNull())
    }

    private class FakeAuthRepository : AuthRepository {
        var requestedEmail: String? = null
        var requestCount: Int = 0
        var result: Result<Unit> = Result.success(Unit)

        override suspend fun login(email: String, password: String): Result<UserSession> =
            error("Not used")

        override suspend fun signUp(
            email: String,
            password: String,
            name: String
        ): Result<UserSession> = error("Not used")

        override suspend fun requestPasswordReset(email: String): Result<Unit> {
            requestedEmail = email
            requestCount++
            return result
        }

        override suspend fun restoreSession(): UserSession? = null

        override suspend fun logout() = Unit
    }
}
