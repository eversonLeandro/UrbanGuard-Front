package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolApi
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolRoundApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PatrolProvideModule {

    @Provides
    @Singleton
    fun providePatrolApi(retrofit: Retrofit): PatrolApi =
        retrofit.create(PatrolApi::class.java)

    @Provides
    @Singleton
    fun providePatrolRoundApi(retrofit: Retrofit): PatrolRoundApi =
        retrofit.create(PatrolRoundApi::class.java)
}
