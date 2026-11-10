package co.edu.unicauca.urbanguard.core.network

import android.util.Log
import co.edu.unicauca.urbanguard.data.remote.api.dto.ApiErrorResponseDto
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Punto único donde se traduce cualquier error de red a un [AppError] legible.
 *
 * TODOS los repositorios deben pasar por aquí. Reglas:
 *  - Si la excepción ya es un [co.edu.unicauca.urbanguard.domain.model.AppException],
 *    su mensaje de negocio se respeta tal cual: nunca se envuelve dos veces.
 *  - El `message` del backend solo se usa si está en español. El backend usa
 *    `HttpStatus.getReasonPhrase()` en algunos casos, que devuelve texto en
 *    inglés; esos se descartan y se usa el mensaje del código HTTP.
 *  - Nunca se muestra al usuario un mensaje técnico de excepción.
 */
object NetworkErrorMapper {
    private const val TAG = "NetworkErrorMapper"
    private const val ACCOUNT_DISABLED_CODE = "ACCOUNT_DISABLED"
    private const val ROUTE_HAS_OPEN_OCCURRENCE_CODE = "ROUTE_HAS_OPEN_OCCURRENCE"

    /**
     * Fases de error de Spring en inglés. El backend las mete en `message`
     * cuando la excepción no es de negocio, y no aportan contexto alguno.
     */
    private val ENGLISH_REASON_PHRASES = setOf(
        "bad request", "unauthorized", "forbidden", "not found",
        "method not allowed", "not acceptable", "unsupported media type",
        "request timeout", "conflict", "gone", "length required",
        "precondition failed", "unprocessable entity", "unprocessable content",
        "too many requests", "internal server error", "not implemented",
        "bad gateway", "service unavailable", "gateway timeout",
        "i/o error", "the request was rejected because the content type is invalid"
    )

    /**
     * Traduce una excepción al error de dominio correspondiente.
     * Es la función principal: use esta en código nuevo.
     */
    fun toAppError(throwable: Throwable): AppError {
        // Idempotencia: si ya trae un error de negocio, se respeta intacto.
        if (throwable is AppException) {
            return throwable.error
        }

        return when (throwable) {
            is HttpException -> fromHttpException(throwable)
            is SocketTimeoutException -> {
                Log.e(TAG, "IOException: ${throwable.javaClass.name} - ${throwable.message}", throwable)
                AppError.Timeout(AppError.Timeout.MESSAGE)
            }
            is IOException -> {
                Log.e(TAG, "IOException: ${throwable.javaClass.name} - ${throwable.message}", throwable)
                AppError.NoConnection(AppError.NoConnection.MESSAGE)
            }
            else -> {
                Log.e(
                    TAG,
                    "Error no mapeado: ${throwable.javaClass.simpleName} - ${throwable.message}",
                    throwable
                )
                AppError.Unexpected(AppError.Unexpected.MESSAGE)
            }
        }
    }

    /**
     * Variante de texto plano para los repositorios que ya trabajan con String.
     * Se mantiene para no romper los call sites existentes.
     */
    fun map(throwable: Throwable): String = toAppError(throwable).message

    /**
     * Envuelve la excepción en un [AppException] conservando el error tipado.
     *
     * Es la forma recomendada de construir un `Result.failure`: al conservar el
     * [AppError], la capa de UI puede distinguir "sesión expirada" de "sin
     * conexión" o "contraseña incorrecta" sin comparar textos.
     */
    fun toAppException(throwable: Throwable): AppException = when (throwable) {
        is AppException -> throwable
        else -> AppException(toAppError(throwable))
    }

    private fun fromHttpException(exception: HttpException): AppError {
        val body = readErrorBody(exception)
        val bodyMessage = body?.message?.trim().orEmpty()

        if (body?.code == ACCOUNT_DISABLED_CODE) {
            return AppError.AccountDisabled(AppError.AccountDisabled.MESSAGE)
        }

        if (body?.code == ROUTE_HAS_OPEN_OCCURRENCE_CODE) {
            return AppError.RouteChangeBlocked(
                bodyMessage.takeIf(::isUsable) ?: AppError.RouteChangeBlocked.MESSAGE
            )
        }

        if (bodyMessage.isNotEmpty() && isUsable(bodyMessage)) {
            Log.w(
                TAG,
                "HTTP ${exception.code()} con mensaje de negocio: $bodyMessage " +
                    "(ref: ${body?.correlationId ?: "sin ref"})"
            )
            return forCode(exception.code(), bodyMessage)
        }

        // Sin mensaje utilizable: se responde con el texto del código HTTP.
        if (bodyMessage.isNotEmpty()) {
            Log.w(
                TAG,
                "HTTP ${exception.code()} con mensaje no utilizable: \"$bodyMessage\" " +
                    "(ref: ${body?.correlationId ?: "sin ref"})"
            )
        }
        return forCode(exception.code(), null)
    }

    private fun readErrorBody(exception: HttpException): ApiErrorResponseDto? = runCatching {
        val raw = exception.response()?.errorBody()?.string()
        if (raw.isNullOrBlank()) null else Gson().fromJson(raw, ApiErrorResponseDto::class.java)
    }.getOrNull()

    /** Descarta los textos en inglés que el backend produce en `message`. */
    private fun isUsable(message: String): Boolean =
        message.lowercase().trim() !in ENGLISH_REASON_PHRASES

    /**
     * Construye el error según el código HTTP. [message] es el texto de negocio
     * del backend, si lo hubo; si es null se usa el texto estándar del código.
     *
     * Nota: un 401 aquí nunca es "contraseña incorrecta". El login real no pasa
     * por el backend (habla directo con Auth0, con su propio mapeo), así que un
     * 401 en cualquier otro endpoint significa que el token venció o fue revocado.
     */
    private fun forCode(code: Int, message: String?): AppError = when (code) {
        400, 422 -> AppError.Validation(message ?: AppError.Validation.MESSAGE)
        401 -> if (message == "REAUTH_REQUIRED") {
            AppError.ReauthRequired(AppError.ReauthRequired.MESSAGE)
        } else {
            AppError.SessionExpired(message ?: AppError.SessionExpired.MESSAGE)
        }
        403 -> AppError.Forbidden(message ?: AppError.Forbidden.MESSAGE)
        404 -> AppError.NotFound(message ?: AppError.NotFound.MESSAGE)
        409 -> AppError.Conflict(message ?: AppError.Conflict.MESSAGE)
        // 410 GONE: el backend lo usa para pases QR expirados (validación visitante).
        410 -> AppError.Validation(message ?: "Este pase ya no es válido.")
        413 -> AppError.ImageTooLarge()
        415 -> AppError.UnsupportedImage()
        429 -> AppError.RateLimited(message ?: AppError.RateLimited.MESSAGE)
        500, 502, 503, 504 -> AppError.ServerError(message ?: AppError.ServerError.MESSAGE)
        else -> {
            Log.w(TAG, "Código HTTP no mapeado: $code")
            AppError.Unexpected(message ?: AppError.Unexpected.MESSAGE)
        }
    }
}
