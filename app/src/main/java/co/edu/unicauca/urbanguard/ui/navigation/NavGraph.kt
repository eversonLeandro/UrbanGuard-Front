package co.edu.unicauca.urbanguard.ui.navigation
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.components.PlaceholderScreen
import co.edu.unicauca.urbanguard.ui.features.admin.SuperAdminScreen
import co.edu.unicauca.urbanguard.ui.features.auth.LoginScreen
import co.edu.unicauca.urbanguard.ui.features.auth.RegisterScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SupportAgent
import co.edu.unicauca.urbanguard.ui.features.home.HomeScreen
import co.edu.unicauca.urbanguard.ui.features.news.NewsFeedScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.ActivateConjuntoScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.ConjuntoCodeScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.LinkRequestDetailScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.PendingRequestsScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.RegisterConjuntoScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolHistoryScreen
import co.edu.unicauca.urbanguard.ui.features.profile.ProfileScreen
import co.edu.unicauca.urbanguard.ui.features.residential.CensusScreen
import kotlinx.coroutines.launch
import co.edu.unicauca.urbanguard.ui.features.onboarding.ConjuntoSelectionScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.LinkByCodeScreen

// Rutas de navegación — una constante por pantalla
object Routes {
    const val LOGIN = "login"
    const val CONJUNTO_CODE = "conjunto_code"
    const val ACTIVATE_CONJUNTO = "activate_conjunto"
    const val REGISTER_CONJUNTO = "register_conjunto/{activationCode}"
    const val PENDING_REQUESTS = "pending_requests"
    const val REQUEST_DETAIL = "link_request_detail/{requestId}"
    const val HOME = "home"
    const val CENSUS = "census"
    const val SECURITY = "security"
    const val NEWS = "news"
    const val PROFILE = "profile"
    const val VISITORS = "visitors"
    const val SUPPORT = "support"
    const val CONJUNTO_INFO = "conjunto_info"
    const val RESERVATIONS = "reservations"
    const val PACKAGES = "packages"
    const val NOTIFICATIONS = "notifications"
    // TODO: agregar el resto de rutas aquí a medida que se implementen:
    const val LINK_BY_CODE = "link_by_code"
    const val REGISTER = "register"
    const val CONJUNTO_SELECTION = "conjunto_selection"
    const val ADMIN = "admin"
}

