package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.repository.IncidentRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.IncidentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IncidentBindModule {

    @Binds
    @Singleton
    abstract fun bindIncidentRepository(
        impl: IncidentRepositoryImpl
    ): IncidentRepository
}
