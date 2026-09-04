package co.edu.unicauca.urbanguard.core.network
import co.edu.unicauca.urbanguard.data.remote.api.dto.ApiErrorResponseDto
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/**
 * Punto único donde se traduce cualquier error de red a un mensaje
 * legible para el usuario. TODOS los repositorios (Auth, Visitor, Pqrs,
 * Patrol, etc.) deben usar esto en vez de escribir su propio try/catch
 * genérico, para que el comportamiento sea idéntico en toda la app.
 */
object NetworkErrorMapper {
    fun map(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> mapHttpException(throwable)
            is IOException -> "Sin conexión. Verifica tu internet."
            else -> "Ocurrió un error inesperado. Intenta de nuevo."
        }
    }

    private fun mapHttpException(exception: HttpException): String {
        // Intenta leer el mensaje real que mandó el backend/Gateway
        val bodyMessage = runCatching {
            val errorBody = exception.response()?.errorBody()?.string()
            Gson().fromJson(errorBody, ApiErrorResponseDto::class.java)?.message
        }.getOrNull()

        if (!bodyMessage.isNullOrBlank()) return bodyMessage

        // Si no hay mensaje legible en el body, usamos el código HTTP como respaldo
        return when (exception.code()) {
            401 -> "Correo o contraseña incorrectos."
            403 -> "No tienes permiso para realizar esta acción."
            404 -> "El recurso solicitado no existe."
            409 -> "Ya existe un registro con esos datos."
            502, 503 -> "El servicio no está disponible en este momento. Intenta más tarde."
            504 -> "El servidor tardó demasiado en responder. Intenta de nuevo."
            else -> "Ocurrió un error. Intenta de nuevo."
        }
    }
}