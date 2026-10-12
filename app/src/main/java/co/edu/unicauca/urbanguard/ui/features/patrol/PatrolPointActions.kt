package co.edu.unicauca.urbanguard.ui.features.patrol

/**
 * Acciones de la tarjeta de un punto de control.
 *
 * Vive en archivo propio: detekt exige que la declaración coincida con el
 * nombre del archivo, y agrupadas evitan el máximo de parámetros por función.
 */
data class PatrolPointActions(
    val onEdit: () -> Unit,
    val onMove: (Int) -> Unit,
    val onToggleActive: (Boolean) -> Unit,
    val onDelete: () -> Unit
)
