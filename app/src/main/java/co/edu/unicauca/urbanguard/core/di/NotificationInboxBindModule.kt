package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.repository.NotificationInboxRepositoryImpl
import co.edu.unicauca.urbanguard.domain.repository.NotificationInboxRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationInboxBindModule {

    @Binds
    @Singleton
    abstract fun bindNotificationInboxRepository(
        impl: NotificationInboxRepositoryImpl
    ): NotificationInboxRepository
}
