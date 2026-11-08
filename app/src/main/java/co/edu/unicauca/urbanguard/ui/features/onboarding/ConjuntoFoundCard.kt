package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.ConjuntoSearch
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBright
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Contenido del resultado de búsqueda: label de sección, resumen del conjunto,
 * campo de apartamento, botón de solicitud y su error.
 *
 * La aparición y desaparición la sigue gobernando el `AnimatedVisibility` de la
 * pantalla, así que aquí solo se declara la columna y sus hijos.
 */
@Composable
fun ConjuntoFoundResult(
    conjunto: ConjuntoSearch,
    apartamento: String,
    apartamentoError: String?,
    isRequesting: Boolean,
    requestError: String?,
    onApartamentoChange: (String) -> Unit,
    onRequest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Label sección
        Text(
            text = "CONJUNTO ENCONTRADO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = UgTextSecondary,
            letterSpacing = 0.8.sp
        )

        // Card del conjunto
        ConjuntoSummaryCard(conjunto)

        // Número de apartamento del futuro residente
        ApartamentoField(
            value = apartamento,
            error = apartamentoError,
            onValueChange = onApartamentoChange,
            onRequest = onRequest
        )

        // Botón solicitar vinculación
        RequestLinkButton(
            isRequesting = isRequesting,
            onClick = onRequest
        )

        // Error de solicitud
        if (requestError != null) {
            RequestErrorBanner(message = requestError)
        }
    }
}

/**
 * Resumen del conjunto encontrado: icono, nombre, direccion, ciudad y total de
 * miembros activos. No incluye el campo de apartamento ni el boton de solicitud,
 * que viven en [ApartamentoField] y [RequestLinkButton].
 */
@Composable
fun ConjuntoSummaryCard(conjunto: ConjuntoSearch) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, UgBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ícono + nombre + dirección
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        UgSuccessBg.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = UgEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = conjunto.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = UgNavy
                )
                Text(
                    text = conjunto.direccion,
                    fontSize = 13.sp,
                    color = UgTextSecondary
                )
                Text(
                    text = conjunto.ciudad,
                    fontSize = 13.sp,
                    color = UgTextSecondary
                )
            }
        }

        // Divider
        Divider(color = UgBorder)

        // Total miembros
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = UgTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${conjunto.totalMiembros} miembros activos",
                fontSize = 13.sp,
                color = UgTextSecondary
            )
        }
    }
}

/**
 * Campo del numero de apartamento del futuro residente, con su error de
 * validacion debajo.
 *
 * El `weight` no aplica aqui: el campo siempre ocupa todo el ancho de la tarjeta
 * y el `onDone` limpia el foco antes de disparar [onRequest], igual que antes
 * de la extraccion.
 */
@Composable
fun ApartamentoField(
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onRequest: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Número o identificador de apartamento") },
            placeholder = {
                Text(
                    "Ej: 302 o A-101",
                    color = UgEmerald,
                    fontSize = 13.sp
                )
            },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onRequest()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UgNavy,
                focusedLabelColor = UgNavy,
                cursorColor = UgNavy,
                errorBorderColor = UgErrorBright,
                errorLabelColor = UgErrorBright
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                fontSize = 12.sp,
                color = UgErrorBright
            )
        }
    }
}

/** Boton que envia la solicitud de vinculación, con su estado de carga. */
@Composable
fun RequestLinkButton(
    isRequesting: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isRequesting,
        colors = ButtonDefaults.buttonColors(
            containerColor = UgEmerald,
            disabledContainerColor = UgEmerald.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (isRequesting) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Enviando solicitud...",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Text(
                text = "Solicitar vinculación",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

/** Banner de error al enviar la solicitud de vinculación. */
@Composable
fun RequestErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgErrorBg, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = UgErrorText,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = message,
            fontSize = 12.sp,
            color = UgErrorText
        )
    }
}
