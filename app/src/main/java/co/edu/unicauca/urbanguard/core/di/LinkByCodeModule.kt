package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.LinkByCodeApi
import co.edu.unicauca.urbanguard.data.repository.LinkByCodeRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.LinkByCodeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LinkByCodeProvideModule {

    @Provides
    @Singleton
    fun provideLinkByCodeApi(retrofit: Retrofit): LinkByCodeApi =
        retrofit.create(LinkByCodeApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LinkByCodeBindModule {

    @Binds
    @Singleton
    abstract fun bindLinkByCodeRepository(
        impl: LinkByCodeRepositoryImpl
    ): LinkByCodeRepository
}
