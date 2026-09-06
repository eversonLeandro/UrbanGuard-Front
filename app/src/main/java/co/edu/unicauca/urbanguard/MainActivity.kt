package co.edu.unicauca.urbanguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import co.edu.unicauca.urbanguard.ui.features.auth.LoginScreen
import co.edu.unicauca.urbanguard.ui.features.auth.RegisterScreen
import co.edu.unicauca.urbanguard.ui.theme.UrbanGuardFrontendTheme

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UrbanGuardFrontendTheme {
                var currentScreen by remember { mutableStateOf("login") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "login" -> {
                            LoginScreen(
                                onLoginSuccess = { role -> /* TODO: navegar según rol */ },
                                onNavigateToOnboarding = { /* TODO: navegar a onboarding */ },
                                onNavigateToRegister = { currentScreen = "register" }
                            )
                        }
                        "register" -> {
                            RegisterScreen(
                                onRegisterSuccess = { role -> /* TODO: navegar según rol */ },
                                onNavigateToLogin = { currentScreen = "login" }
                            )
                        }
                    }
                }
            }
        }
    }
}

