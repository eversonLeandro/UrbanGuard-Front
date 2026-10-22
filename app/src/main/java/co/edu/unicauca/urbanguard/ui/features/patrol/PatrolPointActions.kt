package co.edu.unicauca.urbanguard.ui.features.patrol

/** Acciones de la tarjeta de un punto de control. */
data class PatrolPointActions(
    val onEdit: () -> Unit,
    val onToggleActive: (Boolean) -> Unit
)
