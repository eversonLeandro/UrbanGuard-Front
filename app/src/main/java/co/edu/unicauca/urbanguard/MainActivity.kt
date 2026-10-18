package co.edu.unicauca.urbanguard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import co.edu.unicauca.urbanguard.core.push.UrbanGuardMessagingService
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.ui.components.MainScaffold
import co.edu.unicauca.urbanguard.ui.navigation.routeForDeepLink
import co.edu.unicauca.urbanguard.ui.theme.UrbanGuardFrontendTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    // Deep link pendiente (tap en una notificación o esquema urbanguard://).
    // singleTop reutiliza la actividad: llega por onNewIntent en caliente.
    private var pendingDeepLink = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDeepLink.value = intent?.getStringExtra(UrbanGuardMessagingService.EXTRA_DEEP_LINK)
            ?: intent?.dataString
        enableEdgeToEdge()
        setContent {
            UrbanGuardFrontendTheme {
                val session by sessionManager.session.collectAsState()
                val navController = rememberNavController()

                // Permiso de notificaciones (Android 13+): sin él FCM entrega
                // pero el sistema no muestra nada.
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* best-effort: sin permiso simplemente no hay pushes visibles */ }
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Navegación por deep link del backend:
                // - urbanguard://visitors/{id} → lista de pases del residente.
                // - urbanguard://requests/{id} → estado de vinculación del
                //   solicitante: la decisión del administrador cambia su rol y
                //   sus conjuntos, y esa pantalla es la que los refleja.
                // - urbanguard://patrol/{roundId} → bandeja de notificaciones
                //   (aviso de recorrido no completado, HU-A): aún no hay
                //   endpoint GET por id de ronda, así que no hay detalle.
                val deepLink by pendingDeepLink
                LaunchedEffect(deepLink) {
                    val target = routeForDeepLink(deepLink)
                    if (target != null) {
                        navController.navigate(target) {
                            launchSingleTop = true
                        }
                    }
                    pendingDeepLink.value = null
                }

                MainScaffold(
                    navController = navController,
                    role = session?.role
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink.value = intent.getStringExtra(UrbanGuardMessagingService.EXTRA_DEEP_LINK)
            ?: intent.dataString
    }
}
