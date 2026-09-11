package co.edu.unicauca.urbanguard.ui.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.components.PlaceholderScreen
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
import co.edu.unicauca.urbanguard.ui.features.onboarding.LinkByCodeScreen

// Rutas de navegación — una constante por pantalla
object Routes {
    const val LOGIN = "login"
    const val CONJUNTO_CODE = "conjunto_code"
    const val ACTIVATE_CONJUNTO = "activate_conjunto"
    const val REGISTER_CONJUNTO = "register_conjunto/{conjuntoId}"
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
}

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
            val navigationViewModel: NavigationViewModel = hiltViewModel()
            val scope = rememberCoroutineScope()
            LoginScreen(
                onLoginSuccess = { role ->
                    when (role) {
                        UserRole.ADMIN -> {
                            // Tras login, consulta una sola vez si el Admin tiene
                            // conjunto vinculado y cachea el resultado en la sesión
                            // para decidir la pantalla inicial (UG-45).
                            scope.launch {
                                val hasConjunto = navigationViewModel.resolveAdminPostLogin()
                                navController.navigate(
                                    if (hasConjunto) Routes.HOME else Routes.ACTIVATE_CONJUNTO
                                ) {
                                    // Elimina el Login del backstack — el usuario
                                    // no puede volver atrás con el botón físico
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        }
                        // Residente: gestión de códigos para solicitar vinculación.
                        // El Vigilante NO accede a esta pantalla (regla de negocio).
                        UserRole.RESIDENT -> {
                            // No necesita corrutina — resolveResidentPostLogin() es síncrono
                            val hasConjunto = navigationViewModel.resolveResidentPostLogin()
                            navController.navigate(
                                if (hasConjunto) Routes.HOME else Routes.LINK_BY_CODE
                            ) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                        // TODO: definir destino inicial del Vigilante tras login
                        // (pendiente de decisión — no inventar).
                        UserRole.GUARD -> Unit
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
            val registerNavigationViewModel: NavigationViewModel = hiltViewModel()
            val registerScope = rememberCoroutineScope()
            RegisterScreen(
                onRegisterSuccess = { role ->
                    when (role) {
                        UserRole.ADMIN -> {
                            registerScope.launch {
                                val hasConjunto = registerNavigationViewModel.resolveAdminPostLogin()
                                navController.navigate(
                                    if (hasConjunto) Routes.HOME else Routes.ACTIVATE_CONJUNTO
                                ) {
                                    // Elimina Login y Register del backstack
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        }
                        // Por defecto los nuevos usuarios son Residente
                        UserRole.RESIDENT -> {
                            val hasConjunto = registerNavigationViewModel.resolveResidentPostLogin()
                            navController.navigate(
                                if (hasConjunto) Routes.HOME else Routes.LINK_BY_CODE
                            ) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                        UserRole.GUARD -> Unit
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
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
                onValidated = { conjuntoId ->
                    navController.navigate("register_conjunto/$conjuntoId") {
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
                navArgument("conjuntoId") { type = NavType.StringType }
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
            PendingRequestsScreen(
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
                    // La flecha superior regresa al login limpiando el backstack
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                },
                onLinkSuccess = {
                    // Por ahora vuelve al login — cuando exista
                    // el Home del residente, navegar ahí
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
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
    }
}