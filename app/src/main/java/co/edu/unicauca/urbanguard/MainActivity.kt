package co.edu.unicauca.urbanguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import co.edu.unicauca.urbanguard.ui.navigation.AppNavGraph
import co.edu.unicauca.urbanguard.ui.theme.UrbanGuardFrontendTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UrbanGuardFrontendTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    AppNavGraph()
                }
            }
        }
    }
}