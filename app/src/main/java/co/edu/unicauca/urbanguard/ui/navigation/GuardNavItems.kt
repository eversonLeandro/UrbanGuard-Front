package co.edu.unicauca.urbanguard.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person

/**
 * Ítems de la barra de navegación inferior del Vigilante.
 * Mismo patrón que residentNavItems (BottomNavItems.kt):
 * Inicio, Visitantes, Seguridad, Perfil.
 */
val guardNavItems: List<NavItem> = listOf(
    NavItem(route = Routes.GUARD_HOME, label = "Inicio", icon = Icons.Default.Home),
    NavItem(route = Routes.GUARD_VISITANTES, label = "Visitantes", icon = Icons.Default.People),
    NavItem(route = Routes.GUARD_SEGURIDAD, label = "Seguridad", icon = Icons.Default.Lock),
    NavItem(route = Routes.GUARD_PERFIL, label = "Perfil", icon = Icons.Default.Person)
)
