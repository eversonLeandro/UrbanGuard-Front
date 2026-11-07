package co.edu.unicauca.urbanguard.data.remote.auth0

import co.edu.unicauca.urbanguard.domain.model.AppError
import com.auth0.android.authentication.AuthenticationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Auth0ErrorMapperTest {

    @Test
    fun `blocked user returned with invalid_grant is not reported as wrong credentials`() {
        val error = Auth0ErrorMapper.toAppException(
            AuthenticationException("invalid_grant", "User is blocked")
        ).error

        assertTrue(error is AppError.AccountDisabled)
        assertEquals(AppError.AccountDisabled.MESSAGE, error.message)
    }

    @Test
    fun `access denied by Auth0 is reported as a disabled account`() {
        val error = Auth0ErrorMapper.toAppException(
            AuthenticationException("access_denied", "User is blocked")
        ).error

        assertTrue(error is AppError.AccountDisabled)
        assertEquals(AppError.AccountDisabled.MESSAGE, error.message)
    }

    @Test
    fun `wrong email or password remains an invalid credentials error`() {
        val error = Auth0ErrorMapper.toAppException(
            AuthenticationException("invalid_grant", "Wrong email or password.")
        ).error

        assertTrue(error is AppError.InvalidCredentials)
        assertEquals(AppError.InvalidCredentials.MESSAGE, error.message)
    }

    @Test
    fun `other invalid_grant errors retain the existing credentials fallback`() {
        val error = Auth0ErrorMapper.toAppException(
            AuthenticationException("invalid_grant", "Invalid credentials")
        ).error

        assertTrue(error is AppError.InvalidCredentials)
        assertEquals(AppError.InvalidCredentials.MESSAGE, error.message)
    }

    @Test
    fun `too many attempts remains a temporary account block`() {
        val error = Auth0ErrorMapper.toAppException(
            AuthenticationException("too_many_attempts", "Too many attempts")
        ).error

        assertTrue(error is AppError.AccountBlocked)
        assertEquals(AppError.AccountBlocked.MESSAGE, error.message)
    }
}
