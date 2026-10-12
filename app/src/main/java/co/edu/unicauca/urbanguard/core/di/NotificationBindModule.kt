package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.core.push.PushRegistrar
import co.edu.unicauca.urbanguard.core.push.PushRegistrarImpl
import co.edu.unicauca.urbanguard.data.repository.NotificationRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationBindModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindPushRegistrar(impl: PushRegistrarImpl): PushRegistrar
}
