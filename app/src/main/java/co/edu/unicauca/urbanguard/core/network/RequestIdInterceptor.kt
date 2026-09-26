package co.edu.unicauca.urbanguard.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

/**
 * Agrega un identificador único a cada petición (X-Request-Id).
 * Con un API Gateway + varios microservicios, esto es indispensable:
 * si algo falla en producción, el equipo de backend puede buscar
 * ese mismo ID en los logs del Gateway y de cada microservicio
 * para rastrear exactamente qué pasó con esa petición específica.
 */
class RequestIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestId = UUID.randomUUID().toString()
        val request = chain.request().newBuilder()
            .addHeader("X-Request-Id", requestId)
            .build()
        return chain.proceed(request)
    }
}