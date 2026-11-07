package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.data.remote.api.dto.UserResponseDto

import co.edu.unicauca.urbanguard.data.remote.api.dto.PasswordChangeTicketResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.service.UserApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Verifica que el repositorio devuelve la URL del ticket sin alterarla y que
 * los errores HTTP viajan tipados (401 REAUTH_REQUIRED → [AppError.ReauthRequired],
 * nunca como texto suelto).
 */
class UserRepositoryImplTest {

    @Test
    fun `createPasswordChangeTicket returns the backend url`() = runTest {
        val repository = UserRepositoryImpl(FakeUserApi(ticketUrl = "https://ticket/reset?x=1"))

        val result = repository.createPasswordChangeTicket()

        assertTrue(result.isSuccess)
        assertEquals("https://ticket/reset?x=1", result.getOrNull())
    }

    @Test
    fun `createPasswordChangeTicket maps 401 REAUTH_REQUIRED to ReauthRequired`() = runTest {
        val repository = UserRepositoryImpl(FakeUserApi(error = httpException(401, "REAUTH_REQUIRED")))

        val result = repository.createPasswordChangeTicket()

        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as? AppException)?.error
        assertTrue("Se esperaba ReauthRequired pero fue $error", error is AppError.ReauthRequired)
    }

    @Test
    fun `createPasswordChangeTicket maps server errors to ServerError`() = runTest {
        val repository = UserRepositoryImpl(FakeUserApi(error = httpException(502, "Bad Gateway")))

        val result = repository.createPasswordChangeTicket()

        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as? AppException)?.error
        assertTrue("Se esperaba ServerError pero fue $error", error is AppError.ServerError)
    }

    private class FakeUserApi(
        private val ticketUrl: String? = null,
        private val error: Throwable? = null
    ) : UserApi {
        override suspend fun createPasswordChangeTicket(): PasswordChangeTicketResponseDto {
            error?.let { throw it }
            return PasswordChangeTicketResponseDto(url = requireNotNull(ticketUrl))
        }

        // No se usan en este flujo; la interfaz creció con la HU-006.
        override suspend fun listUsers(estado: String?): List<UserResponseDto> = emptyList()

        override suspend fun deactivateUser(userId: String) {}

        override suspend fun reactivateUser(userId: String): UserResponseDto {
            error?.let { throw it }
            return UserResponseDto(
                publicId = "6f1c9a70-0001-4000-8000-000000000001",
                userId = userId,
                activo = true
            )
        }
    }

    private fun httpException(code: Int, message: String): HttpException {
        val json = """{"status":$code,"error":"Error","message":"$message","path":"/api/users/me/password-change"}"""
        val body = json.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, body))
    }
}
