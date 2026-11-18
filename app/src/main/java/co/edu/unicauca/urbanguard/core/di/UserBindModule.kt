package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.repository.UserRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI del feature de usuarios (HU-006).
 *
 * Solo hace falta el bind del repositorio: [co.edu.unicauca.urbanguard.data.remote.api.service.UserApi]
 * ya se crea en [NetworkModule] (comparte el Retrofit singleton con su
 * OkHttp + interceptores de sesión).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UserBindModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
