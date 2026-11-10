package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorHistoryItemDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.VisitorHistoryPageDto
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryItem
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryPage
import java.time.Instant
import java.util.UUID

internal fun VisitorHistoryPageDto.toVisitorHistoryPage(requestedPage: Int, requestedSize: Int): VisitorHistoryPage {
    val entries = content ?: invalidHistoryResponse()
    val pageNumber = page?.takeIf { it == requestedPage && it >= 0 } ?: invalidHistoryResponse()
    val pageSize = size?.takeIf { it == requestedSize && it > 0 } ?: invalidHistoryResponse()
    val total = totalElements?.takeIf { it >= 0 } ?: invalidHistoryResponse()
    val pages = totalPages ?: invalidHistoryResponse()
    validatePageCounts(pageNumber, pageSize, total, pages, entries.size)

    return VisitorHistoryPage(
        items = entries.map { it?.toHistoryItem() ?: invalidHistoryResponse() },
        page = pageNumber,
        size = pageSize,
        totalElements = total,
        totalPages = pages
    )
}

private fun validatePageCounts(page: Int, size: Int, total: Long, pages: Int, itemCount: Int) {
    val expectedPages = if (total == 0L) 0L else (total - 1) / size + 1
    val remaining = (total - page.toLong() * size).coerceAtLeast(0)
    if (pages.toLong() != expectedPages || itemCount.toLong() != minOf(size.toLong(), remaining)) {
        invalidHistoryResponse()
    }
}

private fun VisitorHistoryItemDto.toHistoryItem(): VisitorHistoryItem {
    val id = required(invitationId)
    val canonicalId = runCatching { UUID.fromString(id).toString() }.getOrNull() ?: invalidHistoryResponse()
    if (!canonicalId.equals(id, ignoreCase = true)) invalidHistoryResponse()

    val timestamp = required(actualEntryAt)
    return VisitorHistoryItem(
        invitationId = canonicalId,
        creatorName = required(creatorName),
        visitorName = required(visitorName),
        guestCount = guestCount?.takeIf { it > 0 } ?: invalidHistoryResponse(),
        actualEntryAt = runCatching { Instant.parse(timestamp) }.getOrNull() ?: invalidHistoryResponse()
    )
}

private fun required(value: String?): String = value?.takeIf { it.isNotBlank() } ?: invalidHistoryResponse()

internal fun invalidHistoryResponse(cause: Throwable? = null): Nothing =
    throw AppException(AppError.Unexpected("No se pudo leer la respuesta del historial de visitantes."))
        .also { it.initCause(cause) }
