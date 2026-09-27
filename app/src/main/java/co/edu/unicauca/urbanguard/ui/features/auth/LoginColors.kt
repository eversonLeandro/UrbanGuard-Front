package co.edu.unicauca.urbanguard.ui.features.auth

import androidx.compose.ui.graphics.Color

/**
 * Colores de la pantalla de login segun el tema activo.
 *
 * Se agrupan en una data class porque los cinco se eligen con el mismo ternario
 * de tema claro/oscuro y asi los subcomponentes del formulario no necesitan
 * cinco parametros de color que solo existen para pasarselos.
 *
 * Tiene su propio archivo para no competir con el nombre del composable que la
 * consume, que es lo que exige la regla MatchingDeclarationName.
 */
internal data class LoginColors(
    val cardColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val inputBg: Color,
    val borderColor: Color
)
