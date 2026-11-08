package co.edu.unicauca.urbanguard.domain.usecase.auth

import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignUpUseCaseTest {

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var signUpUseCase: SignUpUseCase

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        signUpUseCase = SignUpUseCase(fakeAuthRepository)
    }

    @Test
    fun signUp_withInvalidEmail_returnsFailure() = runBlocking {
        val result = signUpUseCase("invalidemail", "password123", "Juan Perez")
        assertTrue(result.isFailure)
        assertEquals("Ingresa un correo electrónico válido", result.exceptionOrNull()?.message)
    }

    @Test
    fun signUp_withShortPassword_returnsFailure() = runBlocking {
        val result = signUpUseCase("user@urbanguard.co", "12345", "Juan Perez")
        assertTrue(result.isFailure)
        assertEquals("La contraseña debe tener al menos 8 caracteres", result.exceptionOrNull()?.message)
    }

    @Test
    fun signUp_withValidData_returnsSuccessSession() = runBlocking {
        val result = signUpUseCase("user@urbanguard.co", "password123", "Juan Perez")
        assertTrue(result.isSuccess)
        val session = result.getOrNull()
        assertEquals("user@urbanguard.co", session?.userId)
        assertEquals("Juan Perez", session?.name)
        assertEquals(UserRole.RESIDENT, session?.role)
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String): Result<UserSession> {
            return Result.success(UserSession("fake_token", email, "User", UserRole.RESIDENT))
        }

        override suspend fun signUp(email: String, password: String, name: String): Result<UserSession> {
            return Result.success(UserSession("fake_token", email, name, UserRole.RESIDENT))
        }

        override suspend fun requestPasswordReset(email: String): Result<Unit> = Result.success(Unit)

        override suspend fun restoreSession(): UserSession? = null

        override suspend fun logout() {}
    }
}
