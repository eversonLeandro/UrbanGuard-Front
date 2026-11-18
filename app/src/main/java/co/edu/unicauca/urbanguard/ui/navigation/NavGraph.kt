package co.edu.unicauca.urbanguard.ui.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.components.PlaceholderScreen
import co.edu.unicauca.urbanguard.ui.features.admin.AdminSecurityMenuScreen
import co.edu.unicauca.urbanguard.ui.features.admin.RegisterGuardScreen
import co.edu.unicauca.urbanguard.ui.features.admin.GuardManagementScreen
import co.edu.unicauca.urbanguard.ui.features.admin.SuperAdminScreen
import co.edu.unicauca.urbanguard.ui.features.users.UserListScreen
import co.edu.unicauca.urbanguard.ui.features.auth.LoginScreen
import co.edu.unicauca.urbanguard.ui.features.auth.PasswordResetScreen
import co.edu.unicauca.urbanguard.ui.features.auth.RegisterScreen
import co.edu.unicauca.urbanguard.ui.features.guard.GuardHomeScreen
import co.edu.unicauca.urbanguard.ui.features.guard.GuardScanQrScreen
import co.edu.unicauca.urbanguard.ui.features.guard.GuardSeguridadScreen
import co.edu.unicauca.urbanguard.ui.features.guard.RoundActiveScreen
import co.edu.unicauca.urbanguard.ui.features.guard.RoundSummaryScreen
import co.edu.unicauca.urbanguard.ui.features.incidents.IncidentDetailScreen
import co.edu.unicauca.urbanguard.ui.features.incidents.IncidentFormScreen
import co.edu.unicauca.urbanguard.ui.features.incidents.IncidentsScreen
import co.edu.unicauca.urbanguard.ui.features.guard.GuardVisitantesScreen
import co.edu.unicauca.urbanguard.ui.features.home.HomeScreen
import co.edu.unicauca.urbanguard.ui.features.news.EditNewsScreen
import co.edu.unicauca.urbanguard.ui.features.news.NewsFeedScreen
import co.edu.unicauca.urbanguard.ui.features.news.PublishNewsScreen
import co.edu.unicauca.urbanguard.ui.features.notifications.NotificationInboxScreen
import co.edu.unicauca.urbanguard.ui.features.pqrs.PqrsDetailScreen
import co.edu.unicauca.urbanguard.ui.features.pqrs.PqrsInboxScreen
import co.edu.unicauca.urbanguard.ui.features.pqrs.CreatePqrsScreen
import co.edu.unicauca.urbanguard.ui.features.pqrs.MyPqrsDetailScreen
import co.edu.unicauca.urbanguard.ui.features.pqrs.MyPqrsScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.ActivateConjuntoScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.ConjuntoCodeScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.ConjuntoSelectionScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.LinkByCodeScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.LinkRequestDetailScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.PendingRequestsScreen
import co.edu.unicauca.urbanguard.ui.features.onboarding.RegisterConjuntoScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolGuardSummaryScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolHistoryScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolPointFormScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolPointsScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolRoundDetailScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolRouteFormScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolRoutesScreen
import co.edu.unicauca.urbanguard.ui.features.patrol.PatrolShiftDetailScreen
import co.edu.unicauca.urbanguard.ui.features.profile.ProfileScreen
import co.edu.unicauca.urbanguard.ui.features.packages.PackageListScreen
import co.edu.unicauca.urbanguard.ui.features.packages.RegisterPackageScreen
import co.edu.unicauca.urbanguard.ui.features.packages.PackageDetailScreen
import co.edu.unicauca.urbanguard.ui.features.packages.PackageMetricsScreen
import co.edu.unicauca.urbanguard.ui.features.visitors.GenerateQrScreen
import co.edu.unicauca.urbanguard.ui.features.visitors.GeneratedPassScreen
import co.edu.unicauca.urbanguard.ui.features.visitors.InvitationPassScreen
import co.edu.unicauca.urbanguard.ui.features.visitors.VisitorPassListScreen

// Clave en el savedStateHandle de PENDING_REQUESTS para recibir el
// resultado de la decisión (APROBADA/RECHAZADA) tomada en el detalle.
private const val KEY_REVIEW_RESULT = "review_result"

