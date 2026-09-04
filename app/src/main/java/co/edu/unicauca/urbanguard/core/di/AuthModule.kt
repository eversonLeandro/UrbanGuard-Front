package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.repository.AuthRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}