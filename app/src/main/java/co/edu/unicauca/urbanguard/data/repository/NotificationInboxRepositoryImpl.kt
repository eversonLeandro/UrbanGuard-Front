package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.data.mapper.toNotificationPage
import co.edu.unicauca.urbanguard.data.remote.api.service.NotificationInboxApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.NotificationPage
import co.edu.unicauca.urbanguard.domain.repository.NotificationInboxRepository
import javax.inject.Inject

/**
 * La bandeja siempre se consulta para el conjunto activo de la sesión: el
 * backend exige `conjuntoId` y valida la membresía, así que sin conjunto activo
 * no hay nada que pedir y se devuelve un error de dominio en vez de llamar a la
 * API.
 */
class NotificationInboxRepositoryImpl @Inject constructor(
    private val api: NotificationInboxApi,
    private val sessionManager: SessionManager
) : NotificationInboxRepository {

    override suspend fun getNotifications(
        page: Int,
        size: Int,
        unreadOnly: Boolean
    ): Result<NotificationPage> {
        val conjuntoId = activeConjuntoId()
            ?: return missingConjunto()
        return apiCall {
            api.getNotifications(conjuntoId, page, size, unreadOnly.takeIf { it }).toNotificationPage()
        }
    }

    override suspend fun getUnreadCount(): Result<Int> {
        val conjuntoId = activeConjuntoId()
            ?: return missingConjunto()
        return apiCall {
            api.getUnreadCount(conjuntoId).unreadCount.toInt().coerceAtLeast(0)
        }
    }

    override suspend fun markRead(id: String): Result<Unit> {
        val conjuntoId = activeConjuntoId()
            ?: return missingConjunto()
        return apiCall {
            api.markRead(id, conjuntoId)
        }
    }

    override suspend fun markAllRead(): Result<Unit> {
        val conjuntoId = activeConjuntoId()
            ?: return missingConjunto()
        return apiCall {
            api.markAllRead(conjuntoId)
        }
    }

    private fun activeConjuntoId(): String? = sessionManager.getActiveConjunto()?.conjuntoId

    private fun missingConjunto(): Result<Nothing> =
        Result.failure(AppException(AppError.Forbidden(MISSING_CONJUNTO_MESSAGE)))

    private companion object {
        const val MISSING_CONJUNTO_MESSAGE =
            "Selecciona un conjunto para consultar tus notificaciones."
    }
}
