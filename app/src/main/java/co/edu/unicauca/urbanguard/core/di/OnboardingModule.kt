package co.edu.unicauca.urbanguard.core.di
import co.edu.unicauca.urbanguard.data.remote.api.service.OnboardingApi
import co.edu.unicauca.urbanguard.data.repository.ConjuntoCodeRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoCodeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OnboardingModule {
}
@Module
@InstallIn(SingletonComponent::class)
object OnboardingProvideModule {

    @Provides
    @Singleton
    fun provideOnboardingApi(retrofit: Retrofit): OnboardingApi =
        retrofit.create(OnboardingApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingBindModule {

    @Binds
    @Singleton
    abstract fun bindConjuntoCodeRepository(
        impl: ConjuntoCodeRepositoryImpl
    ): ConjuntoCodeRepository
}