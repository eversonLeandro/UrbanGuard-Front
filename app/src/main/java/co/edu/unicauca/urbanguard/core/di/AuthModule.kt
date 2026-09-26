package co.edu.unicauca.urbanguard.core.di

import android.content.Context
import co.edu.unicauca.urbanguard.BuildConfig
import co.edu.unicauca.urbanguard.core.session.EncryptedRolePersistence
import co.edu.unicauca.urbanguard.core.session.RolePersistence
import co.edu.unicauca.urbanguard.data.repository.AuthRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRolePersistence(impl: EncryptedRolePersistence): RolePersistence

    companion object {
        @Provides
        @Singleton
        fun provideAuth0(): Auth0 {
            return Auth0(BuildConfig.AUTH0_CLIENT_ID, BuildConfig.AUTH0_DOMAIN)
        }

        @Provides
        @Singleton
        fun provideAuthenticationAPIClient(auth0: Auth0): AuthenticationAPIClient {
            return AuthenticationAPIClient(auth0)
        }

        /**
         * Almacena las credenciales cifradas con una clave del Android Keystore
         * (SharedPreferences cifradas). Soporta refresh token (offline_access)
         * para restaurar la sesión al reabrir la app sin volver a pedir contraseña.
         */
        @Provides
        @Singleton
        fun provideCredentialsManager(
            @ApplicationContext context: Context,
            apiClient: AuthenticationAPIClient
        ): SecureCredentialsManager {
            return SecureCredentialsManager(
                context,
                apiClient,
                SharedPreferencesStorage(context, "urbanguard_auth_prefs")
            )
        }
    }
}