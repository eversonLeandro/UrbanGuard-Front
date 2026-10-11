package co.edu.unicauca.urbanguard.domain.model

import java.time.Instant

/**
 * Novedad de vigilancia reportada por el guardia (UG-43).
 *
 * Aviso operativo con foto opcional y punto de referencia: no es una PQRS
 * (sin radicado ni SLA de respuesta). [photoPath] es la ruta local de la
 * foto hasta que el backend la sirva por URL.
 */
data class SecurityIncident(
    val id: String,
    val complexId: String,
    val category: IncidentCategory,
    val description: String,
    val photoPath: String? = null,
    val pointId: String? = null,
    val pointName: String? = null,
    val createdAt: Instant,
    val syncState: IncidentSyncState = IncidentSyncState.PENDING
)

/** Categoría operativa de la novedad, con su color. */
enum class IncidentCategory {
    SECURITY,
    INFRASTRUCTURE,
    LIGHTING,
    CLEANING,
    OTHER
}

/** Estado de sincronización con el backend. */
enum class IncidentSyncState {
    PENDING,
    SYNCED
}

/** Resultado de validar el formulario. */
enum class IncidentValidation {
    VALID,
    CATEGORY_REQUIRED,
    DESCRIPTION_REQUIRED,
    DESCRIPTION_TOO_SHORT
}

private const val MIN_DESCRIPTION_LENGTH = 10

/**
 * Valida el formulario: categoría y descripción con un mínimo para que el
 * reporte sirva. La foto es opcional pero la UI la incentiva.
 */
fun validateIncident(
    category: IncidentCategory?,
    description: String
): IncidentValidation {
    if (category == null) return IncidentValidation.CATEGORY_REQUIRED
    if (description.isBlank()) return IncidentValidation.DESCRIPTION_REQUIRED
    if (description.trim().length < MIN_DESCRIPTION_LENGTH) {
        return IncidentValidation.DESCRIPTION_TOO_SHORT
    }
    return IncidentValidation.VALID
}

/** Etiqueta visible de la categoría. */
fun IncidentCategory.label(): String = when (this) {
    IncidentCategory.SECURITY -> "Seguridad"
    IncidentCategory.INFRASTRUCTURE -> "Infraestructura"
    IncidentCategory.LIGHTING -> "Alumbrado"
    IncidentCategory.CLEANING -> "Aseo"
    IncidentCategory.OTHER -> "Otro"
}
