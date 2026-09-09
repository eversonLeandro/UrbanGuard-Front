package co.edu.unicauca.urbanguard.ui.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.unicauca.urbanguard.ui.features.auth.LoginScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.ConjuntoCodeScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.LinkByCodeScreen

// Rutas de navegación — una constante por pantalla
object Routes {
    const val LOGIN = "login"
    const val CONJUNTO_CODE = "conjunto_code"
    // TODO: agregar el resto de rutas aquí a medida que se implementen:
    // const val ACTIVATE_CONJUNTO = "activate_conjunto"
    // const val PENDING_REQUESTS = "pending_requests"
    const val LINK_BY_CODE = "link_by_code"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        // Para ver ConjuntoCodeScreen directamente:
        //startDestination = Routes.CONJUNTO_CODE
        // Ver Login (por defecto):
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    // TODO: navegar según el rol cuando haya más pantallas
                    // Por ahora todos van a ConjuntoCodeScreen para probar
                    navController.navigate(Routes.CONJUNTO_CODE) {
                        // Elimina el Login del backstack — el usuario
                        // no puede volver atrás con el botón físico
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    // TODO: navegar a ActivateConjuntoScreen (UG-45)
                    navController.navigate(Routes.CONJUNTO_CODE)
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
        composable(Routes.LINK_BY_CODE) {
            LinkByCodeScreen(
                onNavigateBack = { navController.popBackStack() },
                onLinkSuccess = {
                    // TODO: navegar al home del residente cuando esté listo
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

