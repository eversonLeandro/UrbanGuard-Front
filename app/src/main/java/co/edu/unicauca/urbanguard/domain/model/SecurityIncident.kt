package co.edu.unicauca.urbanguard.domain.model

import java.time.Instant

/**
 * Novedad de vigilancia reportada por el guardia (UG-43).
 *
 * Aviso operativo con la foto obligatoria y punto de referencia: no es una
 * PQRS (sin radicado ni SLA de respuesta). La imagen se obtiene del backend.
 */
data class SecurityIncident(
    val id: String,
    val complexId: String,
    val category: IncidentCategory,
    val description: String,
    val imageUrl: String? = null,
    val hasImage: Boolean = true,
    val pointId: String? = null,
    val pointName: String? = null,
    val guardId: String,
    val executionId: String? = null,
    val createdAt: Instant,
)

/** Categoría operativa de la novedad, con su color. */
enum class IncidentCategory {
    SECURITY,
    INFRASTRUCTURE,
    LIGHTING,
    CLEANING,
    OTHER
}

/** Resultado de validar el formulario. */
enum class IncidentValidation {
    VALID,
    CATEGORY_REQUIRED,
    DESCRIPTION_REQUIRED,
    DESCRIPTION_TOO_SHORT,
    PHOTO_REQUIRED
}

private const val MIN_DESCRIPTION_LENGTH = 10

/**
 * Valida el formulario: categoría, descripción y foto requeridas.
 */
fun validateIncident(
    category: IncidentCategory?,
    description: String,
    photoPath: String?
): IncidentValidation {
    if (category == null) return IncidentValidation.CATEGORY_REQUIRED
    if (description.isBlank()) return IncidentValidation.DESCRIPTION_REQUIRED
    if (description.trim().length < MIN_DESCRIPTION_LENGTH) {
        return IncidentValidation.DESCRIPTION_TOO_SHORT
    }
    if (photoPath.isNullOrBlank()) return IncidentValidation.PHOTO_REQUIRED
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
