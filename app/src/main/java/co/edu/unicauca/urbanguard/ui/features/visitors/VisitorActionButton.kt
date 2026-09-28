package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgDisabledText
import co.edu.unicauca.urbanguard.ui.theme.UgDivider
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald

private val ACTION_BUTTON_HEIGHT = 50.dp
private val ACTION_BUTTON_CORNER = 10.dp
private val ACTION_BUTTON_TEXT_SIZE = 15.sp
private val PROGRESS_SIZE = 20.dp
private val PROGRESS_STROKE = 2.dp

/**
 * Botón principal de la funcionalidad de visitantes: generar un pase nuevo,
 * compartir el pase generado o reintentar. El color es siempre la acción
 * esmeralda de la app; lo que cambia es la etiqueta y si muestra progreso.
 */
@Composable
fun VisitorActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = UgEmerald,
            contentColor = Color.White,
            disabledContainerColor = UgDivider,
            disabledContentColor = UgDisabledText
        ),
        shape = RoundedCornerShape(ACTION_BUTTON_CORNER),
        modifier = modifier
            .fillMaxWidth()
            .height(ACTION_BUTTON_HEIGHT)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = PROGRESS_STROKE,
                modifier = Modifier.height(PROGRESS_SIZE)
            )
        } else {
            Text(
                text = text,
                fontSize = ACTION_BUTTON_TEXT_SIZE,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
