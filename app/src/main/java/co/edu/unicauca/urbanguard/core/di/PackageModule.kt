package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.PackageApi
import co.edu.unicauca.urbanguard.data.repository.PackageRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.PackageRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PackageProvideModule {
    @Provides
    @Singleton
    fun providePackageApi(retrofit: Retrofit): PackageApi = retrofit.create(PackageApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PackageBindModule {
    @Binds
    @Singleton
    abstract fun bindPackageRepository(implementation: PackageRepositoryImpl): PackageRepository
}
