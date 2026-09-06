package co.edu.unicauca.urbanguard.ui.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.theme.*
import androidx.compose.runtime.LaunchedEffect


@Composable
fun LoginScreen(
    onLoginSuccess: (role: UserRole) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onLoginSuccess(UserRole.ADMINISTRADOR)
            // TODO: usar el rol real del uiState cuando el backend esté listo
        }
    }
    // TODO: cuando loginSuccess sea true, leer el rol desde SessionManager
    // (inyectado aparte) y llamar onLoginSuccess(rol) para navegar al Home correcto

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))

        Box(
            modifier = Modifier.size(44.dp).background(UgNavy, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("UG", color = UgEmerald, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text("UrbanGuard", color = UgNavy, fontWeight = FontWeight.Medium, fontSize = 16.sp)

        Spacer(Modifier.height(28.dp))

        Text("Bienvenido", color = UgNavy, fontWeight = FontWeight.Medium, fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start))
        Text("Inicia sesión para continuar", color = UgTextSecondary, fontSize = 13.sp,
            modifier = Modifier.align(Alignment.Start))

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = UgTextSecondary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.errorMessage != null,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = UgTextSecondary) },
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (uiState.isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = UgTextSecondary
                    )
                }
            },
            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            isError = uiState.errorMessage != null,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "¿Olvidaste tu contraseña?",
            color = UgNavy,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            modifier = Modifier.align(Alignment.End)
            // TODO: onClick -> flujo de recuperación (fuera del Sprint 1)
        )

        if (uiState.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(UgErrorBg, RoundedCornerShape(8.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = UgErrorText, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(uiState.errorMessage ?: "", color = UgErrorText, fontSize = 12.5.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = viewModel::onLoginClick,
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ingresando...")
            } else {
                Text("Iniciar sesión", fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(20.dp))

        Row {
            Text("¿Nuevo en UrbanGuard? ", color = UgTextSecondary, fontSize = 12.5.sp)
            Text(
                "Empezar aquí",
                color = UgEmerald,
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp,
                modifier = Modifier.clickable { onNavigateToOnboarding() }
            )
        }
    }
}