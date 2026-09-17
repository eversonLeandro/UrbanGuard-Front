package co.edu.unicauca.urbanguard.data.remote.api.dto

// TODO: cuando el backend lo soporte, reemplazar hasConjunto por la lista
// conjuntosAsociados: List<ConjuntoUserAssociationDto> (multi-conjunto).
// Hasta entonces este endpoint getStatus() sigue devolviendo el modelo actual.
data class OnboardingStatusDto(
    val hasConjunto: Boolean,
    val conjuntoId: String?
)