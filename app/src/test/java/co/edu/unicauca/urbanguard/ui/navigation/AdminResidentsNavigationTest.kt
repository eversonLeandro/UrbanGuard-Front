package co.edu.unicauca.urbanguard.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.core.session.NoOpRolePersistence
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.usecase.residential.GetCensusUseCase
import co.edu.unicauca.urbanguard.domain.usecase.residential.RegisterUnitUseCase
import co.edu.unicauca.urbanguard.domain.usecase.residential.SetOwnerTenantActiveUseCase
import co.edu.unicauca.urbanguard.domain.usecase.residential.UpdateUnitUseCase
import co.edu.unicauca.urbanguard.ui.components.navigateBottomTab
import co.edu.unicauca.urbanguard.ui.features.residential.CensusScreen
import co.edu.unicauca.urbanguard.ui.features.residential.CensusUnitUseCases
import co.edu.unicauca.urbanguard.ui.features.residential.CensusViewModel
import co.edu.unicauca.urbanguard.ui.features.residential.FakeResidentialRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w450dp-h900dp")
class AdminResidentsNavigationTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var controller: NavHostController
    private val role = mutableStateOf<UserRole?>(UserRole.ADMIN)

    @Test
    fun `Residentes abre el menu y cada destino vuelve a la seccion`() {
        mountGraph()
        openResidentsTab()
        assertResidentsRoute(Routes.ADMIN_RESIDENTS_MENU)
        compose.onNodeWithText("Consulta los apartamentos y sus residentes registrados").assertIsDisplayed()
        compose.onNodeWithText("Consulta los ingresos de visitantes del conjunto").assertIsDisplayed()

        compose.onNodeWithText("Censo de apartamentos").performClick()
        assertResidentsRoute(Routes.CENSUS)
        compose.waitUntil(timeoutMillis = 5_000) { censusRepository.lastComplexId == "conjunto" }
        compose.onNodeWithText("101").assertIsDisplayed()
        compose.onNodeWithContentDescription("Volver").performClick()
        assertResidentsRoute(Routes.ADMIN_RESIDENTS_MENU)

        compose.onNodeWithText("Historial de visitantes").performClick()
        assertResidentsRoute(Routes.ADMIN_VISITOR_HISTORY)
        compose.onNodeWithText("Volver desde historial").performClick()
        assertResidentsRoute(Routes.ADMIN_RESIDENTS_MENU)
    }

    @Test
    fun `reseleccionar Residentes desde un hijo vuelve al menu sin duplicarlo`() {
        mountGraph()
        openResidentsTab()
        compose.onNodeWithText("Historial de visitantes").performClick()

        openResidentsTab()
        openResidentsTab()
        assertResidentsRoute(Routes.ADMIN_RESIDENTS_MENU)
        compose.runOnIdle {
            assertTrue(controller.popBackStack())
            assertEquals(Routes.HOME, controller.currentDestination?.route)
            assertFalse(isResidentsSection(controller.currentDestination?.route))
        }
    }

    @Test
    fun `volver desde otra pestana abre el menu y no restaura un hijo`() {
        mountGraph(start = Routes.LOGIN)
        compose.onNodeWithText("Iniciar prueba").performClick()
        openResidentsTab()
        for ((label, route) in listOf(
            "Censo de apartamentos" to Routes.CENSUS,
            "Historial de visitantes" to Routes.ADMIN_VISITOR_HISTORY
        )) {
            compose.onNodeWithText(label).performClick()
            assertResidentsRoute(route)
            compose.runOnIdle {
                controller.navigateBottomTab(Routes.SECURITY, controller.currentDestination?.route)
            }
            compose.onNodeWithText("Seguridad de prueba").assertIsDisplayed()
            openResidentsTab()
            assertResidentsRoute(Routes.ADMIN_RESIDENTS_MENU)
        }
        compose.runOnIdle {
            assertTrue(controller.popBackStack())
            assertEquals(Routes.HOME, controller.currentDestination?.route)
        }
    }

    @Test
    fun `ruta directa rechaza roles no administrativos y sesion ausente`() {
        mountGraph()
        assertTrue(residentNavItems.none { isResidentsSection(it.route) })
        assertTrue(guardNavItems.none { isResidentsSection(it.route) })
        for (invalidRole in listOf(UserRole.RESIDENT, UserRole.GUARD, UserRole.SUPERADMIN, UserRole.USER, null)) {
            compose.runOnIdle {
                role.value = invalidRole
                controller.navigate(Routes.ADMIN_RESIDENTS_MENU)
            }
            compose.onNodeWithText("Sin permisos").assertIsDisplayed()
            compose.onNodeWithText("Censo de apartamentos").assertDoesNotExist()
            compose.runOnIdle {
                controller.popBackStack()
                controller.navigate(Routes.ADMIN_VISITOR_HISTORY)
            }
            compose.onNodeWithText("Sin permisos").assertIsDisplayed()
            compose.onNodeWithText("Volver desde historial").assertDoesNotExist()
            compose.runOnIdle { controller.popBackStack() }
        }
    }

    @Test
    fun `perder el rol retira el destino de historial ya abierto`() {
        mountGraph()
        openResidentsTab()
        compose.onNodeWithText("Historial de visitantes").performClick()
        compose.onNodeWithText("Volver desde historial").assertIsDisplayed()

        compose.runOnIdle { role.value = UserRole.RESIDENT }
        compose.onNodeWithText("Sin permisos").assertIsDisplayed()
        compose.onNodeWithText("Volver desde historial").assertDoesNotExist()
    }

    private fun mountGraph(start: String = Routes.HOME) {
        compose.setContent {
            val nav = rememberNavController()
            controller = nav
            NavHost(navController = nav, startDestination = start) {
                composable(Routes.LOGIN) {
                    TextButton(onClick = {
                        nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    }) { Text("Iniciar prueba") }
                }
                composable(Routes.HOME) { Text("Inicio") }
                composable(Routes.SECURITY) { Text("Seguridad de prueba") }
                adminResidentsDestinations(
                    navController = nav,
                    activeRole = role,
                    census = { back ->
                        CensusScreen(onNavigateBack = back, viewModel = remember { censusViewModel() })
                    },
                    history = { back, _ -> TextButton(onClick = back) { Text("Volver desde historial") } }
                )
            }
        }
    }

    private fun openResidentsTab() = compose.runOnIdle {
        val tab = adminNavItems.single { it.label == "Residentes" }
        controller.navigateBottomTab(tab.route, controller.currentDestination?.route)
    }

    private fun assertResidentsRoute(route: String) = compose.runOnIdle {
        assertEquals(route, controller.currentDestination?.route)
        assertTrue(isResidentsSection(controller.currentDestination?.route))
    }

    private val censusRepository = FakeResidentialRepository(
        Result.success(listOf(ResidentialUnit("apartment-101", "101", emptyList())))
    )

    private fun censusViewModel(): CensusViewModel {
        val session = SessionManager(NoOpRolePersistence).apply {
            saveSession(
                UserSession(
                    "test-token", "admin", "Admin", UserRole.ADMIN,
                    conjuntoActivoId = "conjunto", conjuntoActivoNombre = "Conjunto de prueba"
                )
            )
        }
        return CensusViewModel(
            GetCensusUseCase(censusRepository),
            CensusUnitUseCases(
                RegisterUnitUseCase(censusRepository),
                UpdateUnitUseCase(censusRepository),
                SetOwnerTenantActiveUseCase(censusRepository)
            ),
            session
        )
    }
}
