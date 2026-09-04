package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.core.network.AuthInterceptor
import co.edu.unicauca.urbanguard.core.network.NetworkConfig
import co.edu.unicauca.urbanguard.core.network.RequestIdInterceptor
import co.edu.unicauca.urbanguard.data.remote.api.service.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRequestIdInterceptor(): RequestIdInterceptor = RequestIdInterceptor()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        requestIdInterceptor: RequestIdInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(requestIdInterceptor)   //  primero: marca la petición
            .addInterceptor(authInterceptor)        // luego: agrega el token
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)   //  ahora viene de BuildConfig, no hardcodeado
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}