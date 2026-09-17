package co.edu.unicauca.urbanguard.ui.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.features.auth.LoginScreen
import co.edu.unicauca.urbanguard.ui.features.home.HomeScreen
import co.edu.unicauca.urbanguard.ui.features.news.NewsFeedScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.ActivateConjuntoScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.ConjuntoCodeScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.PendingRequestsScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.RegisterConjuntoScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolHistoryScreen
import co.edu.unicauca.urbanguard.ui.features.profile.ProfileScreen
import co.edu.unicauca.urbanguard.ui.features.residential.CensusScreen
import kotlinx.coroutines.launch

// Rutas de navegación — una constante por pantalla
object Routes {
    const val LOGIN = "login"
    const val CONJUNTO_CODE = "conjunto_code"
    const val ACTIVATE_CONJUNTO = "activate_conjunto"
    const val REGISTER_CONJUNTO = "register_conjunto/{activationCode}"
    const val PENDING_REQUESTS = "pending_requests"
    const val HOME = "home"
    const val CENSUS = "census"
    const val SECURITY = "security"
    const val NEWS = "news"
    const val PROFILE = "profile"
    // TODO: agregar el resto de rutas aquí a medida que se implementen:
    // const val LINK_BY_CODE = "link_by_code"
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
                        UserRole.RESIDENT -> navController.navigate(Routes.CONJUNTO_CODE) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        // TODO: definir destino inicial del Vigilante tras login
                        // (pendiente de decisión — no inventar).
                        UserRole.GUARD -> Unit
                    }
                },
                onNavigateToOnboarding = {
                    // TODO: navegar a RegisterConjuntoScreen (UG-45)
                    navController.navigate(Routes.ACTIVATE_CONJUNTO)
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
            PendingRequestsScreen(
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

        composable(Routes.PROFILE) {
            ProfileScreen()
        }
    }
}