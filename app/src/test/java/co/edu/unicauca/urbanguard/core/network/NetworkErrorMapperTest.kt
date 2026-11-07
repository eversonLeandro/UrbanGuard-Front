package co.edu.unicauca.urbanguard.core.network

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * El mapper es el punto donde se decide qué ve el usuario cuando algo falla, así que
 * estos tests fijan el comportamiento que no debe romperse: el 401 no puede decir
 * "contraseña incorrecta", el texto en inglés del backend no debe llegar a la UI y
 * un error ya traducido no debe envolverse dos veces.
 */
class NetworkErrorMapperTest {

    @Test
    fun `401 is reported as session expired and not as wrong credentials`() {
        val error = NetworkErrorMapper.toAppError(httpException(401, "Unauthorized"))

        assertTrue("Se esperaba SessionExpired pero fue $error", error is AppError.SessionExpired)
    }

    @Test
    fun `401 with REAUTH_REQUIRED is reported as reauth required`() {
        val error = NetworkErrorMapper.toAppError(httpException(401, "REAUTH_REQUIRED"))

        assertTrue("Se esperaba ReauthRequired pero fue $error", error is AppError.ReauthRequired)
    }

    @Test
    fun `ACCOUNT_DISABLED is reported as a disabled account and uses the support message`() {
        val error = NetworkErrorMapper.toAppError(
            httpException(403, "Acceso denegado", businessCode = "ACCOUNT_DISABLED")
        )

        assertTrue(error is AppError.AccountDisabled)
        assertEquals(AppError.AccountDisabled.MESSAGE, error.message)
    }

    @Test
    fun `a generic 403 remains a permission error`() {
        val error = NetworkErrorMapper.toAppError(httpException(403, "Acceso denegado"))

        assertTrue(error is AppError.Forbidden)
    }

    @Test
    fun `409 is reported as a conflict`() {
        val error = NetworkErrorMapper.toAppError(httpException(409, "Conflict"))

        assertTrue(error is AppError.Conflict)
    }

    @Test
    fun `429 is reported as rate limited`() {
        val error = NetworkErrorMapper.toAppError(httpException(429, ""))

        assertTrue(error is AppError.RateLimited)
    }

    @Test
    fun `an English reason phrase from the backend is discarded in favour of the Spanish message`() {
        val error = NetworkErrorMapper.toAppError(httpException(500, "Internal Server Error"))

        assertTrue(error is AppError.ServerError)
        assertEquals(AppError.ServerError.MESSAGE, error.message)
    }

    @Test
    fun `a Spanish business message from the backend is preserved`() {
        val error = NetworkErrorMapper.toAppError(httpException(404, "Solicitud no encontrada"))

        assertTrue(error is AppError.NotFound)
        assertEquals("Solicitud no encontrada", error.message)
    }

    @Test
    fun `an IOException is reported as a connection problem`() {
        val error = NetworkErrorMapper.toAppError(IOException("timeout"))

        assertTrue(error is AppError.NoConnection)
    }

    @Test
    fun `a SocketTimeoutException is reported as timeout`() {
        val error = NetworkErrorMapper.toAppError(SocketTimeoutException("timeout"))

        assertTrue(error is AppError.Timeout)
        assertEquals(AppError.Timeout.MESSAGE, error.message)
    }

    @Test
    fun `an already translated AppException is returned untouched`() {
        val original = AppException(AppError.EmailAlreadyRegistered("Ya existe una cuenta."))

        val error = NetworkErrorMapper.toAppError(original)

        assertTrue(error is AppError.EmailAlreadyRegistered)
        assertEquals("Ya existe una cuenta.", error.message)
    }

    @Test
    fun `wrapping twice does not duplicate the message`() {
        val once = NetworkErrorMapper.toAppException(
            AppException(AppError.InvalidCredentials("Credenciales incorrectas."))
        )
        val twice = NetworkErrorMapper.toAppException(once)

        assertEquals("Credenciales incorrectas.", twice.message)
    }

    @Test
    fun `an unmapped exception never leaks its technical message to the user`() {
        val error = NetworkErrorMapper.toAppError(IllegalStateException("java.lang.NullPointerException at Foo.kt:42"))

        assertTrue(error is AppError.Unexpected)
        assertEquals(AppError.Unexpected.MESSAGE, error.message)
    }

    private fun httpException(code: Int, message: String, businessCode: String? = null): HttpException {
        val codeJson = businessCode?.let { "\"$it\"" } ?: "null"
        val json =
            """{"status":$code,"error":"Error","message":"$message","path":"/api/test","code":$codeJson}"""
        val body = json.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, body))
    }
}
