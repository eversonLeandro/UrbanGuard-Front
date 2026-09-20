package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.CommunityApi
import co.edu.unicauca.urbanguard.data.repository.ConjuntoAssociationRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoAssociationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommunityProvideModule {

    @Provides
    @Singleton
    fun provideCommunityApi(retrofit: Retrofit): CommunityApi =
        retrofit.create(CommunityApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CommunityBindModule {

    @Binds
    @Singleton
    abstract fun bindConjuntoAssociationRepository(
        impl: ConjuntoAssociationRepositoryImpl
    ): ConjuntoAssociationRepository
}
