package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.ui.theme.*

@Composable
fun LinkByCodeScreen(
    onNavigateBack: () -> Unit,
    onLinkSuccess: () -> Unit,
    viewModel: LinkByCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Cuando la solicitud es exitosa navega al home del residente
    LaunchedEffect(uiState.requestSuccess) {
        if (uiState.requestSuccess) {
            onLinkSuccess()
        }
    }

    // Si el requestSuccess está activo mostramos la pantalla de éxito
    if (uiState.requestSuccess && uiState.linkRequest != null) {
        LinkSuccessScreen(
            conjuntoNombre = uiState.conjuntoFound?.nombre ?: "",
            direccion = uiState.conjuntoFound?.direccion ?: "",
            ciudad = uiState.conjuntoFound?.ciudad ?: "",
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UgNavy)
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = UgEmerald
                )
            }
            Text(
                text = "Unirse a conjunto",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = uiState.code,
                    onValueChange = viewModel::onCodeChange,
                    label = { Text("Código de vinculación") },
                    placeholder = { Text("Ej: URB-4X9K", color = UgEmerald, fontSize = 13.sp) },
                    singleLine = true,
                    isError = uiState.codeError != null || uiState.searchError != null,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.onSearchClick()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UgNavy,
                        focusedLabelColor = UgNavy,
                        cursorColor = UgNavy,
                        errorBorderColor = Color(0xFFEF4444),
                        errorLabelColor = Color(0xFFEF4444)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Texto de ayuda / error de validación
                if (uiState.codeError != null) {
                    Text(
                        text = uiState.codeError!!,
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            // Botón buscar con animación de loading
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onSearchClick()
                },
                enabled = !uiState.isSearching,
                colors = ButtonDefaults.buttonColors(
                    containerColor = UgNavy,
                    disabledContainerColor = UgNavy.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isSearching) {
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

            // Banner de error de búsqueda
            AnimatedVisibility(
                visible = uiState.searchError != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                if (uiState.searchError != null) {
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
                            text = uiState.searchError!!,
                            fontSize = 13.sp,
                            color = UgErrorText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Resultado: conjunto encontrado
            AnimatedVisibility(
                visible = uiState.conjuntoFound != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                uiState.conjuntoFound?.let { conjunto ->
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
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
                                            Color(0xFFE8F5E9),
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
                            Divider(color = Color(0xFFE2E8F0))

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

                            // Botón solicitar vinculación
                            Button(
                                onClick = viewModel::onRequestLinkClick,
                                enabled = !uiState.isRequesting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UgEmerald,
                                    disabledContainerColor = UgEmerald.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (uiState.isRequesting) {
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

                            // Error de solicitud
                            if (uiState.requestError != null) {
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
                                        text = uiState.requestError!!,
                                        fontSize = 12.sp,
                                        color = UgErrorText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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
                .background(Color(0xFFFFF3E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFFF57C00),
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            SolicitudInfoRow(label = "Conjunto", value = conjuntoNombre)
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            SolicitudInfoRow(label = "Dirección", value = "$direccion, $ciudad")
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            SolicitudInfoRow(label = "Fecha de solicitud", value = fechaSolicitud)
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
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
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PENDIENTE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF57C00),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Banner informativo amber
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(16.dp).padding(top = 1.dp)
            )
            Text(
                text = "El administrador revisará tu solicitud. Recibirás una notificación con el resultado.",
                fontSize = 13.sp,
                color = Color(0xFFE65100),
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.weight(1f))

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