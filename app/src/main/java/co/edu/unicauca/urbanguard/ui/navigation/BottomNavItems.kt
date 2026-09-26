package co.edu.unicauca.urbanguard.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent

val adminNavItems: List<NavItem> = listOf(
    NavItem(route = Routes.HOME, label = "Inicio", icon = Icons.Default.Home),
    NavItem(route = Routes.CENSUS, label = "Residentes", icon = Icons.Default.Person),
    NavItem(route = Routes.SECURITY, label = "Seguridad", icon = Icons.Default.Lock),
    NavItem(route = Routes.NEWS, label = "Noticias", icon = Icons.Default.Newspaper),
    NavItem(route = Routes.PROFILE, label = "Perfil", icon = Icons.Default.Person)
)

val residentNavItems: List<NavItem> = listOf(
    NavItem(route = Routes.HOME, label = "Inicio", icon = Icons.Default.Home),
    NavItem(route = Routes.VISITORS, label = "Visitantes", icon = Icons.Default.People),
    NavItem(route = Routes.SUPPORT, label = "Soporte", icon = Icons.Default.SupportAgent),
    NavItem(route = Routes.PROFILE, label = "Perfil", icon = Icons.Default.Person)
)

// Los ítems del Vigilante viven en GuardNavItems.kt (guardNavItems).