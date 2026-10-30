package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.PqrsApi
import co.edu.unicauca.urbanguard.data.repository.PqrsRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.PqrsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PqrsProvideModule {
    @Provides
    @Singleton
    fun providePqrsApi(retrofit: Retrofit): PqrsApi = retrofit.create(PqrsApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PqrsBindModule {
    @Binds
    @Singleton
    abstract fun bindPqrsRepository(impl: PqrsRepositoryImpl): PqrsRepository
}
