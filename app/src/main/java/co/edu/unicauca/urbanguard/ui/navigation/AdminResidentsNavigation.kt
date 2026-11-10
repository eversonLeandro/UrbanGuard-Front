package co.edu.unicauca.urbanguard.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.components.ErrorView
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.features.admin.AdminResidentsMenuScreen
import co.edu.unicauca.urbanguard.ui.features.residential.CensusScreen
import co.edu.unicauca.urbanguard.ui.features.visitors.history.VisitorHistoryScreen

internal fun isResidentsSection(route: String?): Boolean =
    route == Routes.ADMIN_RESIDENTS_MENU || route == Routes.CENSUS || route == Routes.ADMIN_VISITOR_HISTORY

internal fun NavGraphBuilder.adminResidentsDestinations(
    navController: NavHostController,
    activeRole: State<UserRole?>,
    onSessionExpired: () -> Unit = {},
    census: @Composable (() -> Unit) -> Unit = { CensusScreen(onNavigateBack = it) },
    history: @Composable (() -> Unit, () -> Unit) -> Unit = { back, select ->
        VisitorHistoryScreen(back, select, onSessionExpired)
    }
) {
    val back: () -> Unit = { navController.popBackStack() }
    composable(Routes.ADMIN_RESIDENTS_MENU) {
        AdminDestination(activeRole.value, "Residentes", back) {
            AdminResidentsMenuScreen(
                onOpenCensus = { navController.navigate(Routes.CENSUS) { launchSingleTop = true } },
                onOpenHistory = { navController.navigate(Routes.ADMIN_VISITOR_HISTORY) { launchSingleTop = true } }
            )
        }
    }
    composable(Routes.CENSUS) {
        census(back)
    }
    composable(Routes.ADMIN_VISITOR_HISTORY) {
        AdminDestination(activeRole.value, "Historial de visitantes", back) {
            history(back) { navController.navigate(Routes.CONJUNTO_SELECTION) }
        }
    }
}

@Composable
private fun AdminDestination(
    role: UserRole?,
    title: String,
    back: () -> Unit,
    content: @Composable () -> Unit
) {
    if (role == UserRole.ADMIN) {
        content()
    } else {
        Column {
            UrbanGuardTopBar(title = title, onNavigateBack = back)
            ErrorView(error = AppError.Forbidden(AppError.Forbidden.MESSAGE))
        }
    }
}
