package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.repository.ResidentialRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.ResidentialRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ResidentialBindModule {

    @Binds
    @Singleton
    abstract fun bindResidentialRepository(
        impl: ResidentialRepositoryImpl
    ): ResidentialRepository
}
