package co.edu.unicauca.urbanguard.core.network

import co.edu.unicauca.urbanguard.BuildConfig

object NetworkConfig {
    // Apunta al API Gateway, no a un microservicio directamente.
    // El Gateway enruta por path: /auth/**, /residential/**, /visitors/**, etc.
    val BASE_URL: String = BuildConfig.BASE_URL

    // Prefijo de versión de API, si el Gateway lo maneja así (ajustar si el equipo backend confirma otro esquema)
    const val API_VERSION_PATH = "api/v1/"

    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 20L
}
