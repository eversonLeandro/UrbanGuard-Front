package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolApi
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolRoundApi
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolShiftApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PatrolProvideModule {

    @Provides
    @Singleton
    fun providePatrolApi(retrofit: Retrofit): PatrolApi =
        retrofit.create(PatrolApi::class.java)

    @Provides
    @Singleton
    fun providePatrolRoundApi(retrofit: Retrofit): PatrolRoundApi =
        retrofit.create(PatrolRoundApi::class.java)

    @Provides
    @Singleton
    fun providePatrolExecutionApi(
        retrofit: Retrofit
    ): co.edu.unicauca.urbanguard.data.remote.api.service.PatrolExecutionApi =
        retrofit.create(co.edu.unicauca.urbanguard.data.remote.api.service.PatrolExecutionApi::class.java)

    @Provides
    @Singleton
    fun providePatrolShiftApi(retrofit: Retrofit): PatrolShiftApi =
        retrofit.create(PatrolShiftApi::class.java)

    @Provides
    @Singleton
    fun providePatrolComplianceApi(retrofit: Retrofit): co.edu.unicauca.urbanguard.data.remote.api.service.PatrolComplianceApi =
        retrofit.create(co.edu.unicauca.urbanguard.data.remote.api.service.PatrolComplianceApi::class.java)

    /**
     * Reloj de la zona del condominio. Se inyecta para poder fijar la fecha en
     * los tests y para que el rango por defecto del historial (7 días) no lea el
     * reloj del dispositivo directamente.
     */
    @Provides
    @Singleton
    fun providePatrolHistoryClock(): Clock =
        Clock.system(PatrolHistoryConstants.CONDOMINIUM_ZONE)
}
