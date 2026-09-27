package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgDisabledText
import co.edu.unicauca.urbanguard.ui.theme.UgDivider
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBright
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun ActivateConjuntoScreen(
    onNavigateBack: () -> Unit = {},
    onValidated: (activationCode: String) -> Unit = {},
    viewModel: ActivateConjuntoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage = uiState.errorMessage

    LaunchedEffect(uiState.isValidated) {
        if (uiState.isValidated) {
            uiState.activationCode?.let { onValidated(it) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Activar Condominio",
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActivationHeader()

            ActivationCodeField(
                value = uiState.code,
                onValueChange = { viewModel.onCodeChange(formatActivationCode(it)) },
                errorMessage = errorMessage,
                isLoading = uiState.isLoading
            )

            Spacer(Modifier.height(20.dp))

            WhyNecessaryCard()

            Spacer(Modifier.height(20.dp))

            ValidateCodeButton(
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading && uiState.code.isNotBlank(),
                onClick = viewModel::validateCode
            )

            Spacer(Modifier.height(16.dp))

            SupportContactRow()

            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Imagen, titulo y descripcion de la pantalla. */
@Composable
private fun ActivationHeader() {
    HeroImage()

    Spacer(Modifier.height(20.dp))

    Text(
        text = "Activar Licencia de Proyecto",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = UgTextPrimary,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Ingresa el código proporcionado por el dueño del proyecto para habilitar tu conjunto.",
        fontSize = 13.sp,
        color = UgTextSecondary,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp
    )

    Spacer(Modifier.height(24.dp))
}

/**
 * Campo del codigo de licencia, con su etiqueta y el mensaje de error o la
 * pista de formato.
 *
 * No envuelve nada: emite el OutlinedTextField, un Spacer y luego el texto
 * condicional, asi que conserva su lugar dentro de la Column centrada.
 */
@Composable
private fun ActivationCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    isLoading: Boolean
) {
    // Label "Código de Licencia"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = UgTextPrimary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Código de Licencia",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgTextPrimary
        )
    }

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                "Ingresa tu código de licencia",
                fontSize = 16.sp,
                color = UgTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = UgTextPrimary
        ),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        isError = errorMessage != null,
        enabled = !isLoading,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = UgNavy,
            unfocusedBorderColor = UgBorder,
            errorBorderColor = UgErrorBright,
            cursorColor = UgNavy
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))

    if (errorMessage != null) {
        Text(
            text = errorMessage,
            fontSize = 12.sp,
            color = UgErrorText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
    } else {
        Text(
            text = "EL CÓDIGO ES ALFANUMÉRICO: usa letras y números.",
            fontSize = 11.sp,
            color = UgTextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Boton de validacion: muestra spinner mientras [isLoading] y el texto si no. */
@Composable
private fun ValidateCodeButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = UgEmerald,
            contentColor = Color.White,
            disabledContainerColor = UgDivider,
            disabledContentColor = UgDisabledText
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.height(20.dp)
            )
        } else {
            Text(
                text = "Validar Código",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Aviso de contacto con soporte.
 *
 * Pendiente: "Contactar soporte" es texto decorativo, sin accion asociada.
 */
@Composable
private fun SupportContactRow() {
    Row {
        Text(
            text = "¿Tienes problemas con tu código? ",
            fontSize = 12.sp,
            color = UgTextSecondary
        )
        Text(
            text = "Contactar soporte",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgNavy
        )
    }
}


/**
 * Muestra el logo de marca centrado sobre el fondo de la pantalla.
 * El recurso ya no tiene fondo blanco (transparencia real), asi que se
 * integra directamente con la superficie sin ningun cuadro visible.
 */
@Composable
private fun HeroImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo_u),
            contentDescription = "Logo UrbanGuard",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(150.dp)
        )
    }
}

@Composable
private fun WhyNecessaryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgInputBg, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = UgEmerald,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "¿Por qué es necesario?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgTextPrimary
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "La licencia vincula su administración con los protocolos de seguridad centralizados de GuardPass.",
            fontSize = 12.sp,
            color = UgTextSecondary,
            lineHeight = 17.sp
        )
    }
}

private fun formatActivationCode(input: String): String {
    return input.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(20)
}
