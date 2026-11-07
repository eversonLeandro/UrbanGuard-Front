package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.PackageDto
import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.model.PackageStatus

fun PackageDto.toDomain(): Package = Package(
    id = id.toString(),
    complexId = complexId.toString(),
    residentialUnitId = residentialUnitId?.toString(),
    residentialUnitLabel = residentialUnitLabel,
    residentId = residentId?.toString(),
    recipientName = recipientName,
    sender = sender,
    description = description,
    observations = observations,
    status = runCatching { PackageStatus.valueOf(status) }.getOrDefault(PackageStatus.RECIBIDO),
    receivedAt = receivedAt,
    deliveredTo = deliveredTo,
    deliveredAt = deliveredAt,
    evidenceUrl = evidenceUrl
)
