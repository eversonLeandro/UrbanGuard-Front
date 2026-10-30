package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.PqrsResidentApi
import co.edu.unicauca.urbanguard.data.repository.ResidentPqrsRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.ResidentPqrsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ResidentPqrsProvideModule {
    @Provides
    @Singleton
    fun provideResidentPqrsApi(retrofit: Retrofit): PqrsResidentApi =
        retrofit.create(PqrsResidentApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ResidentPqrsBindModule {
    @Binds
    @Singleton
    abstract fun bindResidentPqrsRepository(impl: ResidentPqrsRepositoryImpl): ResidentPqrsRepository
}
