package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorValidationErrorDto
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.VisitorValidationFailure
import com.google.gson.Gson
import java.io.IOException

/**
 * Traduce los errores de
 * `POST .../visitor-invitations/validation` a [AppException].
 *
 * El backend responde `{ reason, message }` con `reason` en USED, EXPIRED,
 * NOT_YET_VALID o INVALID_TOKEN; el motivo viaja prefijado en el mensaje
 * como `FAILURE|mensaje` para que el ViewModel lo recupere sin comparar
 * textos visibles.
 */
internal fun mapValidationError(httpCode: Int, rawBody: String?): AppException {
    val reason = runCatching {
        if (rawBody.isNullOrBlank()) null
        else Gson().fromJson(rawBody, VisitorValidationErrorDto::class.java)?.reason
    }.getOrNull()?.trim()?.uppercase()
    val backendMessage = runCatching {
        if (rawBody.isNullOrBlank()) null
        else Gson().fromJson(rawBody, VisitorValidationErrorDto::class.java)?.message
    }.getOrNull()?.trim().orEmpty()
    val failure = validationFailureOf(reason, httpCode)
    val message = backendMessage.ifBlank { defaultValidationMessage(failure) }
    return AppException(validationErrorOf(failure, message, httpCode)).withFailure(failure)
}

private fun validationFailureOf(reason: String?, httpCode: Int): VisitorValidationFailure {
    when (reason) {
        "USED" -> return VisitorValidationFailure.USED
        "EXPIRED" -> return VisitorValidationFailure.EXPIRED
        "NOT_YET_VALID" -> return VisitorValidationFailure.NOT_YET_VALID
        "INVALID_TOKEN" -> return VisitorValidationFailure.INVALID_TOKEN
    }
    return when (httpCode) {
        400 -> VisitorValidationFailure.INVALID_FORMAT
        401 -> VisitorValidationFailure.SESSION_EXPIRED
        403 -> VisitorValidationFailure.FORBIDDEN
        404 -> VisitorValidationFailure.INVALID_TOKEN
        410 -> VisitorValidationFailure.EXPIRED
        409 -> VisitorValidationFailure.USED
        else -> VisitorValidationFailure.SERVER_ERROR
    }
}

private fun defaultValidationMessage(failure: VisitorValidationFailure): String =
    when (failure) {
        VisitorValidationFailure.USED ->
            "Este pase ya fue registrado previamente en la portería."
        VisitorValidationFailure.EXPIRED ->
            "Este pase fue emitido para otra fecha y ya no es válido."
        VisitorValidationFailure.NOT_YET_VALID ->
            "Este pase todavía no es válido para hoy."
        VisitorValidationFailure.INVALID_TOKEN ->
            "El código escaneado no corresponde a una invitación válida."
        VisitorValidationFailure.FORBIDDEN ->
            "No tienes permiso para validar pases en este conjunto."
        VisitorValidationFailure.NO_CONNECTION ->
            AppError.NoConnection.MESSAGE
        VisitorValidationFailure.SESSION_EXPIRED ->
            AppError.SessionExpired.MESSAGE
        else -> AppError.ServerError.MESSAGE
    }

private fun validationErrorOf(
    failure: VisitorValidationFailure,
    message: String,
    httpCode: Int
): AppError =
    when {
        failure == VisitorValidationFailure.USED ||
            failure == VisitorValidationFailure.NOT_YET_VALID ->
            AppError.Conflict(message)
        failure == VisitorValidationFailure.EXPIRED ||
            failure == VisitorValidationFailure.INVALID_FORMAT ||
            failure == VisitorValidationFailure.NO_ACTIVE_COMPLEX ->
            AppError.Validation(message)
        failure == VisitorValidationFailure.INVALID_TOKEN ||
            failure == VisitorValidationFailure.NOT_FOUND ->
            AppError.NotFound(message)
        failure == VisitorValidationFailure.FORBIDDEN ->
            AppError.Forbidden(message)
        failure == VisitorValidationFailure.SESSION_EXPIRED ->
            AppError.SessionExpired(message)
        failure == VisitorValidationFailure.NO_CONNECTION ->
            AppError.NoConnection(message)
        failure == VisitorValidationFailure.SERVER_ERROR &&
            (httpCode == 500 || httpCode == 502 || httpCode == 503 || httpCode == 504) ->
            AppError.ServerError(message)
        failure == VisitorValidationFailure.SERVER_ERROR ->
            AppError.ServerError(message)
        else ->
            AppError.from(Exception(NetworkErrorMapper.map(IOException(message))))
    }

/**
 * Prefija el motivo tipado al mensaje (`FAILURE|mensaje`) conservando el
 * tipo de [AppError], sin necesidad de subclases (la clase es final).
 */
internal fun AppException.withFailure(failure: VisitorValidationFailure): AppException {
    val prefixed = "${failure.name}|${this.error.message}"
    val e = this.error
    val newError: AppError = when (e) {
        is AppError.Conflict -> AppError.Conflict(prefixed)
        is AppError.Validation -> AppError.Validation(prefixed)
        is AppError.NotFound -> AppError.NotFound(prefixed)
        is AppError.Forbidden -> AppError.Forbidden(prefixed)
        is AppError.SessionExpired -> AppError.SessionExpired(prefixed)
        is AppError.NoConnection -> AppError.NoConnection(prefixed)
        is AppError.ServerError -> AppError.ServerError(prefixed)
        else -> AppError.Unexpected(prefixed)
    }
    return AppException(newError)
}
