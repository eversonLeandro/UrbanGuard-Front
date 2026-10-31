package co.edu.unicauca.urbanguard.ui.features.pqrs

import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.ui.components.StatusTone

fun residentPqrsStatusTone(status: PqrsStatus): StatusTone = when (status) {
    PqrsStatus.SUBMITTED, PqrsStatus.IN_REVIEW -> StatusTone.WARNING
    PqrsStatus.IN_PROGRESS -> StatusTone.INFO
    PqrsStatus.RESOLVED -> StatusTone.SUCCESS
    PqrsStatus.REJECTED -> StatusTone.ERROR
    PqrsStatus.CLOSED -> StatusTone.NEUTRAL
}
