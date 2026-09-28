package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.GuardApi
import co.edu.unicauca.urbanguard.data.repository.GuardRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.GuardRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GuardProvideModule {

    @Provides
    @Singleton
    fun provideGuardApi(retrofit: Retrofit): GuardApi =
        retrofit.create(GuardApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class GuardBindModule {

    @Binds
    @Singleton
    abstract fun bindGuardRepository(
        impl: GuardRepositoryImpl
    ): GuardRepository
}
