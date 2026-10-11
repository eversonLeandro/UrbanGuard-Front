package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.repository.PatrolRepositoryImpl
import co.edu.unicauca.urbanguard.data.repository.PatrolRouteRepositoryImpl
import co.edu.unicauca.urbanguard.data.repository.PatrolRoundRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.PatrolRepository
import co.edu.unicauca.urbanguard.domain.repository.PatrolRouteRepository
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PatrolBindModule {

    @Binds
    @Singleton
    abstract fun bindPatrolRepository(
        impl: PatrolRepositoryImpl
    ): PatrolRepository

    @Binds
    @Singleton
    abstract fun bindPatrolRouteRepository(
        impl: PatrolRouteRepositoryImpl
    ): PatrolRouteRepository

    @Binds
    @Singleton
    abstract fun bindPatrolRoundRepository(
        impl: PatrolRoundRepositoryImpl
    ): PatrolRoundRepository
}
