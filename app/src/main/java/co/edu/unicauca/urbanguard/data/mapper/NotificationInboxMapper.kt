package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.NotificationDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.NotificationPageDto
import co.edu.unicauca.urbanguard.domain.model.NotificationItem
import co.edu.unicauca.urbanguard.domain.model.NotificationPage

fun NotificationDto.toNotificationItem(): NotificationItem =
    NotificationItem(
        id = id,
        title = title,
        body = body,
        deepLink = deepLink,
        channel = channel,
        priority = priority,
        conjuntoId = conjuntoId,
        createdAt = createdAt,
        readAt = readAt
    )

fun NotificationPageDto.toNotificationPage(): NotificationPage =
    NotificationPage(
        items = items.map { it.toNotificationItem() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages
    )
