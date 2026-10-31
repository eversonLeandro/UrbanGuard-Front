package co.edu.unicauca.urbanguard.ui.features.pqrs

import co.edu.unicauca.urbanguard.domain.model.PqrsType

data class CreatePqrsUiState(
    val type: PqrsType? = null,
    val subject: String = "",
    val description: String = "",
    val typeError: String? = null,
    val subjectError: String? = null,
    val descriptionError: String? = null,
    val submitError: String? = null,
    val requiresNewAttempt: Boolean = false,
    val completedElsewhere: Boolean = false,
    val isSubmitting: Boolean = false
)

