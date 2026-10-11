package co.edu.unicauca.urbanguard.ui.features.incidents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Icono y color por categoría de novedad, compartidos entre bandeja y
 * formulario para que se vean igual en todos lados.
 */
fun categoryIcon(category: IncidentCategory): ImageVector =
    when (category) {
        IncidentCategory.SECURITY -> Icons.Default.Security
        IncidentCategory.INFRASTRUCTURE -> Icons.Default.Build
        IncidentCategory.LIGHTING -> Icons.Default.Lightbulb
        IncidentCategory.CLEANING -> Icons.Default.CleaningServices
        IncidentCategory.OTHER -> Icons.Default.MoreHoriz
    }

fun categoryColor(category: IncidentCategory): Color =
    when (category) {
        IncidentCategory.SECURITY -> UgErrorText
        IncidentCategory.INFRASTRUCTURE -> UgNavy
        IncidentCategory.LIGHTING -> UgAmber
        IncidentCategory.CLEANING -> UgEmerald
        IncidentCategory.OTHER -> UgTextSecondary
    }
