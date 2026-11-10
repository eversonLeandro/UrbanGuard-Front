package co.edu.unicauca.urbanguard.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.navigation.AppNavGraph
import co.edu.unicauca.urbanguard.ui.navigation.Routes
import co.edu.unicauca.urbanguard.ui.navigation.adminNavItems
import co.edu.unicauca.urbanguard.ui.navigation.guardNavItems
import co.edu.unicauca.urbanguard.ui.navigation.isResidentsSection
import co.edu.unicauca.urbanguard.ui.navigation.residentNavItems

private val bottomBarRoutes = setOf(
    Routes.HOME,
    Routes.SECURITY,
    Routes.NEWS,
    Routes.PROFILE,
    Routes.VISITORS,
    Routes.SUPPORT,
    Routes.GUARD_HOME,
    Routes.GUARD_VISITANTES,
    Routes.GUARD_SEGURIDAD,
    Routes.GUARD_PERFIL
)

@Composable
fun MainScaffold(
    navController: NavHostController,
    role: UserRole?
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val residentsSection = role == UserRole.ADMIN && isResidentsSection(currentRoute)
    val showBottomBar = currentRoute in bottomBarRoutes || residentsSection
    val activeRole = rememberUpdatedState(role)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val items = when (role) {
                    UserRole.ADMIN -> adminNavItems
                    UserRole.RESIDENT -> residentNavItems
                    UserRole.USER -> residentNavItems
                    UserRole.GUARD -> guardNavItems
                    UserRole.SUPERADMIN -> emptyList()
                    null -> emptyList()
                }
                if (items.isNotEmpty()) {
                    BottomNavBar(
                        items = items,
                        currentRoute = if (residentsSection) Routes.ADMIN_RESIDENTS_MENU else currentRoute,
                        onItemClick = { route ->
                            navController.navigateBottomTab(route, currentRoute)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            activeRole = activeRole,
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

internal fun NavHostController.navigateBottomTab(route: String, currentRoute: String?) {
    val fromResidents = isResidentsSection(currentRoute)
    val openResidents = route == Routes.ADMIN_RESIDENTS_MENU
    if (openResidents && fromResidents) {
        if (currentRoute == route || popBackStack(route, inclusive = false)) return
    }
    navigate(route) {
        if (openResidents || fromResidents) {
            // After a fresh login the graph still declares LOGIN, but HOME is on the stack.
            popUpTo(Routes.HOME) { saveState = !fromResidents }
        } else {
            popUpTo(graph.findStartDestination().id) { saveState = true }
        }
        launchSingleTop = true
        restoreState = !openResidents
    }
}
