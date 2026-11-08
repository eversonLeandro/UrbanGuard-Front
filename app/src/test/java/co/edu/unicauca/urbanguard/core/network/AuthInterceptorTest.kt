package co.edu.unicauca.urbanguard.core.network

import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class AuthInterceptorTest {

    @Test
    fun passwordResetRequestDoesNotForwardStoredAuthorizationToken() {
        val sessionManager = SessionManager(NoOpRolePersistence).apply {
            saveSession(UserSession("saved-token", "user-1", "User", UserRole.RESIDENT))
        }
        val request = Request.Builder()
            .url("https://gateway.example.com/api/auth/password-reset")
            .header("Authorization", "Bearer stale-token")
            .header("X-UrbanGuard-Public-Request", "password-reset")
            .build()
        val chain = CapturingChain(request)

        AuthInterceptor(sessionManager).intercept(chain)

        assertNull(chain.proceededRequest?.header("Authorization"))
        assertNull(chain.proceededRequest?.header("X-UrbanGuard-Public-Request"))
    }

    private class CapturingChain(
        private val originalRequest: Request
    ) : Interceptor.Chain {
        var proceededRequest: Request? = null

        override fun request(): Request = originalRequest

        override fun proceed(request: Request): Response {
            proceededRequest = request
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
        }

        override fun connection(): Connection? = null

        override fun call(): Call = throw UnsupportedOperationException()

        override fun connectTimeoutMillis(): Int = 0

        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

        override fun readTimeoutMillis(): Int = 0

        override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

        override fun writeTimeoutMillis(): Int = 0

        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    }
}
