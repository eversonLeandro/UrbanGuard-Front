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
        pendingDeepLink.value = deepLinkFrom(intent)
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
                // - urbanguard://pqrs/{pqrsId} → detalle de la PQRS propia, con
                //   la respuesta del administrador y el historial de estados.
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
        pendingDeepLink.value = deepLinkFrom(intent)
    }

    /**
     * Con la app en primer plano el servicio de FCM publica la notificación y
     * añade [UrbanGuardMessagingService.EXTRA_DEEP_LINK]. En segundo plano o
     * cerrada, el sistema muestra la notificación y el toque abre esta actividad
     * con el `data` del push como extras: la clave que envía el backend es
     * `deepLink`. El esquema `urbanguard://` queda como tercera vía.
     */
    private fun deepLinkFrom(intent: Intent?): String? =
        intent?.getStringExtra(UrbanGuardMessagingService.EXTRA_DEEP_LINK)
            ?: intent?.getStringExtra(FCM_DEEP_LINK_EXTRA)
            ?: intent?.dataString

    private companion object {
        const val FCM_DEEP_LINK_EXTRA = "deepLink"
    }
}