// Clave en el savedStateHandle de PENDING_REQUESTS para recibir el
// resultado de la decisión (APROBADA/RECHAZADA) tomada en el detalle.
private const val KEY_REVIEW_RESULT = "review_result"

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    // El rol de Auth0 determina el destino directamente.
                    // USER (centinela) → el usuario debe seleccionar o crear un conjunto.
                    // SUPERADMIN → panel global de plataforma (Capa 2.3, D3).
                    // ADMIN / RESIDENT / GUARD ya tienen conjunto activo en Auth0 → Home.
                    // La carga real de conjuntos ocurre en ConjuntoSelectionViewModel.
                    val destination = when (role) {
                        UserRole.USER -> Routes.CONJUNTO_SELECTION
                        UserRole.SUPERADMIN -> Routes.ADMIN
                        else -> Routes.HOME
                    }
                    Log.d("NavGraph", "=== [LOGIN NAVIGATION] role: $role -> destino: $destination ===")
                    navController.navigate(destination) {
                        // Elimina el Login del backstack — el usuario
                        // no puede volver atrás con el botón físico
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    // TODO: navegar a RegisterConjuntoScreen (UG-45)
                    navController.navigate(Routes.ACTIVATE_CONJUNTO)
                },
                onNavigateToRegister = {
                    // "¿Nuevo en UrbanGuard? Empezar aquí" → formulario de registro
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // Registro de nuevo usuario ("Empezar aquí" desde el Login)
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { _ ->
                    // Los nuevos registros llegan como USER en Auth0 (sin conjunto asignado).
                    // Se navega directo al selector; ConjuntoSelectionViewModel cargará
                    // la lista real de conjuntos desde el backend al entrar a la pantalla.
                    navController.navigate(Routes.CONJUNTO_SELECTION) {
                        // Elimina Login y Register del backstack
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Selector de conjuntos — pantalla intermedia tras login/registro
        // cuando el usuario tiene rol USER (o quiere cambiar de conjunto activo).
        composable(Routes.CONJUNTO_SELECTION) {
            ConjuntoSelectionScreen(
                onConjuntoSelected = { conjuntoId ->
                    // setActiveConjunto ya lo hizo el ViewModel (llamado onConjuntoSelected),
                    // aquí solo se navega al Home del rol correspondiente a ese conjunto.
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onCreateConjunto = {
                    navController.navigate(Routes.ACTIVATE_CONJUNTO)
                },
                onJoinByCode = {
                    navController.navigate(Routes.LINK_BY_CODE)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.CONJUNTO_CODE) {
            ConjuntoCodeScreen(
                onNavigateBack = {
                    navController.popBackStack()

                }
            )
        }

        composable(Routes.ACTIVATE_CONJUNTO) {
            ActivateConjuntoScreen(
                onValidated = { activationCode ->
                    navController.navigate("register_conjunto/$activationCode") {
                        // No se puede volver de Register a Activate:
                        // se elimina Activate del backstack
                        popUpTo(Routes.ACTIVATE_CONJUNTO) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.REGISTER_CONJUNTO,
            arguments = listOf(
                navArgument("activationCode") { type = NavType.StringType }
            )
        ) {
            RegisterConjuntoScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER_CONJUNTO) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onOpenConjuntoCode = {
                    navController.navigate(Routes.CONJUNTO_CODE)
                },
                onOpenPendingRequests = {
                    navController.navigate(Routes.PENDING_REQUESTS)
                }
            )
        }

        composable(Routes.PENDING_REQUESTS) {
            // Resultado devuelto por el detalle tras aprobar/rechazar.
            // Si no es null se muestra el mensaje de confirmación y se recarga la lista.
            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
            val reviewResult by savedStateHandle
                ?.getStateFlow<String?>(KEY_REVIEW_RESULT, null)
                ?.collectAsStateWithLifecycle()
                ?: remember { mutableStateOf<String?>(null) }

            PendingRequestsScreen(
                reviewResult = reviewResult,
                onReviewResultHandled = {
                    savedStateHandle?.set(KEY_REVIEW_RESULT, null)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenDetail = { requestId ->
                    navController.navigate("link_request_detail/$requestId")
                }
            )
        }

        composable(
            route = Routes.REQUEST_DETAIL,
            arguments = listOf(
                navArgument("requestId") { type = NavType.StringType }
            )
        ) {
            LinkRequestDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReviewFinished = { result ->
                    // Se deja el resultado en el savedStateHandle de la lista
                    // para que al volver muestre la confirmación y actualice el estado.
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KEY_REVIEW_RESULT, result)
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CENSUS) {
            CensusScreen()
        }

        composable(Routes.SECURITY) {
            PatrolHistoryScreen()
        }

        composable(Routes.NEWS) {
            NewsFeedScreen()
        }

        composable(Routes.VISITORS) {
            PlaceholderScreen(
                title = "Visitantes",
                icon = Icons.Default.People
            )
        }
        composable(Routes.LINK_BY_CODE) {
            LinkByCodeScreen(
                onNavigateBack = {
                    navController.navigate(Routes.CONJUNTO_SELECTION) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                },
                onLinkSuccess = {
                    // La solicitud está pendiente de aprobación — vuelve al selector
                    navController.navigate(Routes.CONJUNTO_SELECTION) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.SUPPORT) {
            PlaceholderScreen(
                title = "Soporte",
                icon = Icons.Default.SupportAgent
            )
        }

        composable(Routes.CONJUNTO_INFO) {
            PlaceholderScreen(
                title = "Información del conjunto",
                icon = Icons.Default.Apartment
            )
        }

        composable(Routes.RESERVATIONS) {
            PlaceholderScreen(
                title = "Reservar espacios comunes",
                icon = Icons.Default.CalendarMonth
            )
        }

        composable(Routes.PACKAGES) {
            PlaceholderScreen(
                title = "Mis paquetes",
                icon = Icons.Default.Inventory2
            )
        }

        composable(Routes.NOTIFICATIONS) {
            PlaceholderScreen(
                title = "Notificaciones",
                icon = Icons.Default.Notifications
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen()
        }

        composable(Routes.ADMIN) {
            SuperAdminScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}