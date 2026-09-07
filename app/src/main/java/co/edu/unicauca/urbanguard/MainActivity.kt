package co.edu.unicauca.urbanguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.features.auth.LoginScreen
import co.edu.unicauca.urbanguard.ui.features.auth.RegisterScreen
import co.edu.unicauca.urbanguard.ui.features.home.HomeScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.ConjuntoCodeScreen
import dagger.hilt.android.AndroidEntryPoint
import co.edu.unicauca.urbanguard.ui.theme.UrbanGuardFrontendTheme
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UrbanGuardFrontendTheme {
                var currentScreen by remember { mutableStateOf("login") }

                // Función reutilizable: decide a qué pantalla ir según el rol
                fun navigateByRole(role: UserRole) {
                    currentScreen = when (role) {
                        UserRole.ADMIN -> "onboarding"
                        else -> "home"
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            "login" -> {
                                LoginScreen(
                                    onLoginSuccess = { role -> navigateByRole(role) },
                                    onNavigateToOnboarding = { currentScreen = "onboarding" },
                                    onNavigateToRegister = { currentScreen = "register" }
                                )
                            }
                            "register" -> {
                                RegisterScreen(
                                    onRegisterSuccess = { role -> navigateByRole(role) },
                                    onNavigateToLogin = { currentScreen = "login" }
                                )
                            }
                            "onboarding" -> {
                                ConjuntoCodeScreen(
                                    onNavigateBack = {
                                        sessionManager.clearSession()
                                        currentScreen = "login"
                                    }
                                )
                            }
                            "home" -> {
                                HomeScreen(
                                    onLogout = { currentScreen = "login" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
