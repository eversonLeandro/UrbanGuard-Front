package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgBorderSoft
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBright
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.ui.theme.UgWarningBg
import co.edu.unicauca.urbanguard.ui.theme.UgWarningBorder
import co.edu.unicauca.urbanguard.ui.theme.UgWarningText

@Composable
fun LinkByCodeScreen(
    onNavigateBack: () -> Unit,
    onLinkSuccess: () -> Unit,
    viewModel: LinkByCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // El requestSuccess se muestra como pantalla de éxito y la navegación
    // la dispara el botón "Volver al inicio" (no se auto-navega).

    // Si el requestSuccess está activo mostramos la pantalla de éxito
    if (uiState.requestSuccess && uiState.linkRequest != null) {
        LinkSuccessScreen(
            conjuntoNombre = uiState.conjuntoFound?.nombre ?: "",
            direccion = uiState.conjuntoFound?.direccion ?: "",
            ciudad = uiState.conjuntoFound?.ciudad ?: "",
            unidad = uiState.apartamento,
            fechaSolicitud = uiState.linkRequest!!.fechaSolicitud,
            onNavigateHome = onLinkSuccess
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        // Header
        UrbanGuardTopBar(
            title = "Unirse a conjunto",
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instrucción
            Text(
                text = "Ingresa el código que te compartió el administrador de tu conjunto",
                fontSize = 14.sp,
                color = UgTextSecondary,
                lineHeight = 20.sp
            )

            // Input del código
            LinkCodeField(
                code = uiState.code,
                isError = uiState.codeError != null || uiState.searchError != null,
                codeError = uiState.codeError,
                onCodeChange = viewModel::onCodeChange,
                onSearch = viewModel::onSearchClick
            )

            // Botón buscar con animación de loading
            SearchConjuntoButton(
                isSearching = uiState.isSearching,
                onSearch = viewModel::onSearchClick
            )

            // Banner de error de búsqueda
            AnimatedVisibility(
                visible = uiState.searchError != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                if (uiState.searchError != null) {
                    SearchErrorBanner(message = uiState.searchError!!)
                }
            }

            // Resultado: conjunto encontrado
            AnimatedVisibility(
                visible = uiState.conjuntoFound != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                uiState.conjuntoFound?.let { conjunto ->
                    ConjuntoFoundResult(
                        conjunto = conjunto,
                        apartamento = uiState.apartamento,
                        apartamentoError = uiState.apartamentoError,
                        isRequesting = uiState.isRequesting,
                        requestError = uiState.requestError,
                        onApartamentoChange = viewModel::onApartamentoChange,
                        onRequest = viewModel::onRequestLinkClick
                    )
                }
            }
        }
    }
}

/**
 * Campo del código de vinculación con su error de validación debajo.
 *
 * El `onSearch` del teclado se comporta igual que el botón: limpia el foco y
 * dispara la búsqueda.
 */
@Composable
private fun LinkCodeField(
    code: String,
    isError: Boolean,
    codeError: String?,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text("Código de vinculación") },
            placeholder = {
                Text(
                    "Ingresa el código que te dio tu administrador",
                    color = UgEmerald,
                    fontSize = 13.sp
                )
            },
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSearch()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UgNavy,
                focusedLabelColor = UgNavy,
                cursorColor = UgNavy,
                errorBorderColor = UgErrorBright,
                errorLabelColor = UgErrorBright
            ),
            supportingText = {
                // El backend genera 6 caracteres sin separadores, así que se
                // explica el formato real en vez de mostrar un ejemplo inventado.
                Text("Son 6 caracteres, sin guiones ni espacios.", fontSize = 12.sp)
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Texto de ayuda / error de validación
        if (codeError != null) {
            Text(
                text = codeError,
                fontSize = 12.sp,
                color = UgErrorBright
            )
        }
    }
}

/**
 * Botón de búsqueda con su animación de loading. Limpia el foco antes de
 * delegar en [onSearch].
 */
@Composable
private fun SearchConjuntoButton(
    isSearching: Boolean,
    onSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Button(
        onClick = {
            focusManager.clearFocus()
            onSearch()
        },
        enabled = !isSearching,
        colors = ButtonDefaults.buttonColors(
            containerColor = UgNavy,
            disabledContainerColor = UgNavy.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (isSearching) {
            // Animación de carga dentro del botón
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Buscando conjunto...",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Buscar conjunto",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }
    }
}

/** Banner de error de búsqueda. */
@Composable
private fun SearchErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgErrorBg, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = UgErrorText,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = message,
            fontSize = 13.sp,
            color = UgErrorText,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────
// Pantalla de éxito — solicitud enviada
// ─────────────────────────────────────────────

@Composable
private fun LinkSuccessScreen(
    conjuntoNombre: String,
    direccion: String,
    ciudad: String,
    unidad: String,
    fechaSolicitud: String,
    onNavigateHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))

        // Ícono escudo en círculo naranja/amber — igual al prototipo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(UgWarningBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = UgWarningText,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Solicitud enviada",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = UgNavy
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Tu solicitud está pendiente de aprobación por el administrador",
            fontSize = 14.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(28.dp))

        // Card resumen de la solicitud
        SolicitudSummaryCard(
            conjuntoNombre = conjuntoNombre,
            direccion = direccion,
            ciudad = ciudad,
            unidad = unidad,
            fechaSolicitud = fechaSolicitud
        )

        Spacer(Modifier.height(16.dp))

        // Banner informativo amber
        PendingReviewNotice()

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))

        // Botón volver al inicio
        OutlinedButton(
            onClick = onNavigateHome,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = UgNavy),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(UgNavy)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Volver al inicio",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

/**
 * Resumen de lo que se envió, con el estado pendiente al final. El badge va en
 * la última fila y no en un composable aparte porque comparte `Row` con la
 * etiqueta "Estado" y partirlo obligaría a duplicar el peso de ambas columnas.
 */
@Composable
private fun SolicitudSummaryCard(
    conjuntoNombre: String,
    direccion: String,
    ciudad: String,
    unidad: String,
    fechaSolicitud: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, UgBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        SolicitudInfoRow(label = "Conjunto", value = conjuntoNombre)
        Divider(color = UgBorderSoft, thickness = 1.dp)
        SolicitudInfoRow(label = "Dirección", value = "$direccion, $ciudad")
        Divider(color = UgBorderSoft, thickness = 1.dp)
        SolicitudInfoRow(label = "Apartamento", value = "N.º $unidad")
        Divider(color = UgBorderSoft, thickness = 1.dp)
        SolicitudInfoRow(label = "Fecha de solicitud", value = fechaSolicitud)
        Divider(color = UgBorderSoft, thickness = 1.dp)
        // Fila de estado con badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Estado",
                fontSize = 13.sp,
                color = UgTextSecondary
            )
            Box(
                modifier = Modifier
                    .background(UgWarningBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "PENDIENTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = UgWarningText,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/** Aviso de que la solicitud quedó esperando respuesta del administrador. */
@Composable
private fun PendingReviewNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgWarningBg, RoundedCornerShape(8.dp))
            .border(1.dp, UgWarningBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = UgWarningText,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
        )
        Text(
            text = "El administrador revisará tu solicitud. Recibirás una notificación con el resultado.",
            fontSize = 13.sp,
            color = UgWarningText,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SolicitudInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = UgTextSecondary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = UgNavy,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}
