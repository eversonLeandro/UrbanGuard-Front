package co.edu.unicauca.urbanguard.ui.components

/**
 * Tono semántico de una etiqueta de estado, independiente del texto y del
 * modelo de cada pantalla. [StatusBadge] lo traduce a colores del tema.
 */
enum class StatusTone {
    /** Éxito o cumplimiento (verde). */
    SUCCESS,

    /** Advertencia o pendiente (naranja). */
    WARNING,

    /** Error o incumplimiento (rojo). */
    ERROR,

    /** Neutro o sin estado (gris). */
    NEUTRAL
}
