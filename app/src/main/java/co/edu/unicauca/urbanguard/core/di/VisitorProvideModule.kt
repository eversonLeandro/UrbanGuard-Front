package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.VisitorApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VisitorProvideModule {

    @Provides
    @Singleton
    fun provideVisitorApi(retrofit: Retrofit): VisitorApi =
        retrofit.create(VisitorApi::class.java)
}
