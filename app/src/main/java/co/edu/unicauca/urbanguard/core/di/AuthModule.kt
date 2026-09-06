package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.BuildConfig
import co.edu.unicauca.urbanguard.data.repository.AuthRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

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
    }
}