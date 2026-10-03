package co.edu.unicauca.urbanguard.domain.usecase.users

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El caso de uso es un passthrough delgado: solo delega al repositorio y
 * propaga el resultado intacto (la URL nunca se transforma ni se persiste).
 */
class ChangePasswordUseCaseTest {

    @Test
    fun `invoke returns the ticket url on success`() = runTest {
        val useCase = ChangePasswordUseCase(FakeUserRepository(Result.success("https://ticket/reset")))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals("https://ticket/reset", result.getOrNull())
    }

    @Test
    fun `invoke propagates repository failures untouched`() = runTest {
        val failure = Result.failure<String>(AppException(AppError.ReauthRequired(AppError.ReauthRequired.MESSAGE)))
        val useCase = ChangePasswordUseCase(FakeUserRepository(failure))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(failure.exceptionOrNull(), result.exceptionOrNull())
    }

    private class FakeUserRepository(
        private val ticketResult: Result<String>
    ) : UserRepository {
        override suspend fun createPasswordChangeTicket(): Result<String> = ticketResult
    }
}
