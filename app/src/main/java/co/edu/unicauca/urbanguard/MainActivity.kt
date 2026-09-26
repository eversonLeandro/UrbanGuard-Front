package co.edu.unicauca.urbanguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.ui.components.MainScaffold
import co.edu.unicauca.urbanguard.ui.theme.UrbanGuardFrontendTheme
import dagger.hilt.android.AndroidEntryPoint
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
                val session by sessionManager.session.collectAsState()
                val navController = rememberNavController()

                MainScaffold(
                    navController = navController,
                    role = session?.role
                )
            }
        }
    }
}
