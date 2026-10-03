package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.repository.UserRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
