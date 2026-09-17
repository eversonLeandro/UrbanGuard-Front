package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.LinkRequestApi
import co.edu.unicauca.urbanguard.data.repository.LinkRequestRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.LinkRequestRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LinkRequestProvideModule {

    @Provides
    @Singleton
    fun provideLinkRequestApi(retrofit: Retrofit): LinkRequestApi =
        retrofit.create(LinkRequestApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LinkRequestBindModule {

    @Binds
    @Singleton
    abstract fun bindLinkRequestRepository(
        impl: LinkRequestRepositoryImpl
    ): LinkRequestRepository
}