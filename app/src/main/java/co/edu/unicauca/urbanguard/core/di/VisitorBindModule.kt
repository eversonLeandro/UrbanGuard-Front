package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.repository.VisitorRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VisitorBindModule {

    @Binds
    @Singleton
    abstract fun bindVisitorRepository(
        impl: VisitorRepositoryImpl
    ): VisitorRepository
}
