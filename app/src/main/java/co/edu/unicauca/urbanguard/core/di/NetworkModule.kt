package co.edu.unicauca.urbanguard.core.di

import co.edu.unicauca.urbanguard.core.network.AuthInterceptor
import co.edu.unicauca.urbanguard.core.network.NetworkConfig
import co.edu.unicauca.urbanguard.core.network.RequestIdInterceptor
import co.edu.unicauca.urbanguard.data.remote.api.service.AuthApi
import co.edu.unicauca.urbanguard.data.remote.api.service.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.inject.Named

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
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
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

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    /** Solo el PUT de foto usa este cliente: no repetir ni seguir redirecciones del cuerpo. */
    @Provides
    @Singleton
    @Named("profileUpload")
    fun provideProfileUploadClient(okHttpClient: OkHttpClient): OkHttpClient =
        okHttpClient.newBuilder()
            .apply { interceptors().removeAll { it is AuthInterceptor } }
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()

    @Provides
    @Singleton
    @Named("profileUpload")
    fun provideProfileUploadUserApi(
        retrofit: Retrofit,
        @Named("profileUpload") client: OkHttpClient
    ): UserApi = retrofit.newBuilder().client(client).build().create(UserApi::class.java)
}
