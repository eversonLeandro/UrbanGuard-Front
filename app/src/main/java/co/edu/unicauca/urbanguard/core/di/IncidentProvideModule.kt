package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.IncidentApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IncidentProvideModule {

    @Provides
    @Singleton
    fun provideIncidentApi(retrofit: Retrofit): IncidentApi =
        retrofit.create(IncidentApi::class.java)
}
