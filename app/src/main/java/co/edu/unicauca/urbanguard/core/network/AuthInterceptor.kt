package co.edu.unicauca.urbanguard.core.network

import co.edu.unicauca.urbanguard.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.url.encodedPath == "/api/auth/password-reset" &&
            original.header("X-UrbanGuard-Public-Request") == "password-reset"
        ) {
            val request = original.newBuilder()
                .removeHeader("X-UrbanGuard-Public-Request")
                .removeHeader("Authorization")
                .build()
            return chain.proceed(request)
        }
        val token = sessionManager.token
        val request = if (token != null) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else original
        return chain.proceed(request)
    }
}
