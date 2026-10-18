package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.data.remote.api.service.NotificationInboxApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationInboxProvideModule {

    @Provides
    @Singleton
    fun provideNotificationInboxApi(retrofit: Retrofit): NotificationInboxApi =
        retrofit.create(NotificationInboxApi::class.java)
}
