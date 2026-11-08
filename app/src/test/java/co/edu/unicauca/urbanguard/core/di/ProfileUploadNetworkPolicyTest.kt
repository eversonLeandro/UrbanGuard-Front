package co.edu.unicauca.urbanguard.core.di

import okhttp3.OkHttpClient
import co.edu.unicauca.urbanguard.core.network.AuthInterceptor
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileUploadNetworkPolicyTest {
    @Test fun `upload client disables retries and redirects without changing shared client`() {
        val shared = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(SessionManager(NoOpRolePersistence))).build()
        val upload = NetworkModule.provideProfileUploadClient(shared)
        assertTrue(shared.retryOnConnectionFailure)
        assertTrue(shared.followRedirects)
        assertFalse(upload.retryOnConnectionFailure)
        assertFalse(upload.followRedirects)
        assertFalse(upload.followSslRedirects)
        assertTrue(shared.interceptors.any { it is AuthInterceptor })
        assertFalse(upload.interceptors.any { it is AuthInterceptor })
    }
}
