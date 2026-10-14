package co.edu.unicauca.urbanguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.ui.theme.UgWarningText

/**
 * Etiqueta de estado compartida: píldora con el tono semántico de [tone].
 *
 * Centraliza el estilo que antes repetían las pantallas (fondo tenue del color
 * del texto) para que el historial, el detalle del turno y el resumen del
 * vigilante muestren el mismo badge.
 */
@Composable
fun StatusBadge(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier
) {
    val color = statusToneColor(tone)
    Text(
        text = text,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

/** Color del tema asociado al tono. */
fun statusToneColor(tone: StatusTone): Color = when (tone) {
    StatusTone.SUCCESS -> UgEmerald
    StatusTone.WARNING -> UgWarningText
    StatusTone.ERROR -> UgErrorText
    StatusTone.NEUTRAL -> UgTextSecondary
}