// Cierre de sesión: vacía por completo el back stack y deja solo el Login.
// popUpTo(0) es un id inexistente, así que se descartan TODAS las entradas
// (incluidas las de la sesión anterior). Con el botón "atrás" en el Login
// la app se cierra: es imposible volver a una pantalla autenticada.
fun NavHostController.navigateToLogin() {
    navigate(Routes.LOGIN) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

// Pantalla de entrada según el rol. null (sin sesión) → Login.
fun roleToDestination(role: UserRole?): String = when (role) {
    UserRole.USER -> Routes.CONJUNTO_SELECTION
    UserRole.SUPERADMIN -> Routes.ADMIN
    UserRole.GUARD -> Routes.GUARD_HOME
    UserRole.ADMIN, UserRole.RESIDENT -> Routes.HOME
    else -> Routes.LOGIN
}

/**
 * Mapa de navegacion completo de la app: decide la pantalla de entrada segun el rol
 * y registra cada destino.
 *
 * Suprimido de LongMethod a proposito. Partir este archivo por rol pierde la vista
 * unica de las rutas, que es lo que lo hace legible y revisable, y ademas NavHost
 * quedaria repartido en varios grafos sin tipado de destinos. Si alguna vez se supera
 * la vista por rol con un unico NavHost en AppNavGraph, la supresion debe retirarse.
 */
@Suppress("LongMethod")
@Composable
fun AppNavGraph(
    activeRole: State<UserRole?>,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    // Mientras se restaura la sesión persistida se muestra un splash breve;
    // el NavHost arranca directamente en la pantalla del rol (sin flash de Login).
    var restoring by remember { mutableStateOf(true) }
    var initialRoute by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        initialRoute = sessionViewModel.initialRoute()
        restoring = false
    }

    if (restoring) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(co.edu.unicauca.urbanguard.R.drawable.logo_u),
                contentDescription = "Logo UrbanGuard",
                modifier = Modifier.size(120.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
        return
    }

    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = initialRoute ?: Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    // El rol de Auth0 determina el destino directamente.
                    // USER (centinela) → el usuario debe seleccionar o crear un conjunto.
                    // SUPERADMIN → panel global de plataforma (Capa 2.3, D3).
                    // ADMIN / RESIDENT / GUARD ya tienen conjunto activo en Auth0 → Home.
                    // La carga real de conjuntos ocurre en ConjuntoSelectionViewModel.
                    val destination = roleToDestination(role)
                    Log.d(
                        "NavGraph",
                        "=== [LOGIN NAVIGATION] role: $role -> destino: $destination ==="
                    )
                    navController.navigate(destination) {
                        // Elimina el Login del backstack — el usuario
                        // no puede volver atrás con el botón físico
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    // Pendiente (UG-45): navegar a RegisterConjuntoScreen
                    navController.navigate(Routes.ACTIVATE_CONJUNTO)
                },
                onNavigateToRegister = {
                    // "¿Nuevo en UrbanGuard? Empezar aquí" → formulario de registro
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToPasswordReset = { email ->
                    navController.navigate(
                        "${Routes.PASSWORD_RESET.substringBefore("?")}?email=${Uri.encode(email)}"
                    )
                }
            )
        }

        composable(
            route = Routes.PASSWORD_RESET,
            arguments = listOf(navArgument("email") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            PasswordResetScreen(
                initialEmail = backStackEntry.arguments?.getString("email").orEmpty(),
                onNavigateToLogin = { navController.popBackStack() }
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
                onConjuntoSelected = { _, role ->
                    // setActiveConjunto ya lo hizo el ViewModel (llamado onConjuntoSelected),
                    // aquí solo se navega al Home del rol correspondiente a ese conjunto.
                    // Se vacía la pila: el destino queda como única entrada de la sesión.
                    navController.navigate(roleToDestination(role)) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onCreateConjunto = {
                    navController.navigate(Routes.ACTIVATE_CONJUNTO)
                },
                onJoinByCode = {
                    navController.navigate(Routes.LINK_BY_CODE)
                },
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigateToLogin()
                    }
                },
                // Solo se ofrece volver cuando la pantalla no es el punto de
                // partida de la sesión; en el onboarding inicial no hay a dónde ir.
                onNavigateBack = if (navController.previousBackStackEntry != null) {
                    { navController.popBackStack() }
                } else {
                    null
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
                onNavigateBack = {
                    navController.popBackStack()
                },
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
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenConjuntoSelection = {
                    navController.navigate(Routes.CONJUNTO_SELECTION)
                },
                onOpenConjuntoCode = {
                    navController.navigate(Routes.CONJUNTO_CODE)
                },
                onOpenPendingRequests = {
                    navController.navigate(Routes.PENDING_REQUESTS)
                },
                onOpenGenerateQr = {
                    // Mismas opciones que usa la pestaña del bottom nav, para no
                    // duplicar la pila ni romper la selección de pestaña activa.
                    navController.navigate(Routes.VISITORS) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenPqrsInbox = { navController.navigate(Routes.PQRS_INBOX) },
                onOpenMyPqrs = { navController.navigate(Routes.MY_PQRS) },
                onOpenNews = { navController.navigate(Routes.NEWS) },
                onOpenPackageMetrics = { navController.navigate(Routes.PACKAGE_METRICS) }
            )
        }

        composable(Routes.PACKAGE_METRICS) {
            PackageMetricsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.PQRS_INBOX) {
            PqrsInboxScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenDetail = { pqrsId -> navController.navigate("pqrs_detail/$pqrsId") }
            )
        }

        composable(Routes.MY_PQRS) {
            MyPqrsScreen(
                onNavigateBack = { navController.popBackStack() },
                onRadicar = { navController.navigate(Routes.CREATE_PQRS) },
                onOpenDetail = { id -> navController.navigate("my_pqrs_detail/$id") }
            )
        }

        composable(Routes.CREATE_PQRS) {
            CreatePqrsScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreated = { id ->
                    navController.navigate("my_pqrs_detail/$id") {
                        popUpTo(Routes.CREATE_PQRS) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.MY_PQRS_DETAIL,
            arguments = listOf(navArgument("pqrsId") { type = NavType.StringType })
        ) {
            MyPqrsDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.PQRS_DETAIL,
            arguments = listOf(navArgument("pqrsId") { type = NavType.StringType })
        ) {
            PqrsDetailScreen(onNavigateBack = { navController.popBackStack() })
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

        adminResidentsDestinations(
            navController = navController,
            activeRole = activeRole,
            onSessionExpired = { sessionViewModel.logout { navController.navigateToLogin() } }
        )

        composable(Routes.SECURITY) {
            AdminSecurityMenuScreen(
                onNavigateToRegisterGuard = {
                    navController.navigate(Routes.REGISTER_GUARD)
                },
                onNavigateToGuardManagement = {
                    navController.navigate(Routes.GUARD_MANAGEMENT)
                },
                onNavigateToPatrolHistory = {
                    navController.navigate(Routes.PATROL_HISTORY)
                },
                onNavigateToPatrolPoints = {
                    navController.navigate(Routes.PATROL_POINTS)
                },
                onNavigateToPatrolRoutes = {
                    navController.navigate(Routes.PATROL_ROUTES)
                },
                onNavigateToIncidents = {
                    navController.navigate(Routes.ADMIN_INCIDENTS)
                }
            )
        }

        composable(Routes.ADMIN_INCIDENTS) {
            IncidentsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddClick = {},
                isAdmin = true,
                onIncidentClick = { incident ->
                    navController.navigate("admin_incident_detail/${incident.id}")
                }
            )
        }

        composable(
            route = Routes.ADMIN_INCIDENT_DETAIL,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { entry ->
            IncidentDetailScreen(
                reportId = entry.arguments?.getString("reportId").orEmpty(),
                isAdmin = true,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PATROL_HISTORY) {
            PatrolHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onShiftClick = { shiftType, date, from, to ->
                    navController.navigate(
                        "patrol_shift_detail?shiftType=${shiftType.name}" +
                            "&date=$date&from=$from&to=$to"
                    )
                }
            )
        }

        composable(
            route = Routes.PATROL_SHIFT_DETAIL,
            arguments = listOf(
                navArgument("shiftType") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
                navArgument("from") { type = NavType.StringType },
                navArgument("to") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from").orEmpty()
            val to = backStackEntry.arguments?.getString("to").orEmpty()
            PatrolShiftDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRoundClick = { occurrenceId ->
                    navController.navigate(
                        "patrol_round_detail?occurrenceId=$occurrenceId"
                    )
                },
                onGuardClick = { guardId ->
                    navController.navigate(
                        "patrol_guard_summary?guardId=$guardId&from=$from&to=$to"
                    )
                },
                onIncidentClick = { reportId ->
                    navController.navigate("admin_incident_detail/$reportId")
                }
            )
        }

        composable(
            route = Routes.PATROL_ROUND_DETAIL,
            arguments = listOf(
                navArgument("occurrenceId") { type = NavType.StringType },
                navArgument("guardName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            PatrolRoundDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.PATROL_GUARD_SUMMARY,
            arguments = listOf(
                navArgument("guardId") { type = NavType.StringType },
                navArgument("from") { type = NavType.StringType },
                navArgument("to") { type = NavType.StringType }
            )
        ) {
            PatrolGuardSummaryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.REGISTER_GUARD) {
            RegisterGuardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.GUARD_MANAGEMENT) {
            GuardManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PATROL_POINTS) {
            PatrolPointsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddClick = {
                    navController.navigate("patrol_point_form")
                },
                onEditClick = { pointId ->
                    navController.navigate("patrol_point_form?pointId=$pointId")
                }
            )
        }

        composable(
            route = Routes.PATROL_POINT_FORM,
            arguments = listOf(
                navArgument("pointId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            PatrolPointFormScreen(
                pointId = backStackEntry.arguments?.getString("pointId"),
                onSaved = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.PATROL_ROUTES) {
            PatrolRoutesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddClick = {
                    navController.navigate("patrol_route_form")
                },
                onEditClick = { routeId ->
                    navController.navigate("patrol_route_form?routeId=$routeId")
                }
            )
        }

        composable(
            route = Routes.PATROL_ROUTE_FORM,
            arguments = listOf(
                navArgument("routeId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            PatrolRouteFormScreen(
                routeId = backStackEntry.arguments?.getString("routeId"),
                onSaved = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.NEWS) {
            NewsFeedScreen(
                onPublishClick = {
                    navController.navigate(Routes.PUBLISH_NEWS)
                },
                onEditClick = { news ->
                    navController.navigate(Routes.editNews(news.announcementId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.PUBLISH_NEWS) {
            PublishNewsScreen(
                onPublished = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.EDIT_NEWS,
            arguments = listOf(navArgument("announcementId") { type = NavType.StringType })
        ) {
            EditNewsScreen(
                onSaved = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.VISITORS) {
            VisitorPassListScreen(
                onGenerateNewClick = {
                    navController.navigate(Routes.VISITORS_NEW_PASS)
                },
                onInvitationClick = {
                    navController.navigate(Routes.VISITORS_PASS_DETAIL)
                }
            )
        }
        composable(Routes.VISITORS_NEW_PASS) {
            GenerateQrScreen(
                onGenerated = {
                    // El pase ya quedó guardado en el repositorio: al volver
                    // atrás desde la vista del pase se descarta el formulario
                    // para no regenerarlo al girar el dispositivo.
                    navController.navigate(Routes.VISITORS_PASS) {
                        popUpTo(Routes.VISITORS_NEW_PASS) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.VISITORS_PASS) {
            GeneratedPassScreen(
                onBackClick = { navController.popBackStack() },
                onGenerateAnotherClick = {
                    navController.navigate(Routes.VISITORS_NEW_PASS)
                }
            )
        }
        composable(Routes.VISITORS_PASS_DETAIL) {
            InvitationPassScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Routes.LINK_BY_CODE) {
            LinkByCodeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLinkSuccess = {
                    // La solicitud está pendiente de aprobación — el botón
                    // "Volver a mis conjuntos" y la flecha del top bar de la
                    // pantalla de éxito devuelven al selector.
                    navController.navigate(Routes.CONJUNTO_SELECTION) {
                        popUpTo(navController.graph.id) { inclusive = true }
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
            PackageListScreen(
                onRegisterPackage = { navController.navigate(Routes.REGISTER_PACKAGE) },
                onPackageDetail = { packageId -> navController.navigate("packages/detail/$packageId") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PACKAGE_DETAIL,
            arguments = listOf(navArgument("packageId") { type = NavType.StringType })
        ) {
            PackageDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.REGISTER_PACKAGE) {
            RegisterPackageScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationInboxScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenDeepLink = { deepLink ->
                    routeForDeepLink(deepLink)?.let { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.NOTIFICATIONS) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigateToLogin()
                    }
                }
            )
        }

        composable(Routes.ADMIN) {
            SuperAdminScreen(
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigateToLogin()
                    }
                },
                onOpenUserManagement = { navController.navigate(Routes.USER_MANAGEMENT) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        // ── Consola global (SUPERADMIN, HU-006) ───────────────────────
        composable(Routes.USER_MANAGEMENT) {
            UserListScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ── Vigilante (rol GUARD) ─────────────────────────────────────
        composable(Routes.GUARD_HOME) {
            GuardHomeScreen(
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onScanQr = {
                    navController.navigate(Routes.GUARD_SCAN_QR)
                },
                onReportIncident = {
                    navController.navigate(Routes.GUARD_INCIDENTS)
                },
                onPackages = {
                    navController.navigate(Routes.PACKAGES)
                },
                // Salida del guardia sin membresía activa: cierra sesión completa
                // y limpia el back stack, mismo camino que el resto de la app.
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigateToLogin()
                    }
                }
            )
        }

        composable(Routes.GUARD_VISITANTES) {
            GuardVisitantesScreen(
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onScanQr = {
                    navController.navigate(Routes.GUARD_SCAN_QR)
                }
            )
        }

        composable(Routes.GUARD_SEGURIDAD) {
            GuardSeguridadScreen(
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onActiveRound = {
                    navController.navigate(Routes.GUARD_ROUND_ACTIVE)
                },
                onOpenRound = { roundId ->
                    navController.navigate(
                        Routes.GUARD_ROUND_SUMMARY.replace("{roundId}", roundId)
                    )
                }
            )
        }

        composable(Routes.GUARD_PERFIL) {
            ProfileScreen(
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigateToLogin()
                    }
                }
            )
        }

        composable(Routes.GUARD_SCAN_QR) {
            GuardScanQrScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.GUARD_ROUND_ACTIVE) {
            RoundActiveScreen(
                onFinished = {
                    navController.navigate(
                        Routes.GUARD_ROUND_SUMMARY.substringBefore("?")
                    ) {
                        popUpTo(Routes.GUARD_ROUND_ACTIVE) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReportIncident = { executionId, pointId ->
                    navController.navigate(
                        "guard_incident_form?pointId=${pointId.orEmpty()}" +
                            "&executionId=${executionId.orEmpty()}"
                    )
                }
            )
        }

        composable(
            Routes.GUARD_ROUND_SUMMARY,
            arguments = listOf(
                navArgument("roundId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            RoundSummaryScreen(
                onBackClick = {
                    navController.popBackStack(
                        Routes.GUARD_SEGURIDAD,
                        inclusive = false
                    )
                },
                onBackToRounds = {
                    navController.popBackStack(
                        Routes.GUARD_SEGURIDAD,
                        inclusive = false
                    )
                }
            )
        }

        composable(Routes.GUARD_INCIDENTS) {
            IncidentsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddClick = {
                    navController.navigate("guard_incident_form")
                },
                onIncidentClick = {
                    navController.navigate("guard_incident_detail/${it.id}")
                }
            )
        }

        composable(
            route = Routes.GUARD_INCIDENT_FORM,
            arguments = listOf(
                navArgument("pointId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("executionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            IncidentFormScreen(
                pointId = backStackEntry.arguments?.getString("pointId"),
                executionId = backStackEntry.arguments?.getString("executionId"),
                onSaved = {
                    navController.navigate(Routes.GUARD_INCIDENTS) {
                        popUpTo(Routes.GUARD_INCIDENTS) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.GUARD_INCIDENT_DETAIL,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { entry ->
            IncidentDetailScreen(
                reportId = entry.arguments?.getString("reportId").orEmpty(),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
