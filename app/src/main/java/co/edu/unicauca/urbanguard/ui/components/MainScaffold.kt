package co.edu.unicauca.urbanguard.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.navigation.AppNavGraph
import co.edu.unicauca.urbanguard.ui.navigation.Routes
import co.edu.unicauca.urbanguard.ui.navigation.adminNavItems
import co.edu.unicauca.urbanguard.ui.navigation.residentNavItems
import co.edu.unicauca.urbanguard.ui.navigation.vigilanteNavItems

private val bottomBarRoutes = setOf(
    Routes.HOME,
    Routes.CENSUS,
    Routes.SECURITY,
    Routes.NEWS,
    Routes.PROFILE,
    Routes.VISITORS,
    Routes.SUPPORT
)

@Composable
fun MainScaffold(
    navController: NavHostController,
    role: UserRole?
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val items = when (role) {
                    UserRole.ADMIN -> adminNavItems
                    UserRole.RESIDENT -> residentNavItems
                    UserRole.GUARD -> vigilanteNavItems
                    null -> emptyList()
                }
                if (items.isNotEmpty()) {
                    BottomNavBar(
                        items = items,
                        currentRoute = currentRoute,
                        onItemClick = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}