package co.edu.unicauca.urbanguard.ui.features.onboarding

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.ui.theme.*

@Composable
fun ActivateConjuntoScreen(
    onValidated: (conjuntoId: String?) -> Unit = {},
    viewModel: ActivateConjuntoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage = uiState.errorMessage

    LaunchedEffect(uiState.isValidated) {
        if (uiState.isValidated) {
            onValidated(uiState.conjuntoId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgSurfaceLight)
    ) {
        TopBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                value = uiState.code,
                onValueChange = { viewModel.onCodeChange(formatActivationCode(it)) },
                placeholder = {
                    Text(
                        "ACT-XXXX-YYYY",
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
                enabled = !uiState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = UgViolet,
                    unfocusedBorderColor = UgViolet.copy(alpha = 0.5f),
                    errorBorderColor = Color(0xFFDC2626),
                    cursorColor = UgViolet
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "FORMATO OBLIGATORIO: ",
                        fontSize = 11.sp,
                        color = UgTextSecondary
                    )
                    Text(
                        text = "ACT-XXXX-YYYY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UgViolet
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            WhyNecessaryCard()

            Spacer(Modifier.height(20.dp))

            val isEnabled = !uiState.isLoading && uiState.code.isNotBlank()

            Button(
                onClick = { viewModel.validateCode() },
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = UgViolet,
                    contentColor = Color.White,
                    disabledContainerColor = UgDisabled,
                    disabledContentColor = UgDisabledText
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isLoading) {
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

            Spacer(Modifier.height(16.dp))

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
                    color = UgViolet
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, color = UgDivider)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(UgViolet, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.GppGood,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Activar Condominio",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgTextPrimary
        )
    }
}

/**
 * TODO: Reemplazar por el asset real de ilustración "manos sosteniendo casa"
 * (no existe ningún drawable/vector con esa escena en el proyecto).
 * Se usa un ícono de Home como marcador de posición manteniendo la
 * composición (fondo teal redondeado + ícono central).
 */
@Composable
private fun HeroImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(UgHeroTeal, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(56.dp)
        )
    }
}

@Composable
private fun WhyNecessaryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgCardBg, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = UgViolet,
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
    val cleaned = input.uppercase().filter { it.isLetterOrDigit() }.take(11)
    val builder = StringBuilder()
    cleaned.forEachIndexed { index, char ->
        if (index == 3 || index == 8) builder.append('-')
        builder.append(char)
    }
    return builder.toString().take(13)
}